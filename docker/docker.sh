set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="${SCRIPT_DIR}/docker-compose.yml"

case "$1" in
  all)
    echo " 이미지 빌드 및 컨테이너 가동"
    docker compose -f "$COMPOSE_FILE" build --no-cache
    docker compose -f "$COMPOSE_FILE" up -d
    echo "✅ 컨테이너 기동 완료, 로그 출력 중"
    docker logs drawing-app --tail=200 -f
    ;;
  build)
    echo "🔧 이미지 빌드 중..."
    docker compose -f "$COMPOSE_FILE" build --no-cache
    ;;
  up)
    echo "⚡️ 컨테이너 기동 중..."
    docker compose -f "$COMPOSE_FILE" up -d
    ;;
  down)
    echo "🔴 컨테이너 중지 중..."
    docker compose -f "$COMPOSE_FILE" down
    ;;
  logs)
    echo "📜 로그 출력 중..."
    docker compose -f "$COMPOSE_FILE" logs -f
    ;;
  restart)
    echo "🔄 컨테이너 재시작 중..."
    docker compose -f "$COMPOSE_FILE" down
    docker compose -f "$COMPOSE_FILE" build --no-cache
    docker compose -f "$COMPOSE_FILE" up -d
    ;;
  nuke)
    echo "💥 컨테이너 및 볼륨 삭제 중..."
    docker compose -f "$COMPOSE_FILE" down -v
    docker image prune -f
    ;;
  *)
    echo "Usage: $0 {all|build|up|down|logs|restart|nuke}"
    exit 1
    ;;
esac
