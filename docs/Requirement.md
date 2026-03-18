# Redis Topology Strategy

이 문서는 Gateway, Auth 등 공통 캐시 계층으로 사용하는 Redis의 배치 전략을 정의한다.

## Current Decision

초기 개발 단계에서는 Redis를 중앙 집중형 단일 서버로 운영한다.

형태:

```text
API Gateway
Auth Service
Permission Service
    ↓
Central Redis (single)
```

의도:

- 개발 환경 단순화
- 운영 복잡도 최소화
- 캐시 전략을 먼저 안정화
- Gateway/Auth/Permission의 Redis 사용 패턴 검증

## Why This Is Reasonable

초기에는 다음이 더 중요하다.

- 인증/권한 흐름이 맞는지
- 캐시 키 설계가 맞는지
- TTL 정책이 맞는지
- 서비스 간 책임 분리가 맞는지

이 시점에 Redis 이중화까지 먼저 가져가면 인프라 복잡도만 증가하고,
애플리케이션 정책 검증 속도는 오히려 떨어질 수 있다.

따라서 초기에는:

- Redis 1대
- 단일 endpoint
- 서비스별 key prefix 분리

가 가장 현실적이다.

## Development Topology

```text
Client
  ↓
Load Balancer or direct access
  ↓
API Gateway
  ├ Auth Service
  ├ User Service
  ├ Block Service
  ├ Permission Service
  └ Central Redis (single)
```

## Namespace Rule

같은 Redis를 공유하더라도 서비스별 책임은 분리한다.

예시:

- `gateway:admin-permission:*`
- `gateway:auth-cache:*`
- `gateway:rate-limit:*`
- `auth:session:*`
- `auth:sso-ticket:*`
- `auth:oauth-state:*`
- `permission:policy-cache:*`

원칙:

- 같은 Redis 인프라를 써도 키 공간은 분리
- source of truth는 각 원본 서비스
- Redis는 캐시/임시 상태 저장 용도

## Next Stage

실서비스 운영 단계에서는 Redis를 레플리카 포함 구조로 확장한다.

권장 형태:

```text
Redis Primary 1
Redis Replica 2
```

또는 최소:

```text
Redis Primary 1
Redis Replica 1
```

현재 결정은 운영 시점에 최소 2대 수준의 replica 구성을 고려하는 것이다.

## Production Direction

운영에서는 다음을 목표로 한다.

- primary + replica 구조
- 장애 대응 가능한 읽기/복제 구성
- 백업/복구 전략 수립
- Redis 모니터링 적용
- 장애 발생 시 복구 절차 표준화
- 메트릭, 알림, 대시보드 기반 운영 모니터링
- 서비스별 key prefix 유지
- TTL 정책 유지

## Important Rule

Redis가 확장되더라도 애플리케이션 책임은 바뀌지 않는다.

- Auth는 인증 source of truth
- Permission Service는 권한 source of truth
- Gateway는 정책 오케스트레이션 담당
- Redis는 결과 캐시/임시 상태 저장 계층

## Final Recommendation

현재 단계:

- 중앙 Redis 1대
- 모든 서비스가 공유 가능
- key prefix로 논리 분리

운영 단계:

- Redis primary + replica 구조로 확장
- 최소 2대 수준의 복제 구성 고려
- 복구 절차와 모니터링 체계까지 포함해 운영

즉,

```text
개발 초기: 단일 중앙 Redis
운영 확장: 레플리카 포함 다중 Redis
```
