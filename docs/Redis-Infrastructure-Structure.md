# Redis Infrastructure Structure

이 문서는 현재 저장소를 Spring Boot 애플리케이션이 아니라 중앙 Redis 인프라 프로젝트로 운영할 때의 구조를 정의한다.

## Goal

이 프로젝트의 목적은 애플리케이션을 실행하는 것이 아니라,
Gateway, Auth, Permission 등 여러 서비스가 공통으로 사용하는 중앙 Redis 서버를 구성하고 운영하는 것이다.

따라서 이 저장소는 다음 역할을 가진다.

- Redis 1대 실행
- 단일 endpoint 제공
- 환경별 실행 설정 관리
- 운영 문서와 정책 관리

## Why Spring Boot Is Not Needed

중앙 Redis 서버는 Spring Boot 애플리케이션이 아니다.

`SpringBootApplication`은 API 서버나 내부 서비스가 있을 때 필요하다.
하지만 이 저장소의 역할이 Redis 인프라 자체라면 다음 항목은 핵심이 아니다.

- Controller
- Service
- Repository
- Spring Boot main class
- Gradle 기반 애플리케이션 빌드

즉, 중앙 Redis 서버 저장소라면 `RedisApplication` 같은 엔트리포인트는 제거하는 것이 맞다.

## Project Role

이 저장소는 다음 중 첫 번째 역할에 해당한다.

1. Redis 인프라 프로젝트
2. Redis를 사용하는 애플리케이션 프로젝트

현재 목표는 1번이다.

즉:

- Redis 자체를 실행하고
- 설정 파일을 관리하고
- 운영 규칙을 문서화하고
- 서비스들이 공통 endpoint로 접근하게 만드는 저장소다

## Recommended Structure

권장 디렉터리 구조는 아래와 같다.

```text
redis-server/
  docker-compose.yml
  redis/
    redis.conf
  env/
    .env.dev
    .env.prod.example
  docs/
    Requirement.md
    Redis-V1-Design.md
    Redis-Infrastructure-Structure.md
    Redis-Runbook.md
```

## Directory Responsibilities

### `docker-compose.yml`

역할:

- Redis 컨테이너 실행
- 포트 바인딩
- 환경 변수 연결
- 볼륨 연결
- `redis.conf` 마운트

V1에서는 Redis 단일 인스턴스만 정의한다.

### `redis/redis.conf`

역할:

- Redis 서버 설정 관리
- 메모리 정책 설정
- persistence 정책 설정
- 보안 관련 기본 설정
- 로그 정책 설정

예:

- `requirepass`
- `appendonly`
- `maxmemory`
- `maxmemory-policy`
- `bind`
- `protected-mode`

### `env/.env.dev`

역할:

- 개발 환경 실행 변수 정의

예:

- `REDIS_PORT`
- `REDIS_PASSWORD`
- `REDIS_MAXMEMORY`

### `env/.env.prod.example`

역할:

- 운영 환경 변수 예시 제공
- 실제 운영 값은 별도 비밀 저장소에서 관리

### `docs/`

역할:

- 키 prefix 규칙 문서화
- TTL 정책 문서화
- 장애 대응 절차 문서화
- 백업/복구 규칙 문서화
- 서비스 연결 규칙 문서화

## Redis Connection Model

모든 서비스는 동일한 Redis endpoint에 연결한다.

예:

```text
redis://central-redis:6379
```

또는 비밀번호 사용 시:

```text
redis://:password@central-redis:6379
```

규칙:

- Gateway는 같은 endpoint 사용
- Auth는 같은 endpoint 사용
- Permission은 같은 endpoint 사용
- User 계열 서비스도 같은 endpoint 사용 가능

논리 분리는 prefix로 수행한다.

## Namespace Separation

중앙 Redis를 공유하더라도 서비스 책임은 key prefix로 분리한다.

예시:

- `gateway:rate-limit:*`
- `gateway:auth-cache:*`
- `auth:session:*`
- `auth:refresh-token:*`
- `auth:oauth-state:*`
- `permission:policy-cache:*`

원칙:

- 서비스별 prefix는 필수
- 같은 Redis를 써도 데이터는 섞지 않음
- source of truth는 Redis가 아니라 각 서비스

## What This Repository Should Not Contain

중앙 Redis 인프라 프로젝트라면 아래 항목은 기본적으로 필요 없다.

- `SpringBootApplication`
- Java 비즈니스 코드
- Controller/Service/Repository 패키지
- API 명세
- 애플리케이션용 Gradle 설정

예외:

- Redis health check 보조 스크립트
- 운영 자동화 스크립트
- 백업/복구 스크립트

## Recommended Runtime Model

V1 실행 모델:

```text
Client
  ↓
API Gateway
  ├ Auth Service
  ├ Permission Service
  ├ User Service
  └ Central Redis
```

중요한 점:

- Redis는 공용 캐시와 임시 상태 저장 계층이다
- 원본 데이터 저장소가 아니다
- 각 서비스는 동일한 Redis에 접근하지만 키 공간은 분리한다

## Migration Direction

현재 저장소가 Spring Boot 형태라면 아래 순서로 전환한다.

1. `src/main/java` 제거
2. `src/test/java` 제거
3. `build.gradle`, `settings.gradle`, `gradlew*` 제거 여부 결정
4. `docker-compose.yml` 추가
5. `redis/redis.conf` 추가
6. `env/` 디렉터리 추가
7. 운영 문서 추가

완전히 인프라 프로젝트로 전환할 경우 Gradle 자체도 제거 가능하다.

## When Spring Boot Would Be Needed

다음 경우에는 Spring Boot 프로젝트가 필요할 수 있다.

- Redis 관리 API를 별도로 제공할 때
- 캐시 삭제용 내부 관리자 서버를 만들 때
- rate limit 설정을 동적으로 변경하는 관리 서비스를 만들 때

하지만 그 경우에도 그것은 "Redis 서버"가 아니라 "Redis를 사용하는 서비스"다.

현재 목표인 중앙 Redis 서버 프로젝트와는 분리하는 것이 맞다.

## V1 Final Decision

V1에서 이 저장소는 다음처럼 정의한다.

- Redis 단일 서버 인프라 저장소
- Spring Boot 애플리케이션 아님
- 단일 endpoint 제공
- 서비스별 key prefix 분리
- 문서 중심 운영
- Docker 기반 실행

## Summary

이 프로젝트를 중앙 Redis 서버로 운영하려면 구조는 인프라 중심으로 바뀌어야 한다.

- `SpringBootApplication`은 필요 없다
- Java 애플리케이션 코드도 필수는 아니다
- 핵심은 `docker-compose.yml`, `redis.conf`, 환경 변수, 운영 문서다

즉, 이 저장소는 앞으로 "Spring Boot 서비스"가 아니라 "중앙 Redis 인프라 저장소"로 관리한다.
