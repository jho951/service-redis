# Redis Runbook

이 문서는 중앙 Redis V1 운영 절차를 정리한다.

## Runtime

- Redis 버전: `redis:7.4-alpine`
- 실행 방식: Docker Compose
- 인스턴스 수: 1
- endpoint: `redis://central-redis:6379`

## Start

개발 환경 실행:

```bash
./docker/run.sh up
```

중지:

```bash
./docker/run.sh down
```

상태 확인:

```bash
./docker/run.sh ps
./docker/run.sh logs
```

## Health Check

컨테이너 내부 ping:

```bash
docker exec central-redis redis-cli -a "$REDIS_PASSWORD" ping
```

정상 응답:

```text
PONG
```

## Storage

볼륨 경로:

- `./docker/data`
- `./docker/logs`

Persistence:

- RDB 사용
- AOF 사용

## Connection Policy

모든 서비스는 같은 endpoint를 사용한다.

예시:

- `redis://:password@central-redis:6379`

서비스별 논리 분리는 key prefix로 처리한다.

## Namespace Policy

예시:

- `gateway:rate-limit:*`
- `gateway:auth-cache:*`
- `auth:session:*`
- `auth:refresh-token:*`
- `auth:oauth-state:*`
- `permission:policy-cache:*`

## TTL Baseline

- `gateway:rate-limit:*` : 1분 ~ 5분
- `auth:oauth-state:*` : 5분
- `auth:session:*` : 30분 ~ 24시간
- `permission:policy-cache:*` : 5분 ~ 30분

## Operational Rules

- Redis를 source of truth로 사용하지 않는다.
- 서비스별 prefix 없이 key를 저장하지 않는다.
- TTL 없는 key는 특별한 사유 없이는 만들지 않는다.
- 운영 비밀번호는 `env/.env.prod.example`에 직접 저장하지 않는다.

## Backup

V1에서는 다음을 기본으로 한다.

- AOF 유지
- RDB 스냅샷 유지
- 데이터 디렉터리 정기 백업

## Recovery

1. 기존 컨테이너 중지
2. `docker/data` 보존 여부 확인
3. 필요 시 백업 데이터 복원
4. 컨테이너 재기동
5. `PING`, key 조회, 서비스 연결 확인

## Next Stage

V2에서 검토할 항목:

- primary + replica
- 장애 조치
- 모니터링 고도화
- 백업 자동화
