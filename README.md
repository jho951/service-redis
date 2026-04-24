# Central Redis Server

이 저장소는 Spring Boot 애플리케이션이 아니라 중앙 Redis 인프라 저장소다.

## Contract Source

- 공통 계약 레포: `https://github.com/jho951/service-contract`
- 계약 동기화 기준 파일: [contract.lock.yml](contract.lock.yml)
- 계약 변경 절차: [contract-change-workflow.md](docs/contract-change-workflow.md)
- 이 서비스의 코드 SoT: `redis-service` `main`
- PR에서는 `.github/workflows/contract-check.yml`이 lock 파일과 계약 영향 변경 여부를 검사합니다.
- 인터페이스 변경 시 본 저장소 구현보다 계약 레포 변경을 먼저 반영합니다.

## 목적

- Redis 단일 인스턴스 운영
- 공용 endpoint 제공
- Docker Compose 기반 실행
- 서비스별 key prefix, TTL, 운영 원칙 문서화

Redis는 캐시와 임시 상태 저장소로만 사용한다.
원본 데이터의 source of truth는 각 서비스가 가진다.

## 빠른 시작

IntelliJ 또는 로컬 바이너리 실행:

```bash
./scripts/run.local.sh
```

Docker dev 실행:

```bash
./scripts/run.docker.sh up
./scripts/run.docker.sh up-monitoring
./scripts/run.docker.sh ps
./scripts/run.docker.sh logs
./scripts/run.docker.sh down
```

Docker 배포용 설정 실행:

```bash
./scripts/run.docker.sh up prod
./scripts/run.docker.sh up-monitoring prod
./scripts/run.docker.sh logs prod
./scripts/run.docker.sh down prod
```

## 구조

```text
docker/
  Dockerfile
  docker-compose.yml
  dev/
    docker-compose.yml
    redis.conf
  prod/
    docker-compose.yml
    redis.conf
  docker-entrypoint.sh
  redis.conf
local/
  redis.conf
scripts/
  run.docker.sh
  run.local.sh
env.local
env.docker.dev
env.docker.prod
env.example
docs/
  Requirement.md
  Redis-Runbook.md
  Extension.md
```

## 런타임

- local binary env: `env.local`
- docker dev env: `env.docker.dev`
- docker prod env: `env.docker.prod`
- image: dev=`central-redis:dev`, prod=`${REDIS_IMAGE}`
- container: `${REDIS_CONTAINER_NAME:-central-redis-dev}`
- service dns: `redis-server` (같은 Docker host 내부 compose 기준)
- container alias: `central-redis` (같은 Docker host 내부 compose 기준)
- redis port: `6379`
- metrics port: `9121`
- shared network: `${SHARED_SERVICE_NETWORK:-service-backbone-shared}` (external)
- local binary config: [`local/redis.conf`](local/redis.conf)
- docker fallback config: [`docker/redis.conf`](docker/redis.conf)
- dev redis config: [`docker/dev/redis.conf`](docker/dev/redis.conf)
- prod redis config: [`docker/prod/redis.conf`](docker/prod/redis.conf)
- common compose: [`docker/docker-compose.yml`](docker/docker-compose.yml)
- dev compose: [`docker/dev/docker-compose.yml`](docker/dev/docker-compose.yml)
- prod compose: [`docker/prod/docker-compose.yml`](docker/prod/docker-compose.yml)

## 실행 환경 분리

| 실행 방식 | 기본 env 파일 | 용도 |
|---|---|---|
| IntelliJ/로컬 redis-server | `env.local` | 로컬에 설치된 `redis-server`를 직접 실행 |
| Docker dev | `env.docker.dev` + `docker/dev/docker-compose.yml` | 개발 PC의 Docker Compose 실행 |
| Docker prod | `env.docker.prod` + `docker/prod/docker-compose.yml` | 배포 서버의 Docker Compose 실행 |

`run.local.sh`는 기본으로 `env.local`을 읽는다.
IntelliJ External Tool이나 Run Configuration에서 실행 파일을 `scripts/run.local.sh`로 지정하면 같은 설정으로 실행된다.

`run.docker.sh`는 두 번째 인자로 Docker 환경을 고른다.
공통 설정은 `docker-compose.yml`에 두고, dev/prod 차이는 각각 `docker/dev/docker-compose.yml`, `docker/prod/docker-compose.yml`에서 덮어쓴다.
Redis 설정도 `docker/dev/redis.conf`, `docker/prod/redis.conf`로 분리한다.

여러 서비스를 같은 Docker host에 두면 `service-backbone-shared`와 `central-redis` alias를 그대로 쓸 수 있다.
서비스를 EC2별로 분리하면 이 alias는 호스트 간에 전파되지 않으므로, 각 애플리케이션은 Redis EC2 private DNS/IP 또는 관리형 Redis endpoint를 직접 사용해야 한다.

```bash
./scripts/run.docker.sh up dev
./scripts/run.docker.sh up prod
```

별도 파일을 쓰려면 기존처럼 `ENV_FILE`을 지정한다.

```bash
ENV_FILE=/path/to/env.file ./scripts/run.docker.sh up
ENV_FILE=/path/to/env.file ./scripts/run.local.sh
```

운영 배포에서는 `REDIS_IMAGE=<account>.dkr.ecr.<region>.amazonaws.com/<env>-redis-service:<sha>`를 주입하고 `docker compose pull && up -d`로 반영한다. exporter는 `REDIS_EXPORTER_IMAGE`로 별도 제어한다.

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
| 연결 호스트 | single-host compose에서는 `redis-server` 또는 `central-redis`, EC2 분산 배포에서는 Redis private DNS/IP를 사용 | Redis 호스트가 같은 Docker host가 아니면 compose alias 대신 VPC 내부 endpoint를 사용 |
| 포트 | REDIS_PORT 기본 6379 | Redis가 내부 네트워크에서 6379 수신 |
| 네트워크 | single-host compose에서는 external service-backbone-shared, EC2 분산 배포에서는 VPC 내부 네트워크 사용 | 같은 호스트가 아니면 external Docker network 대신 Security Group과 private routing으로 접근 |
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

EC2 분산 배포 체크:

1. Redis 포트 `6379`와 exporter 포트 `9121`은 public 인터넷이 아니라 허용된 VPC CIDR 또는 monitoring/gateway SG에서만 접근 가능한지
2. gateway, auth-service, authz-service의 `REDIS_HOST`가 compose alias가 아니라 Redis private DNS/IP 또는 관리형 endpoint인지
3. monitoring-service Prometheus target이 Redis exporter private endpoint를 수집하는지

## 문서

- 현재 구조와 정책: [`docs/Requirement.md`](docs/Requirement.md)
- 운영 절차: [`docs/Redis-Runbook.md`](docs/Redis-Runbook.md)
- 향후 확장: [`docs/Extension.md`](docs/Extension.md)
- 축약 안내:
  [`docs/Design.md`](docs/Design.md),
  [`docs/Redis-MSA-Integration.md`](docs/Redis-MSA-Integration.md)
