# Redis MSA Integration

이 문서는 중앙 Redis 서버를 MSA 구조에 실제로 연결할 때 필요한 설정을 정리한다.

## Integration Scope

적용 대상:

- Gateway
- Auth
- Permission
- User

전제:

- Redis 서버는 1대
- 단일 endpoint 사용
- 서비스별 key prefix 분리

## Connection Settings

각 서비스는 동일한 Redis endpoint를 사용한다.

환경 변수 기준:

```text
REDIS_HOST=central-redis
REDIS_PORT=6379
REDIS_PASSWORD=...
REDIS_SSL=false
```

Spring Boot 예시:

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      ssl:
        enabled: ${REDIS_SSL:false}
      timeout: 3s
```

원칙:

- 서비스별로 다른 Redis endpoint를 쓰지 않는다.
- 비밀번호는 Secret Manager 또는 런타임 비밀 주입으로 관리한다.
- 소스 저장소에는 실 비밀번호를 넣지 않는다.

## Key Prefix Policy

모든 키는 아래 형식을 따른다.

```text
{service}:{domain}:{identifier}
```

서비스별 기본 prefix:

- `gateway:*`
- `auth:*`
- `permission:*`
- `user:*`

예시:

- `gateway:rate-limit:{clientKey}`
- `gateway:auth-cache:{userId}`
- `auth:session:{sessionId}`
- `auth:refresh-token:{userId}:{tokenId}`
- `auth:oauth-state:{state}`
- `permission:policy-cache:{roleId}`
- `user:profile-cache:{userId}`

## TTL Policy

권장 TTL:

- `gateway:rate-limit:*` : 1분 ~ 5분
- `gateway:auth-cache:*` : 1분 ~ 10분
- `auth:oauth-state:*` : 5분
- `auth:session:*` : 30분 ~ 24시간
- `auth:refresh-token:*` : 토큰 만료 시간과 동일
- `permission:policy-cache:*` : 5분 ~ 30분
- `user:profile-cache:*` : 1분 ~ 10분

원칙:

- TTL은 서비스 상수로 관리한다.
- 숫자를 코드에 직접 흩뿌리지 않는다.
- 영구 저장 목적의 무제한 TTL은 지양한다.

## Invalidation Policy

### Gateway

- rate limit은 TTL 기반 자연 만료를 기본으로 한다.
- 권한 캐시가 있으면 권한 변경 이벤트 시 삭제한다.

### Auth

- 로그아웃 시 `auth:session:*` 삭제
- refresh token 폐기 시 `auth:refresh-token:*` 삭제
- OAuth 완료 또는 만료 시 `auth:oauth-state:*` 삭제

### Permission

- 권한 정책 변경 시 `permission:policy-cache:*` 삭제
- gateway에 권한 캐시가 있으면 `gateway:admin-permission:*`도 함께 삭제

### User

- 사용자 프로필 변경 시 `user:profile-cache:*` 삭제

## Network Policy

중앙 Redis 서버는 공용 Docker network 이름을 가진다.

기본 network:

```text
redis-core
```

각 서비스는 이 네트워크에 참여해야 한다.

Docker Compose 예시:

```yaml
services:
  auth:
    image: your-auth-service
    networks:
      - redis-core

networks:
  redis-core:
    external: true
    name: redis-core
```

원칙:

- Redis 포트는 필요한 범위만 노출한다.
- 내부 서비스는 네트워크 이름으로 접속한다.
- 운영 환경에서는 VPC, 보안그룹, 방화벽으로 접근 범위를 제한한다.

## Secret Policy

시크릿 적용 원칙:

- `env/.env.prod.example`는 예시만 저장
- 실제 비밀번호는 Git에 올리지 않음
- CI/CD 변수, Docker secret, Kubernetes Secret, Secret Manager 사용

최소 항목:

- `REDIS_PASSWORD`

## Monitoring Policy

이 저장소는 Redis exporter를 선택적으로 제공한다.

노출 메트릭:

- 메모리 사용량
- 연결 수
- evicted keys
- expired keys
- commands processed
- up 상태

실행:

```bash
./docker/run.sh up-monitoring
```

metrics endpoint:

```text
http://localhost:9121/metrics
```

권장 대시보드 항목:

- used memory
- max memory
- connected clients
- rejected connections
- evicted keys
- keyspace hits / misses

## Service Checklist

각 서비스 적용 체크리스트:

1. 중앙 Redis endpoint 연결
2. 서비스 prefix 적용
3. TTL 상수 정의
4. 무효화 시점 구현
5. Redis 장애 시 fallback 정책 결정

## Failure Handling

서비스별로 Redis 장애 시 정책을 정해야 한다.

권장:

- `gateway:rate-limit` : fail-open 여부 명시
- `auth:session` : 세션 소실/조회 실패 처리 명시
- `permission cache` : cache miss 시 원본 조회

중요:

- Redis는 source of truth가 아니다.
- 장애 시 재생성 가능한 데이터만 저장한다.
