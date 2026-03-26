#!/bin/sh
set -eu

REDIS_CONFIG_FILE="${REDIS_CONFIG_FILE:-/usr/local/etc/redis/redis.conf}"
REDIS_MAXMEMORY="${REDIS_MAXMEMORY:-256mb}"
REDIS_MAXMEMORY_POLICY="${REDIS_MAXMEMORY_POLICY:-allkeys-lru}"

set -- \
  redis-server "$REDIS_CONFIG_FILE" \
  --maxmemory "$REDIS_MAXMEMORY" \
  --maxmemory-policy "$REDIS_MAXMEMORY_POLICY"

if [ -n "${REDIS_PASSWORD:-}" ]; then
  set -- "$@" --requirepass "$REDIS_PASSWORD"
fi

exec "$@"
