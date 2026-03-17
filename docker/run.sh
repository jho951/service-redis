#!/bin/sh
set -eu

BASE_DIR="$(CDPATH= cd -- "$(dirname "$0")" && pwd)"
ROOT_DIR="$(CDPATH= cd -- "$BASE_DIR/.." && pwd)"
COMPOSE_FILE="$BASE_DIR/docker-compose.yml"
ENV_FILE="$ROOT_DIR/env/.env.dev"

COMMAND="${1:-up}"

case "$COMMAND" in
  up)
    docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d --build
    ;;
  up-monitoring)
    docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" --profile monitoring up -d --build
    ;;
  down)
    docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" down
    ;;
  restart)
    docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" down
    docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d --build
    ;;
  logs)
    docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" logs -f redis
    ;;
  logs-monitoring)
    docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" logs -f redis-exporter
    ;;
  ps)
    docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" ps
    ;;
  *)
    echo "Usage: ./docker/run.sh [up|up-monitoring|down|restart|logs|logs-monitoring|ps]" >&2
    exit 1
    ;;
esac
