#!/usr/bin/env bash
set -euo pipefail

SERVICE_NAME="${OPS_SERVICE_NAME:-personal-ops-hub}"
ENV_FILE="${OPS_ENV_FILE:-/etc/personal-ops-hub/personal-ops-hub.env}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="${OPS_PROJECT_DIR:-$(cd "$SCRIPT_DIR/.." && pwd)}"

load_env_file() {
  if [[ -f "$ENV_FILE" ]]; then
    set -a
    # shellcheck disable=SC1090
    . "$ENV_FILE"
    set +a
  fi
}

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
  if ! command -v curl >/dev/null 2>&1; then
    echo "error: curl is required" >&2
    exit 1
  fi
}

ensure_clean_worktree() {
  if [[ "${OPS_UPDATE_ALLOW_DIRTY:-false}" == "true" ]]; then
    return
  fi
  if ! git diff --quiet || ! git diff --cached --quiet; then
    echo "error: working tree has local changes; commit/stash them or set OPS_UPDATE_ALLOW_DIRTY=true" >&2
    git status --short
    exit 1
  fi
}

pull_latest_if_possible() {
  if ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
    echo "Skipping git pull: not a git worktree"
    return
  fi
  if ! git remote get-url origin >/dev/null 2>&1; then
    echo "Skipping git pull: origin remote is not configured"
    return
  fi
  ensure_clean_worktree
  git pull --ff-only
}

build_app() {
  ./gradlew installDist
}

restart_service() {
  systemctl daemon-reload
  systemctl restart "$SERVICE_NAME"
}

wait_for_health() {
  local port="${PORT:-8080}"
  local health_url="${OPS_HEALTH_URL:-http://127.0.0.1:$port/api/health}"
  for _ in {1..20}; do
    if curl -fsS "$health_url" >/dev/null 2>&1; then
      echo "Health check passed: $health_url"
      return
    fi
    sleep 1
  done
  echo "warning: health check did not pass yet: $health_url" >&2
}

require_root
require_systemd
load_env_file
cd "$PROJECT_DIR"
pull_latest_if_possible
build_app
restart_service
wait_for_health

echo "Updated $SERVICE_NAME"
echo "Commit: $(git rev-parse --short HEAD 2>/dev/null || echo unknown)"
echo "Status: systemctl status $SERVICE_NAME --no-pager"
