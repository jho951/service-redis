# Central Redis Server

이 저장소는 Spring Boot 애플리케이션이 아니라 중앙 Redis 인프라 저장소다.

## 목적

- Redis 단일 인스턴스 운영
- 공용 endpoint 제공
- Docker Compose 기반 실행
- 서비스별 key prefix, TTL, 운영 원칙 문서화

Redis는 캐시와 임시 상태 저장소로만 사용한다.
원본 데이터의 source of truth는 각 서비스가 가진다.

## 빠른 시작

```bash
./scripts/run.docker.sh up
./scripts/run.docker.sh up-monitoring
./scripts/run.docker.sh ps
./scripts/run.docker.sh logs
./scripts/run.docker.sh down
```

로컬 바이너리 실행:

```bash
./scripts/run.local.sh
```

## 구조

```text
docker/
  Dockerfile
  docker-compose.yml
  docker-entrypoint.sh
  redis.conf
scripts/
  run.docker.sh
  run.local.sh
env.dev
env.prod
env.example
docs/
  Requirement.md
  Redis-Runbook.md
  Extension.md
```

## 런타임

- image: `central-redis:v1`
- container: `central-redis`
- service dns: `redis-server`
- redis port: `6379`
- metrics port: `9121`
- shared network: `${SHARED_SERVICE_NETWORK:-service-backbone-shared}` (external)
- config: [`docker/redis.conf`](/Users/jhons/Downloads/BE/redis-server/docker/redis.conf)
- compose: [`docker/docker-compose.yml`](/Users/jhons/Downloads/BE/redis-server/docker/docker-compose.yml)

## 연결 원칙

- 모든 서비스는 동일한 Redis endpoint를 사용한다.
- 물리 분리 대신 key prefix로 논리 분리한다.
- 서비스별로 다른 Redis를 붙이지 않는다.
- TTL은 서비스 코드에서 키 저장 시점에 설정한다.

예시:

```text
redis://redis-server:6379
gateway:rate-limit:{clientKey}
auth:session:{sessionId}
permission:policy-cache:{roleId}
```

## Gateway ↔ Redis Server 계약 (현재 상태 포함)

| 항목 | 현재 Gateway 상태 | Redis Server에서 맞출 것 |
|---|---|---|
| 연결 호스트 | REDIS_HOST 기본값 redis-server | Redis 서비스 DNS를 redis-server로 제공하거나, gateway env로 동일 값 설정 |
| 포트 | REDIS_PORT 기본 6379 | Redis가 내부 네트워크에서 6379 수신 |
| 네트워크 | gateway가 external service-backbone-shared 사용 | Redis도 동일 external 네트워크에 조인 |
| 타임아웃 | REDIS_TIMEOUT_MS 기본 1000 | 네트워크 지연 고려해 1000~3000ms 범위로 운영값 조정 |
| 세션 캐시 스위치 | GATEWAY_SESSION_CACHE_ENABLED 기본 true | Redis 가용성 확보(미가용 시 cache miss 증가) |
| TTL (L2) | GATEWAY_SESSION_CACHE_TTL_SECONDS 기본 60 | 메모리 정책(eviction)과 TTL 전략 충돌 없게 설정 |
| 키 prefix | GATEWAY_SESSION_CACHE_KEY_PREFIX 기본 gateway:session: | 다른 서비스 키와 prefix 충돌 방지 |
| 캐시 키 형식 | 토큰 원문이 아닌 SHA-256 해시 키 사용 | 별도 조치 불필요, 그대로 저장 허용 |
| 인증 | 현재 gateway Redis auth 미사용 전제 | Redis를 내부망 전용으로 제한(ACL/암호 사용 시 gateway 코드 추가 필요) |
| 장애 처리 | Redis read/write 실패 시 gateway는 fail-open으로 인증 플로우 계속 | Redis 장애 알람/모니터링 구성 필수 |
| 관측 | gateway는 cache outcome(SESSION_CACHE_L1/L2) 기록 | Redis 모니터링(연결 수, 메모리, hit rate) 제공 |
| 운영 모드 | 도커 compose에서 외부 Redis 연결 구조 | Redis를 별도 스택으로 운영하고 shared network만 공유 |

체크리스트:

1. redis-server 컨테이너/서비스가 service-backbone-shared에 붙어있는지
2. gateway 컨테이너에서 redis-server:6379 TCP 접속 가능한지
3. GATEWAY_SESSION_CACHE_ENABLED=true 상태에서 인증 요청 시 SESSION_CACHE_L2가 로그에 찍히는지

## 문서

- 현재 구조와 정책: [`docs/Requirement.md`](/Users/jhons/Downloads/BE/redis-server/docs/Requirement.md)
- 운영 절차: [`docs/Redis-Runbook.md`](/Users/jhons/Downloads/BE/redis-server/docs/Redis-Runbook.md)
- 향후 확장: [`docs/Extension.md`](/Users/jhons/Downloads/BE/redis-server/docs/Extension.md)
- 축약 안내:
  [`docs/Design.md`](/Users/jhons/Downloads/BE/redis-server/docs/Design.md),
  [`docs/Redis-MSA-Integration.md`](/Users/jhons/Downloads/BE/redis-server/docs/Redis-MSA-Integration.md)
