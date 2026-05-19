package net.lateinint

import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement
import kotlin.io.path.absolutePathString
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
    ): EventRecord =
        connection().use { conn ->
            conn.prepareStatement(
                "insert into events(timestamp, severity, source, message, details) values(?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS,
            ).use { stmt ->
                stmt.setLong(1, timestamp)
                stmt.setString(2, severity.name)
                stmt.setString(3, source)
                stmt.setString(4, message)
                stmt.setString(5, details)
                stmt.executeUpdate()
                stmt.generatedKeys.use { keys ->
                    val id = if (keys.next()) keys.getLong(1) else 0L
                    EventRecord(id, timestamp, severity, source, message, details)
                }
            }
        }

    @Synchronized
    fun recentEvents(limit: Int = 100): List<EventRecord> =
        connection().use { conn ->
            conn.prepareStatement(
                """
                select id, timestamp, severity, source, message, details
                from events
                order by timestamp desc
                limit ?
                """.trimIndent(),
            ).use { stmt ->
                stmt.setInt(1, limit)
                stmt.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(
                                EventRecord(
                                    id = rs.getLong("id"),
                                    timestamp = rs.getLong("timestamp"),
                                    severity = EventSeverity.valueOf(rs.getString("severity")),
                                    source = rs.getString("source"),
                                    message = rs.getString("message"),
                                    details = rs.getString("details"),
                                ),
                            )
                        }
                    }
                }
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
                        details text
                    )
                    """.trimIndent(),
                )
                stmt.executeUpdate("create index if not exists idx_events_timestamp on events(timestamp desc)")
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
}
