#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 2 ]]; then
  echo "usage: $0 <name> <success|failure> [message]" >&2
  exit 2
fi

name="$1"
state="$2"
message="${3:-}"
hub_url="${OPS_HUB_URL:-http://127.0.0.1:8080}"
token="${OPS_ADMIN_TOKEN:-}"

json_escape() {
  printf '%s' "$1" | sed \
    -e 's/\\/\\\\/g' \
    -e 's/"/\\"/g' \
    -e ':a;N;$!ba;s/\n/\\n/g'
}

if [[ "$state" == "success" ]]; then
  success=true
else
  success=false
fi

curl_args=(
  -fsS
  -X POST
  "$hub_url/api/backups/report"
  -H "Content-Type: application/json"
  -d "{\"name\":\"$(json_escape "$name")\",\"success\":$success,\"message\":\"$(json_escape "$message")\"}"
)

if [[ -n "$token" ]]; then
  curl_args+=(-H "X-Ops-Token: $token")
fi

curl "${curl_args[@]}"
