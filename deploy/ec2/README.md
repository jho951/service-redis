# EC2 Deploy Assets

이 디렉터리는 EC2에 앱 소스를 clone 하지 않고, 배포용 파일만 올리는 운영 방식을 위한 최소 산출물입니다.

## 서버에 두는 파일

- `docker-compose.yml`
- `.env.production`

## 운영 원칙

- EC2에는 `redis-service` 앱 소스를 clone 하지 않습니다.
- EC2는 Docker image를 pull 받아 실행하는 서버입니다.
- Redis와 exporter는 `127.0.0.1` 에만 바인딩하고, 내부 접근 또는 별도 프록시로만 노출합니다.

## 배포 순서

1. EC2에 이 디렉터리의 파일만 복사합니다.
2. `.env.production.example` 을 `.env.production` 으로 복사하고 값을 채웁니다.
3. 아래 명령으로 이미지를 갱신합니다.

```bash
docker compose --env-file .env.production pull
docker compose --env-file .env.production up -d
```
