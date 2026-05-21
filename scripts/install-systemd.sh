#!/usr/bin/env bash
set -euo pipefail

SERVICE_NAME="${OPS_SERVICE_NAME:-personal-ops-hub}"
RUN_USER="${OPS_RUN_USER:-root}"
ENV_DIR="${OPS_ENV_DIR:-/etc/personal-ops-hub}"
ENV_FILE="${OPS_ENV_FILE:-$ENV_DIR/personal-ops-hub.env}"
DATA_DIR="${OPS_DATA_DIR:-/var/lib/personal-ops-hub}"
PORT="${PORT:-8080}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="${OPS_PROJECT_DIR:-$(cd "$SCRIPT_DIR/.." && pwd)}"
UNIT_FILE="/etc/systemd/system/$SERVICE_NAME.service"
APP_BIN="$PROJECT_DIR/build/install/personal-ops-hub/bin/personal-ops-hub"
HEALTH_URL="${OPS_HEALTH_URL:-http://127.0.0.1:$PORT/api/health}"

require_root() {
  if [[ "$(id -u)" -ne 0 ]]; then
    echo "error: run this script as root" >&2
    exit 1
  fi
}

require_systemd() {
  if ! command -v systemctl >/dev/null 2>&1; then
    echo "error: systemctl is required" >&2
    exit 1
  fi
}

random_secret() {
  if command -v openssl >/dev/null 2>&1; then
    openssl rand -base64 24 | tr -d '\n'
  else
    od -An -N24 -tx1 /dev/urandom | tr -d ' \n'
  fi
}

install_runtime_deps_if_needed() {
  if command -v java >/dev/null 2>&1 &&
    java -version 2>&1 | grep -q 'version "21' &&
    command -v curl >/dev/null 2>&1; then
    return
  fi
  if command -v apt-get >/dev/null 2>&1; then
    echo "Installing runtime dependencies..."
    apt-get update
    apt-get install -y openjdk-21-jdk curl
  else
    echo "error: Java 21 and curl are required, and apt-get is unavailable" >&2
    exit 1
  fi
}

write_env_file_if_missing() {
  mkdir -p "$ENV_DIR" "$DATA_DIR"
  chmod 700 "$ENV_DIR"

  if [[ -f "$ENV_FILE" ]]; then
    echo "Keeping existing environment file: $ENV_FILE"
    return
  fi

  local admin_password
  local admin_token
  admin_password="$(random_secret)"
  admin_token="$(random_secret)"

  cat >"$ENV_FILE" <<EOF
OPS_APP_NAME="Personal Ops Hub"
PORT=$PORT
OPS_DB_PATH=$DATA_DIR/ops-hub.db
OPS_COLLECTION_INTERVAL_SECONDS=30
OPS_RETENTION_HOURS=24

OPS_ADMIN_USER=admin
OPS_ADMIN_PASSWORD=$admin_password
OPS_ADMIN_TOKEN=$admin_token

OPS_TIME_ZONE=Asia/Seoul
OPS_DISK_PATHS=/
OPS_HTTP_CHECKS=ops-hub=http://127.0.0.1:$PORT/api/health

OPS_MANAGE_ENABLED=false
OPS_ALLOWED_SYSTEMD_UNITS=$SERVICE_NAME.service
OPS_RESTART_ONLY_SYSTEMD_UNITS=$SERVICE_NAME.service
# Use "*" to allow actions on every Docker container visible in docker ps --all.
OPS_ALLOWED_DOCKER_CONTAINERS=
EOF
  chmod 600 "$ENV_FILE"

  echo "Created environment file: $ENV_FILE"
  echo "Initial dashboard login:"
  echo "  user: admin"
  echo "  password: $admin_password"
}

build_app() {
  cd "$PROJECT_DIR"
  ./gradlew installDist
}

write_unit_file() {
  cat >"$UNIT_FILE" <<EOF
[Unit]
Description=Personal Ops Hub
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
WorkingDirectory=$PROJECT_DIR
EnvironmentFile=$ENV_FILE
ExecStart=$APP_BIN
Restart=always
RestartSec=5
User=$RUN_USER

[Install]
WantedBy=multi-user.target
EOF
}

wait_for_health() {
  for _ in {1..20}; do
    if curl -fsS "$HEALTH_URL" >/dev/null 2>&1; then
      return
    fi
    sleep 1
  done
  echo "warning: health check did not pass yet: $HEALTH_URL" >&2
}

require_root
require_systemd
install_runtime_deps_if_needed
write_env_file_if_missing
build_app
write_unit_file

systemctl daemon-reload
systemctl enable --now "$SERVICE_NAME"
systemctl restart "$SERVICE_NAME"
wait_for_health

echo "Installed $SERVICE_NAME"
echo "Dashboard: http://127.0.0.1:$PORT/dashboard"
echo "Status: systemctl status $SERVICE_NAME --no-pager"
