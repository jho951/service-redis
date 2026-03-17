# Central Redis Server

이 저장소는 Spring Boot 애플리케이션이 아니라 중앙 Redis 인프라 저장소다.

## What This Repository Is

- Redis 1대 운영
- 단일 endpoint 제공
- Docker 기반 실행
- 서비스별 key prefix 분리 정책 문서화

따라서 `gradle build`가 동작하지 않는 것은 정상이다.
이 저장소는 Gradle 프로젝트가 아니다.

## Quick Start

Redis 서버 시작:

```bash
./docker/run.sh up
```

상태 확인:

```bash
./docker/run.sh ps
```

로그 확인:

```bash
./docker/run.sh logs
```

중지:

```bash
./docker/run.sh down
```

재시작:

```bash
./docker/run.sh restart
```

## Structure

```text
docker/
  Dockerfile
  docker-compose.yml
  docker-entrypoint.sh
  redis.conf
  run.sh
env/
  .env.dev
  .env.prod.example
docs/
  Requirement.md
  Redis-Infrastructure-Structure.md
  Redis-Runbook.md
```

## Runtime

- image: `central-redis:v1`
- container: `central-redis`
- default port: `6379`
- config: [`docker/redis.conf`](/Users/jhons/Downloads/BE/redis-server/docker/redis.conf)
- compose: [`docker/docker-compose.yml`](/Users/jhons/Downloads/BE/redis-server/docker/docker-compose.yml)

## Environment

개발 환경 변수:

- [`env/.env.dev`](/Users/jhons/Downloads/BE/redis-server/env/.env.dev)

운영 예시:

- [`env/.env.prod.example`](/Users/jhons/Downloads/BE/redis-server/env/.env.prod.example)

주요 변수:

- `REDIS_PORT`
- `REDIS_PASSWORD`
- `REDIS_MAXMEMORY`
- `REDIS_MAXMEMORY_POLICY`

## Connection

서비스들은 같은 endpoint를 사용한다.

예시:

```text
redis://:local-dev-redis-password@localhost:6379
```

논리 분리는 key prefix로 수행한다.

예시:

- `gateway:rate-limit:*`
- `auth:session:*`
- `permission:policy-cache:*`

## Documents

- 구조 문서: [`docs/Redis-Infrastructure-Structure.md`](/Users/jhons/Downloads/BE/redis-server/docs/Redis-Infrastructure-Structure.md)
- 운영 문서: [`docs/Redis-Runbook.md`](/Users/jhons/Downloads/BE/redis-server/docs/Redis-Runbook.md)
- V1 정책 문서: [`docs/Requirement.md`](/Users/jhons/Downloads/BE/redis-server/docs/Requirement.md)
