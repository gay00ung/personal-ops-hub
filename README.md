# personal-ops-hub

A personal server monitoring and automation hub built with Ktor.

## What it does

- Shows CPU, memory, disk, uptime, load average, and JVM status.
- Stores recent metrics in SQLite for a one-hour dashboard graph.
- Checks HTTP endpoints, TCP ports, Docker containers, and backup markers.
- Shows a read-only server inventory for cron, systemd timers, services, Docker, and listening ports.
- Shows recent `journalctl` output for explicitly allowed systemd units.
- Shows recent `docker logs` output for explicitly allowed Docker containers.
- Searches dashboard sections, service checks, inventory rows, and events from a quick jump dialog.
- Can start, stop, or restart explicitly allowed systemd units and Docker containers from the dashboard.
- Records incident, recovery, deploy, backup, RSS, page-watch, and report events.
- Streams live metrics over WebSocket at `/ws/metrics`.
- Sends alerts to Discord and/or Telegram when configured.
- Serves a built-in dashboard at `/dashboard`.
- Serves built-in API documentation at `/docs/api`.
- Supports GitHub webhook deploys with HMAC signature verification.
- Sends a daily server report.
- Watches RSS feeds and page content changes, including last-check status in the dashboard.

## Local run

```bash
./gradlew run
```

Open:

```text
http://127.0.0.1:8080/dashboard
```

API reference:

```text
http://127.0.0.1:8080/docs/api
```

## Server install and update

On an Ubuntu server, install the app as a systemd service:

```bash
sudo scripts/install-systemd.sh
```

The installer builds the app, creates `/etc/personal-ops-hub/personal-ops-hub.env` if it does not exist, creates `/var/lib/personal-ops-hub`, installs the `personal-ops-hub` systemd unit, enables it, and starts it. Existing env files are preserved.

After pushing new code, update the server with:

```bash
sudo scripts/update-server.sh
```

The updater runs `git pull --ff-only`, rebuilds the distribution, restarts `personal-ops-hub`, and checks `http://127.0.0.1:8080/api/health`.

## Safe service actions

Dashboard management actions are disabled by default. Enable them only after dashboard auth is configured:

```bash
OPS_MANAGE_ENABLED=true
OPS_ALLOWED_SYSTEMD_UNITS=personal-ops-hub,caddy,nginx
OPS_RESTART_ONLY_SYSTEMD_UNITS=personal-ops-hub.service
OPS_ALLOWED_DOCKER_CONTAINERS=
```

Only targets listed in `OPS_ALLOWED_SYSTEMD_UNITS` or `OPS_ALLOWED_DOCKER_CONTAINERS` get action buttons. Set `OPS_ALLOWED_DOCKER_CONTAINERS=*` to allow actions on every Docker container visible in `docker ps --all`. `OPS_RESTART_ONLY_SYSTEMD_UNITS` can be used for units that should never be stopped from the UI. The generated systemd install env file defaults the app's own unit to restart-only.

Each management action records an event with command output plus audit fields: actor, redacted remote address, and user-agent. Remote addresses are partially masked before storage.

Systemd log viewing is read-only and uses the same `OPS_ALLOWED_SYSTEMD_UNITS` allowlist. The dashboard calls `journalctl -u <unit> -n <lines> --no-pager --output=short-iso` with a fixed command array and never accepts arbitrary commands.

Docker log viewing is read-only and uses the same `OPS_ALLOWED_DOCKER_CONTAINERS` allowlist. The dashboard calls `docker logs --tail <lines> --timestamps <container>` with a fixed command array and never accepts arbitrary commands.

## Docker run

```bash
cp .env.example .env
docker compose up -d --build
```

Use Caddy with `Caddyfile.example` as the public HTTPS reverse proxy.

## Important environment variables

- `OPS_ADMIN_PASSWORD`: enables dashboard Basic Auth.
- `OPS_ADMIN_TOKEN`: protects mutating endpoints such as manual checks and backup reports.
- `PORT`: HTTP port used by Ktor and by the default self health check.
- `OPS_RETENTION_HOURS`: metric sample retention in hours. Defaults to `24`.
- `OPS_EVENT_RETENTION_DAYS`: resolved/log event retention in days. Defaults to `90`; set `0` to disable pruning. Open and acknowledged action items are preserved.
- `OPS_DISCORD_WEBHOOK_URL`: sends Discord alerts.
- `OPS_TELEGRAM_BOT_TOKEN` and `OPS_TELEGRAM_CHAT_ID`: sends Telegram alerts.
- `OPS_HTTP_CHECKS`: `name=https://example.com/health;blog=https://example.com`.
- `OPS_TCP_CHECKS`: `ssh=127.0.0.1:22;caddy=127.0.0.1:443`.
- `OPS_DOCKER_CONTAINERS`: `caddy,postgres,personal-ops-hub`.
- `OPS_BACKUP_MARKERS`: `daily=/path/to/latest-success:1440`.
- `OPS_RSS_FEEDS`: `kotlin=https://blog.jetbrains.com/kotlin/feed/`.
- `OPS_PAGE_WATCHES`: `notice=https://example.com/notice`.
- `OPS_GITHUB_WEBHOOK_SECRET`: GitHub webhook secret.
- `OPS_DEPLOY_COMMAND`: command to run after a verified GitHub webhook.
- `OPS_MANAGE_ENABLED`: enables dashboard start/stop/restart actions.
- `OPS_ALLOWED_SYSTEMD_UNITS`: comma-separated allowlist for managed systemd units.
- `OPS_RESTART_ONLY_SYSTEMD_UNITS`: systemd units that can be restarted but not stopped from the UI.
- `OPS_ALLOWED_DOCKER_CONTAINERS`: comma-separated allowlist for managed Docker containers. Use `*` to allow every visible Docker container.

## Alerts

Configure Discord with a channel webhook URL:

```bash
export OPS_DISCORD_WEBHOOK_URL="https://discord.com/api/webhooks/..."
```

Configure Telegram with a bot token and chat id:

```bash
export OPS_TELEGRAM_BOT_TOKEN="..."
export OPS_TELEGRAM_CHAT_ID="..."
```

Alerts are sent for action-required events such as service failures, resource threshold breaches, backup failures, deploy failures, RSS fetch failures, and page-watch changes. When a later success resolves an open incident, the hub sends one recovery alert. Plain INFO log records do not trigger incident alerts. Alert delivery failures are logged and do not block event creation or dashboard APIs.

## API

- `GET /api/health`
- `GET /api/summary`
- `GET /api/metrics/current`
- `GET /api/metrics/history?since=<epoch-ms>`
- `GET /api/services`
- `POST /api/services/run`
- `GET /api/inventory`
- `GET /api/logs/systemd?unit=<unit>&lines=100`
- `GET /api/logs/docker?container=<container>&lines=100`
- `POST /api/manage/actions`
- `GET /api/events`
- `GET /api/automation`
- `POST /api/alerts/test`
- `POST /api/backups/report`
- `POST /webhook/github`
- `WS /ws/metrics`

## Backup reporting

After a backup job, call:

```bash
OPS_HUB_URL=http://127.0.0.1:8080 OPS_ADMIN_TOKEN=... \
  scripts/report-backup.sh daily success "restic backup completed"
```

For a failed backup:

```bash
OPS_HUB_URL=http://127.0.0.1:8080 OPS_ADMIN_TOKEN=... \
  scripts/report-backup.sh daily failure "restic backup failed"
```
