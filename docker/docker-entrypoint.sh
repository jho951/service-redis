#!/bin/sh
set -eu

: "${REDIS_PASSWORD:=local-dev-redis-password}"
: "${REDIS_MAXMEMORY:=256mb}"
: "${REDIS_MAXMEMORY_POLICY:=allkeys-lru}"

mkdir -p /data /var/log/redis

exec redis-server /usr/local/etc/redis/redis.conf \
  --requirepass "${REDIS_PASSWORD}" \
  --maxmemory "${REDIS_MAXMEMORY}" \
  --maxmemory-policy "${REDIS_MAXMEMORY_POLICY}"
