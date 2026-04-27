# Troubleshooting

## Docker 실행 시 env 파일을 찾지 못한다

증상:

- `Source env file not found`

점검:

1. 실행 모드에 맞는 env 파일이 있는지 확인합니다.
2. local binary는 `env.local`, Docker dev는 `env.docker.dev`, Docker prod는 `env.docker.prod`를 사용하는지 확인합니다.
3. 별도 파일을 쓰면 `ENV_FILE=/path/to/file` 형식으로 명시했는지 확인합니다.

## 애플리케이션에서 Redis 연결에 실패한다

증상:

- `Connection refused`
- `redis-server` host lookup 실패

점검:

1. 단일 Docker host 구성인지, EC2 분산 배포인지 먼저 구분합니다.
2. 단일 host compose면 `redis-server` 또는 `central-redis` alias를 사용합니다.
3. 분산 배포면 compose alias 대신 Redis private DNS/IP 또는 관리형 endpoint를 사용합니다.
4. `6379` 포트가 허용된 내부 네트워크에서 열려 있는지 확인합니다.

## Redis exporter metric이 수집되지 않는다

점검:

1. `./scripts/run.docker.sh up-monitoring`으로 exporter profile이 함께 떠 있는지 확인합니다.
2. exporter 포트 `9121`이 열려 있는지 확인합니다.
3. monitoring-service가 exporter private endpoint를 수집하도록 설정되었는지 확인합니다.

## local binary 실행이 실패한다

증상:

- `redis-server` 실행 실패
- 설정 파일 관련 오류

점검:

1. 로컬에 `redis-server` 바이너리가 설치되어 있는지 확인합니다.
2. `scripts/run.local.sh`가 `env.local`과 `local/redis.conf`를 읽는지 확인합니다.
3. Docker용 `docker/redis.conf`와 local용 `local/redis.conf`를 혼동하지 않았는지 확인합니다.

## prod compose 또는 배포 자산 검증이 실패한다

점검:

1. `docker/compose.yml`, `docker/prod/compose.yml`, `deploy/ec2/docker-compose.yml` 사이 drift가 없는지 확인합니다.
2. `scripts/validate-ec2-deploy-assets.sh` 결과를 확인합니다.
3. runtime contract와 materialized env 값이 맞는지 `scripts/validate-runtime-contract.sh`로 점검합니다.

## Redis는 떠 있는데 기능이 간헐적으로 실패한다

해석:

- 이 저장소는 공용 Redis 인프라를 제공하지만, TTL과 key prefix는 각 서비스가 소유합니다.

점검:

1. 서비스별 key prefix 충돌이 없는지 확인합니다.
2. 서비스 코드가 key 저장 시 TTL을 실제로 설정하는지 확인합니다.
3. 메모리 정책과 eviction 상황 때문에 예상보다 빨리 key가 사라지는 것은 아닌지 확인합니다.
