#!/bin/sh
set -eu

BASE_DIR="$(CDPATH= cd -- "$(dirname "$0")" && pwd)"
ROOT_DIR="$(CDPATH= cd -- "$BASE_DIR/.." && pwd)"
ENV_FILE="${ENV_FILE:-$ROOT_DIR/env.local}"
REDIS_CONFIG_FILE="${REDIS_CONFIG_FILE:-$ROOT_DIR/local/redis.conf}"

if ! command -v redis-server >/dev/null 2>&1; then
  echo "redis-server is required for local execution." >&2
  exit 1
fi

if [ -f "$ENV_FILE" ]; then
  set -a
  . "$ENV_FILE"
  set +a
fi

case "$REDIS_CONFIG_FILE" in
  /*) ;;
  *) REDIS_CONFIG_FILE="$ROOT_DIR/$REDIS_CONFIG_FILE" ;;
esac

REDIS_PORT="${REDIS_PORT:-6379}"
REDIS_MAXMEMORY="${REDIS_MAXMEMORY:-256mb}"
REDIS_MAXMEMORY_POLICY="${REDIS_MAXMEMORY_POLICY:-allkeys-lru}"
REDIS_DATA_DIR="${REDIS_DATA_DIR:-build/redis/local/data}"
REDIS_LOG_DIR="${REDIS_LOG_DIR:-build/redis/local/logs}"

case "$REDIS_DATA_DIR" in
  /*) DATA_DIR="$REDIS_DATA_DIR" ;;
  *) DATA_DIR="$ROOT_DIR/$REDIS_DATA_DIR" ;;
esac

case "$REDIS_LOG_DIR" in
  /*) LOG_DIR="$REDIS_LOG_DIR" ;;
  *) LOG_DIR="$ROOT_DIR/$REDIS_LOG_DIR" ;;
esac

mkdir -p "$DATA_DIR" "$LOG_DIR"

set -- \
  redis-server "$REDIS_CONFIG_FILE" \
  --port "$REDIS_PORT" \
  --dir "$DATA_DIR" \
  --logfile "$LOG_DIR/redis-server.log" \
  --maxmemory "$REDIS_MAXMEMORY" \
  --maxmemory-policy "$REDIS_MAXMEMORY_POLICY"

if [ -n "${REDIS_PASSWORD:-}" ]; then
  set -- "$@" --requirepass "$REDIS_PASSWORD"
fi

exec "$@"
