#!/bin/sh
set -e

PGBIN=/usr/bin
PGDATA=/var/lib/postgresql/data

echo "[allinone] iniciando PostgreSQL..."
mkdir -p "$PGDATA" /run/postgresql
chown -R postgres:postgres "$PGDATA" /run/postgresql

if [ ! -f "$PGDATA/PG_VERSION" ]; then
  su postgres -c "$PGBIN/initdb -D $PGDATA -A trust"
  echo "[allinone] banco inicializado (primeira execucao)"
fi

su postgres -c "$PGBIN/pg_ctl -D $PGDATA -o '-c listen_addresses=127.0.0.1 -p 5432' -l /tmp/postgres.log start"

for i in $(seq 1 30); do
  if su postgres -c "$PGBIN/pg_isready -q -h 127.0.0.1 -p 5432"; then
    break
  fi
  sleep 1
done

su postgres -c "psql -h 127.0.0.1 -p 5432 -U postgres -tAc \"SELECT 1 FROM pg_roles WHERE rolname='${POSTGRES_USER}'\"" | grep -q 1 || \
  su postgres -c "psql -h 127.0.0.1 -p 5432 -U postgres -c \"CREATE USER ${POSTGRES_USER} WITH PASSWORD '${POSTGRES_PASSWORD}';\""
su postgres -c "psql -h 127.0.0.1 -p 5432 -U postgres -tAc \"SELECT 1 FROM pg_database WHERE datname='${POSTGRES_DB}'\"" | grep -q 1 || \
  su postgres -c "psql -h 127.0.0.1 -p 5432 -U postgres -c \"CREATE DATABASE ${POSTGRES_DB} OWNER ${POSTGRES_USER};\""

echo "[allinone] iniciando Redis..."
redis-server --daemonize yes --port 6379

sleep 1
echo "[allinone] aplicando migrations e subindo o backend..."
cd /app
export DATABASE_URL="postgresql://${POSTGRES_USER}:${POSTGRES_PASSWORD}@127.0.0.1:5432/${POSTGRES_DB}?schema=public"
export REDIS_URL="redis://127.0.0.1:6379"
npx prisma migrate deploy
exec node dist/main.js