# personal-ops-hub

A personal server monitoring and automation hub built with Ktor.

## What it does

- Shows CPU, memory, disk, uptime, load average, and JVM status.
- Stores recent metrics in SQLite for a one-hour dashboard graph.
- Checks HTTP endpoints, TCP ports, Docker containers, and backup markers.
- Shows a read-only server inventory for cron, systemd timers, services, Docker, and listening ports.
- Records incident, recovery, deploy, backup, RSS, page-watch, and report events.
- Streams live metrics over WebSocket at `/ws/metrics`.
- Sends alerts to Discord and/or Telegram when configured.
- Serves a built-in dashboard at `/dashboard`.
- Supports GitHub webhook deploys with HMAC signature verification.
- Sends a daily server report.
- Watches RSS feeds and page content changes.

## Local run

```bash
./gradlew run
```

Open:

```text
http://127.0.0.1:8080/dashboard
```

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
