FROM redis:7.4-alpine

COPY docker/redis.conf /usr/local/etc/redis/redis.conf
COPY docker/dev/redis.conf /usr/local/etc/redis/dev/redis.conf
COPY docker/prod/redis.conf /usr/local/etc/redis/prod/redis.conf
COPY docker/docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh

RUN chmod +x /usr/local/bin/docker-entrypoint.sh \
    && mkdir -p /data /var/log/redis

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
