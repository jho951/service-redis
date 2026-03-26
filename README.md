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
- redis port: `6379`
- metrics port: `9121`
- config: [`docker/redis.conf`](/Users/jhons/Downloads/BE/redis-server/docker/redis.conf)
- compose: [`docker/docker-compose.yml`](/Users/jhons/Downloads/BE/redis-server/docker/docker-compose.yml)

## 연결 원칙

- 모든 서비스는 동일한 Redis endpoint를 사용한다.
- 물리 분리 대신 key prefix로 논리 분리한다.
- 서비스별로 다른 Redis를 붙이지 않는다.
- TTL은 서비스 코드에서 키 저장 시점에 설정한다.

예시:

```text
redis://:password@central-redis:6379
gateway:rate-limit:{clientKey}
auth:session:{sessionId}
permission:policy-cache:{roleId}
```

## 문서

- 현재 구조와 정책: [`docs/Requirement.md`](/Users/jhons/Downloads/BE/redis-server/docs/Requirement.md)
- 운영 절차: [`docs/Redis-Runbook.md`](/Users/jhons/Downloads/BE/redis-server/docs/Redis-Runbook.md)
- 향후 확장: [`docs/Extension.md`](/Users/jhons/Downloads/BE/redis-server/docs/Extension.md)
- 축약 안내:
  [`docs/Design.md`](/Users/jhons/Downloads/BE/redis-server/docs/Design.md),
  [`docs/Redis-MSA-Integration.md`](/Users/jhons/Downloads/BE/redis-server/docs/Redis-MSA-Integration.md)
