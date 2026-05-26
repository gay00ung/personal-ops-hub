package net.lateinint

import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement
import kotlin.io.path.absolutePathString
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class OpsDatabase(private val path: Path) {
    init {
        path.parent?.let { Files.createDirectories(it) }
        migrate()
    }

    @Synchronized
    fun insertMetric(snapshot: SystemSnapshot, retentionHours: Long) {
        connection().use { conn ->
            conn.prepareStatement("insert or replace into metrics(timestamp, payload) values(?, ?)").use { stmt ->
                stmt.setLong(1, snapshot.timestamp)
                stmt.setString(2, AppJson.encodeToString(SystemSnapshot.serializer(), snapshot))
                stmt.executeUpdate()
            }
            val cutoff = snapshot.timestamp - retentionHours.hours.inWholeMilliseconds
            conn.prepareStatement("delete from metrics where timestamp < ?").use { stmt ->
                stmt.setLong(1, cutoff)
                stmt.executeUpdate()
            }
        }
    }

    @Synchronized
    fun recentMetrics(since: Long, limit: Int = 240): List<SystemSnapshot> =
        connection().use { conn ->
            conn.prepareStatement(
                """
                select payload
                from metrics
                where timestamp >= ?
                order by timestamp desc
                limit ?
                """.trimIndent(),
            ).use { stmt ->
                stmt.setLong(1, since)
                stmt.setInt(2, limit)
                stmt.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            runCatching {
                                AppJson.decodeFromString(SystemSnapshot.serializer(), rs.getString("payload"))
                            }.getOrNull()?.let(::add)
                        }
                    }.asReversed()
                }
            }
        }

    @Synchronized
    fun pruneEvents(retentionDays: Long, now: Long = System.currentTimeMillis()): Int {
        if (retentionDays <= 0) return 0
        val cutoff = now - retentionDays.days.inWholeMilliseconds
        return connection().use { conn ->
            conn.prepareStatement(
                """
                delete from events
                where timestamp < ?
                    and (
                        action_required = 0
                        or state = ?
                    )
                """.trimIndent(),
            ).use { stmt ->
                stmt.setLong(1, cutoff)
                stmt.setString(2, EventState.RESOLVED.name)
                stmt.executeUpdate()
            }
        }
    }

    @Synchronized
    fun upsertServiceCheck(result: ServiceCheckResult) {
        connection().use { conn ->
            conn.prepareStatement(
                """
                insert into service_checks(name, kind, status, checked_at, payload)
                values(?, ?, ?, ?, ?)
                on conflict(name, kind) do update set
                    status = excluded.status,
                    checked_at = excluded.checked_at,
                    payload = excluded.payload
                """.trimIndent(),
            ).use { stmt ->
                stmt.setString(1, result.name)
                stmt.setString(2, result.kind.name)
                stmt.setString(3, result.status.name)
                stmt.setLong(4, result.checkedAt)
                stmt.setString(5, AppJson.encodeToString(ServiceCheckResult.serializer(), result))
                stmt.executeUpdate()
            }
        }
    }

    @Synchronized
    fun latestServiceChecks(): List<ServiceCheckResult> =
        connection().use { conn ->
            conn.prepareStatement("select payload from service_checks order by name, kind").use { stmt ->
                stmt.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            runCatching {
                                AppJson.decodeFromString(ServiceCheckResult.serializer(), rs.getString("payload"))
                            }.getOrNull()?.let(::add)
                        }
                    }
                }
            }
        }

    @Synchronized
    fun insertEvent(
        severity: EventSeverity,
        source: String,
        message: String,
        details: String? = null,
        timestamp: Long = System.currentTimeMillis(),
        actionRequired: Boolean = severity != EventSeverity.INFO,
    ): EventRecord =
        connection().use { conn ->
            conn.prepareStatement(
                """
                insert into events(timestamp, severity, source, message, details, state, action_required)
                values(?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                Statement.RETURN_GENERATED_KEYS,
            ).use { stmt ->
                val initialState = if (actionRequired) EventState.OPEN else EventState.RESOLVED
                stmt.setLong(1, timestamp)
                stmt.setString(2, severity.name)
                stmt.setString(3, source)
                stmt.setString(4, message)
                stmt.setString(5, details)
                stmt.setString(6, initialState.name)
                stmt.setInt(7, if (actionRequired) 1 else 0)
                stmt.executeUpdate()
                stmt.generatedKeys.use { keys ->
                    val id = if (keys.next()) keys.getLong(1) else 0L
                    EventRecord(id, timestamp, severity, source, message, details, initialState, actionRequired)
                }
            }
        }

    @Synchronized
    fun recentEvents(
        limit: Int = 100,
        severity: EventSeverity? = null,
        state: EventState? = null,
        query: String? = null,
    ): List<EventRecord> =
        connection().use { conn ->
            val conditions = mutableListOf<String>()
            val args = mutableListOf<String>()

            severity?.let {
                conditions += "severity = ?"
                args += it.name
            }
            state?.let {
                conditions += "state = ?"
                args += it.name
                conditions += "action_required = 1"
            }
            query?.trim()?.lowercase()?.takeIf { it.isNotBlank() }?.let {
                conditions += "(lower(source) like ? or lower(message) like ? or lower(coalesce(details, '')) like ?)"
                repeat(3) { _ -> args += "%$it%" }
            }

            val where = if (conditions.isEmpty()) "" else "where ${conditions.joinToString(" and ")}"
            conn.prepareStatement(
                """
                select id, timestamp, severity, source, message, details, state, action_required
                from events
                $where
                order by timestamp desc
                limit ?
                """.trimIndent(),
            ).use { stmt ->
                args.forEachIndexed { index, value -> stmt.setString(index + 1, value) }
                stmt.setInt(args.size + 1, limit)
                stmt.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(rs.toEventRecord())
                        }
                    }
                }
            }
        }

    @Synchronized
    fun updateEventState(id: Long, state: EventState): EventRecord? =
        connection().use { conn ->
            val existing = eventById(conn, id) ?: return@use null
            if (!existing.actionRequired) return@use existing

            val updated = conn.prepareStatement("update events set state = ? where id = ? and action_required = 1").use { stmt ->
                stmt.setString(1, state.name)
                stmt.setLong(2, id)
                stmt.executeUpdate()
            }
            if (updated == 0) null else eventById(conn, id)
        }

    @Synchronized
    fun resolveOpenEvents(source: String): Int =
        connection().use { conn ->
            conn.prepareStatement(
                """
                update events
                set state = ?
                where source = ?
                    and action_required = 1
                    and state in (?, ?)
                """.trimIndent(),
            ).use { stmt ->
                stmt.setString(1, EventState.RESOLVED.name)
                stmt.setString(2, source)
                stmt.setString(3, EventState.OPEN.name)
                stmt.setString(4, EventState.ACKNOWLEDGED.name)
                stmt.executeUpdate()
            }
        }

    @Synchronized
    fun openActionEventSources(prefix: String): List<String> =
        connection().use { conn ->
            conn.prepareStatement(
                """
                select distinct source
                from events
                where action_required = 1
                    and state in (?, ?)
                    and source like ?
                order by source
                """.trimIndent(),
            ).use { stmt ->
                stmt.setString(1, EventState.OPEN.name)
                stmt.setString(2, EventState.ACKNOWLEDGED.name)
                stmt.setString(3, "$prefix%")
                stmt.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) add(rs.getString("source"))
                    }
                }
            }
        }

    private fun eventById(conn: Connection, id: Long): EventRecord? =
        conn.prepareStatement(
            """
            select id, timestamp, severity, source, message, details, state, action_required
            from events
            where id = ?
            """.trimIndent(),
        ).use { stmt ->
            stmt.setLong(1, id)
            stmt.executeQuery().use { rs ->
                if (rs.next()) rs.toEventRecord() else null
            }
        }

    @Synchronized
    fun getState(key: String): String? =
        connection().use { conn ->
            conn.prepareStatement("select value from automation_state where key = ?").use { stmt ->
                stmt.setString(1, key)
                stmt.executeQuery().use { rs -> if (rs.next()) rs.getString("value") else null }
            }
        }

    @Synchronized
    fun getStateEntry(key: String): AutomationStateEntry? =
        connection().use { conn ->
            conn.prepareStatement("select value, updated_at from automation_state where key = ?").use { stmt ->
                stmt.setString(1, key)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) AutomationStateEntry(rs.getString("value"), rs.getLong("updated_at")) else null
                }
            }
        }

    @Synchronized
    fun setState(key: String, value: String) {
        connection().use { conn ->
            conn.prepareStatement(
                """
                insert into automation_state(key, value, updated_at)
                values(?, ?, ?)
                on conflict(key) do update set
                    value = excluded.value,
                    updated_at = excluded.updated_at
                """.trimIndent(),
            ).use { stmt ->
                stmt.setString(1, key)
                stmt.setString(2, value)
                stmt.setLong(3, System.currentTimeMillis())
                stmt.executeUpdate()
            }
        }
    }

    private fun migrate() {
        connection().use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeUpdate(
                    """
                    create table if not exists metrics(
                        timestamp integer primary key,
                        payload text not null
                    )
                    """.trimIndent(),
                )
                stmt.executeUpdate(
                    """
                    create table if not exists service_checks(
                        name text not null,
                        kind text not null,
                        status text not null,
                        checked_at integer not null,
                        payload text not null,
                        primary key(name, kind)
                    )
                    """.trimIndent(),
                )
                stmt.executeUpdate(
                    """
                    create table if not exists events(
                        id integer primary key autoincrement,
                        timestamp integer not null,
                        severity text not null,
                        source text not null,
                        message text not null,
                        details text,
                        state text not null default 'OPEN',
                        action_required integer not null default 1
                    )
                    """.trimIndent(),
                )
                ensureEventsStateColumn(conn)
                ensureEventsActionRequiredColumn(conn)
                stmt.executeUpdate("create index if not exists idx_events_timestamp on events(timestamp desc)")
                stmt.executeUpdate("create index if not exists idx_events_state on events(state)")
                stmt.executeUpdate("create index if not exists idx_events_severity on events(severity)")
                stmt.executeUpdate("create index if not exists idx_events_action_required on events(action_required)")
                stmt.executeUpdate(
                    """
                    create table if not exists automation_state(
                        key text primary key,
                        value text not null,
                        updated_at integer not null
                    )
                    """.trimIndent(),
                )
            }
        }
    }

    private fun connection(): Connection =
        DriverManager.getConnection("jdbc:sqlite:${path.absolutePathString()}").apply {
            createStatement().use { stmt ->
                stmt.execute("pragma journal_mode = WAL")
                stmt.execute("pragma busy_timeout = 5000")
            }
        }

    private fun ResultSet.toEventRecord(): EventRecord =
        EventRecord(
            id = getLong("id"),
            timestamp = getLong("timestamp"),
            severity = EventSeverity.valueOf(getString("severity")),
            source = getString("source"),
            message = getString("message"),
            details = getString("details"),
            state = runCatching { EventState.valueOf(getString("state")) }.getOrDefault(EventState.OPEN),
            actionRequired = getInt("action_required") == 1,
        )

    private fun ensureEventsStateColumn(conn: Connection) {
        val hasState = conn.prepareStatement("pragma table_info(events)").use { stmt ->
            stmt.executeQuery().use { rs ->
                generateSequence { if (rs.next()) rs.getString("name") else null }.any { it == "state" }
            }
        }
        if (!hasState) {
            conn.createStatement().use { stmt ->
                stmt.executeUpdate("alter table events add column state text not null default 'OPEN'")
            }
        }
    }

    private fun ensureEventsActionRequiredColumn(conn: Connection) {
        val hasActionRequired = conn.prepareStatement("pragma table_info(events)").use { stmt ->
            stmt.executeQuery().use { rs ->
                generateSequence { if (rs.next()) rs.getString("name") else null }.any { it == "action_required" }
            }
        }
        if (!hasActionRequired) {
            conn.createStatement().use { stmt ->
                stmt.executeUpdate("alter table events add column action_required integer not null default 1")
                stmt.executeUpdate("update events set action_required = case when severity in ('CRITICAL', 'WARNING') then 1 else 0 end")
                stmt.executeUpdate("update events set state = 'RESOLVED' where action_required = 0")
            }
        }
    }
}

data class AutomationStateEntry(
    val value: String,
    val updatedAt: Long,
)
