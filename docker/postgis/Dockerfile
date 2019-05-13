FROM postgres:9.5

# Subscribe to pgsql-pkg-debian@postgresql.org for release announcements
# and watch https://github.com/docker-library/postgres Dockerfile
ARG POSTGIS_MAJOR=2.3

ARG DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
&& apt-get install -y --no-install-recommends \
 postgresql-$PG_MAJOR-postgis-$POSTGIS_MAJOR \
 postgis-$POSTGIS_MAJOR
# pgbouncer \


ENTRYPOINT ["docker-entrypoint.sh"]

EXPOSE 5432
CMD ["postgres"]