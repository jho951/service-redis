#!/bin/sh
set -eu

BASE_DIR="$(CDPATH= cd -- "$(dirname "$0")" && pwd)"
ROOT_DIR="$(CDPATH= cd -- "$BASE_DIR/.." && pwd)"
COMPOSE_FILE="$ROOT_DIR/docker/docker-compose.yml"

COMMAND="${1:-up}"
TARGET_ENV="${2:-${DOCKER_ENV:-dev}}"

case "$TARGET_ENV" in
  dev|local)
    COMPOSE_OVERRIDE_FILE="$ROOT_DIR/docker/dev/docker-compose.yml"
    if [ -z "${ENV_FILE:-}" ]; then
      ENV_FILE="$ROOT_DIR/env.docker.dev"
    fi
    ;;
  prod|production)
    COMPOSE_OVERRIDE_FILE="$ROOT_DIR/docker/prod/docker-compose.yml"
    if [ -z "${ENV_FILE:-}" ]; then
      ENV_FILE="$ROOT_DIR/env.docker.prod"
    fi
    ;;
  *)
    echo "Unknown Docker environment: $TARGET_ENV" >&2
    echo "Use dev or prod, or set ENV_FILE=/path/to/env.file." >&2
    exit 1
    ;;
esac

compose() {
  docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" -f "$COMPOSE_OVERRIDE_FILE" "$@"
}

case "$COMMAND" in
  up)
    compose up -d --build
    ;;
  up-monitoring)
    compose --profile monitoring up -d --build
    ;;
  down)
    compose down
    ;;
  restart)
    compose down
    compose up -d --build
    ;;
  logs)
    compose logs -f redis-server
    ;;
  logs-monitoring)
    compose logs -f redis-exporter
    ;;
  ps)
    compose ps
    ;;
  *)
    echo "Usage: ./scripts/run.docker.sh [up|up-monitoring|down|restart|logs|logs-monitoring|ps] [dev|prod]" >&2
    echo "Environment: set ENV_FILE=/path/to/env.file to override env.docker.dev/env.docker.prod." >&2
    exit 1
    ;;
esac
