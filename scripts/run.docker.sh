#!/bin/sh
set -eu

BASE_DIR="$(CDPATH= cd -- "$(dirname "$0")" && pwd)"
ROOT_DIR="$(CDPATH= cd -- "$BASE_DIR/.." && pwd)"
COMPOSE_PROJECT_NAME="redis-server"
COMMAND="${1:-up}"
TARGET_ENV="${2:-${DOCKER_ENV:-dev}}"

case "$TARGET_ENV" in
  dev|local)
    TARGET_ENV="dev"
    COMPOSE_FILE="$ROOT_DIR/docker/dev/compose.yml"
    ENV_FILE="${ENV_FILE:-$ROOT_DIR/env.docker.dev}"
    ;;
  prod|production)
    TARGET_ENV="prod"
    COMPOSE_FILE="$ROOT_DIR/docker/prod/compose.yml"
    ENV_FILE="${ENV_FILE:-$ROOT_DIR/env.docker.prod}"
    ;;
  *)
    echo "Unknown Docker environment: $TARGET_ENV" >&2
    echo "Usage: ./scripts/run.docker.sh [up|up-monitoring|down|restart|logs|logs-monitoring|ps|build] [dev|prod]" >&2
    exit 1
    ;;
esac

SHARED_NETWORK="${SHARED_SERVICE_NETWORK:-${SERVICE_SHARED_NETWORK:-service-backbone-shared}}"
if ! docker network inspect "$SHARED_NETWORK" >/dev/null 2>&1; then
  echo "Creating external docker network: $SHARED_NETWORK"
  docker network create "$SHARED_NETWORK" >/dev/null
fi

compose() {
  SHARED_SERVICE_NETWORK="$SHARED_NETWORK" SERVICE_SHARED_NETWORK="$SHARED_NETWORK" \
    docker compose --env-file "$ENV_FILE" -p "$COMPOSE_PROJECT_NAME" -f "$COMPOSE_FILE" "$@"
}

case "$COMMAND" in
  up) compose up -d --build ;;
  up-monitoring) compose --profile monitoring up -d --build ;;
  down) compose down --remove-orphans ;;
  restart) compose down --remove-orphans && compose up -d --build ;;
  logs) compose logs -f redis-server ;;
  logs-monitoring) compose logs -f redis-exporter ;;
  ps) compose ps ;;
  build) compose build ;;
  *)
    echo "Usage: ./scripts/run.docker.sh [up|up-monitoring|down|restart|logs|logs-monitoring|ps|build] [dev|prod]" >&2
    echo "Environment: set ENV_FILE=/path/to/env.file to override env.docker.dev/env.docker.prod." >&2
    exit 1
    ;;
esac
