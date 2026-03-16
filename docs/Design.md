# Redis V1 Design

Redis V1 구조의 구현 관점입니다.

## Scope

V1의 목표는 다음 세 가지다.

- Redis 1대 운영
- 단일 endpoint 사용
- 서비스별 key prefix 분리

Redis는 공통 캐시와 임시 상태 저장소로만 사용한다.
원본 데이터의 source of truth는 각 서비스가 가진다.

## Topology

```text
Client
  ↓
API Gateway
  ├ Auth Service
  ├ User Service
  ├ Block Service
  ├ Permission Service
  └ Redis (single)
```

### Rules

- 모든 서비스는 동일한 Redis endpoint에 접속한다.
- Redis 인스턴스는 하나만 둔다.
- 데이터 분리는 물리 분리 대신 key prefix로 수행한다.
- Redis는 영구 저장소로 사용하지 않는다.

## Redis Responsibilities

V1에서 Redis가 맡는 책임입니다.

- 인증 세션 저장
- refresh token 메타데이터 저장
- OAuth state 같은 단기 상태 저장
- 권한 조회 결과 캐시
- gateway 레벨 rate limit
- 짧은 TTL 기반 조회 캐시

Redis가 맡지 않는 책임은 다음과 같다.

- 사용자 원본 정보 저장
- 권한 정책 원본 저장
- 장기 영속 데이터 저장

## Endpoint Policy

모든 서비스는 아래 형태의 단일 연결 정보를 사용한다.

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      ssl:
        enabled: ${REDIS_SSL:false}
```

운영 정책:

- 개발/초기 운영 모두 단일 endpoint 유지
- 서비스별로 다른 Redis를 붙이지 않음
- 장애 대응은 V2에서 replica 구조로 확장

## Key Namespace

### Naming Rule

모든 키는 아래 형식을 따른다.

```text
{service}:{domain}:{identifier}
```

예시:

- `gateway:rate-limit:{clientKey}`
- `gateway:auth-cache:{userId}`
- `gateway:admin-permission:{adminId}`
- `auth:session:{sessionId}`
- `auth:refresh-token:{userId}:{tokenId}`
- `auth:oauth-state:{state}`
- `permission:policy-cache:{roleId}`
- `user:profile-cache:{userId}`

### Namespace Rules

- prefix는 서비스 책임 기준으로 나눈다.
- 같은 서비스 안에서는 domain 기준으로 세분화한다.
- identifier는 조회와 삭제가 쉽도록 고정된 규칙을 사용한다.
- 문자열 연결 규칙은 코드에서 공통 정책 클래스로 관리한다.

## TTL Policy

V1에서는 TTL 없는 데이터 저장을 지양한다.

권장 TTL:

- `gateway:rate-limit:*` : 1분 ~ 5분
- `gateway:auth-cache:*` : 1분 ~ 10분
- `gateway:admin-permission:*` : 5분 ~ 30분
- `auth:oauth-state:*` : 5분
- `auth:session:*` : 30분 ~ 24시간
- `auth:refresh-token:*` : 토큰 만료 시간과 동일
- `permission:policy-cache:*` : 5분 ~ 30분
- `user:profile-cache:*` : 1분 ~ 10분

원칙:

- TTL은 도메인별 상수로 관리한다.
- 무한 TTL은 특별한 사유가 없으면 금지한다.
- cache miss 시 원본 서비스를 다시 조회한다.

## Invalidation Policy

Redis는 캐시 계층이므로 원본 변경 시 무효화 정책이 있어야 한다.

### Auth

- 로그아웃 시 `auth:session:*` 삭제
- 토큰 폐기 시 `auth:refresh-token:*` 삭제

### Permission

- 권한 변경 시 `permission:policy-cache:*` 삭제
- gateway가 별도 권한 캐시를 가지면 `gateway:admin-permission:*`도 함께 삭제

### User

- 프로필 변경 시 `user:profile-cache:*` 삭제

## Application Design

애플리케이션 내부에서는 Redis 접근 규칙을 공통화한다.

### Recommended Packages

- `com.drawer.redis.config`
- `com.drawer.redis.key`
- `com.drawer.redis.policy`
- `com.drawer.redis.service`

### Recommended Components

#### RedisConfig

- `RedisConnectionFactory`
- `StringRedisTemplate`
- 필요 시 `RedisTemplate<String, Object>`

#### RedisKeyPolicy

- 서비스별 prefix 관리
- key 생성 메서드 제공
- 문자열 하드코딩 금지

예시:

- `AuthRedisKeys.session(sessionId)`
- `AuthRedisKeys.refreshToken(userId, tokenId)`
- `GatewayRedisKeys.rateLimit(clientKey)`
- `PermissionRedisKeys.policy(roleId)`

#### RedisTtlPolicy

- 도메인별 TTL 상수 관리
- 서비스 코드에서 숫자 직접 입력 금지

#### RedisCacheService

- `set`
- `get`
- `delete`
- TTL 포함 저장

## Serialization Policy

V1 기본 원칙:

- key는 문자열
- 단순 상태값은 문자열 또는 숫자
- 구조화된 값은 JSON 직렬화

주의:

- Java 기본 직렬화는 사용하지 않는다.
- 서비스 간 공유 가능성을 고려해 포맷은 명시적으로 유지한다.

## Operational Rules

- Redis에는 캐시와 임시 상태만 저장한다.
- scan 의존 삭제는 최소화한다.
- 가능하면 정확한 key를 알고 삭제한다.
- 메모리 사용량은 prefix별로 추적 가능해야 한다.
- 장애 시 Redis 데이터는 재생성 가능해야 한다.

## V1 Implementation Order

1. Redis endpoint 설정 추가
2. RedisConfig 추가
3. RedisKeyPolicy 추가
4. RedisTtlPolicy 추가
5. RedisCacheService 추가
6. Auth session 저장부터 적용
7. Gateway rate limit 적용
8. Permission cache 적용

## Out of Scope

다음은 V1 범위에서 제외한다.

- Redis Sentinel
- Redis Cluster
- Primary/Replica 분리 읽기
- Pub/Sub 기반 이벤트 설계
- 분산 락 표준화
- 고가용성 운영 설계

## Summary

V1은 단순해야 한다.

- Redis는 1대
- endpoint는 1개
- 서비스별 prefix로 논리 분리
- TTL과 무효화 정책을 먼저 고정
- 원본 데이터 책임은 각 서비스 유지

이 구조로 먼저 인증, 권한, rate limit 패턴을 검증한 뒤 V2에서 replica 구조로 확장한다.
