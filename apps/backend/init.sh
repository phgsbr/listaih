#!/bin/sh
set -e

PGDATA=/var/lib/postgresql/data

echo "[init] Inicializando PostgreSQL..."

mkdir -p "$PGDATA" /run/postgresql
chown -R postgres:postgres "$PGDATA" /run/postgresql

if [ ! -f "$PGDATA/PG_VERSION" ]; then
    echo "[init] Banco vazio — executando initdb..."
    su postgres -c "initdb -D $PGDATA -A trust" >/dev/null
    echo "[init] initdb concluido"
fi

echo "[init] Iniciando PostgreSQL temporario para bootstrap..."
su postgres -c "pg_ctl -D $PGDATA -o '-c listen_addresses=127.0.0.1 -p 5432' -l /tmp/pg_bootstrap.log start" >/dev/null

for i in $(seq 1 30); do
    if su postgres -c "pg_isready -q -h 127.0.0.1 -p 5432"; then
        break
    fi
    sleep 1
done

echo "[init] Criando usuario e banco se nao existirem..."
su postgres -c "psql -h 127.0.0.1 -p 5432 -U postgres -tAc \"SELECT 1 FROM pg_roles WHERE rolname='${POSTGRES_USER}'\"" | grep -q 1 || \
    su postgres -c "psql -h 127.0.0.1 -p 5432 -U postgres -c \"CREATE USER ${POSTGRES_USER} WITH PASSWORD '${POSTGRES_PASSWORD}';\""

su postgres -c "psql -h 127.0.0.1 -p 5432 -U postgres -tAc \"SELECT 1 FROM pg_database WHERE datname='${POSTGRES_DB}'\"" | grep -q 1 || \
    su postgres -c "psql -h 127.0.0.1 -p 5432 -U postgres -c \"CREATE DATABASE ${POSTGRES_DB} OWNER ${POSTGRES_USER};\""

echo "[init] Aplicando migrations do Prisma..."
cd /app
export DATABASE_URL="postgresql://${POSTGRES_USER}:${POSTGRES_PASSWORD}@127.0.0.1:5432/${POSTGRES_DB}?schema=public"
npx prisma migrate deploy

echo "[init] Parando PostgreSQL temporario..."
su postgres -c "pg_ctl -D $PGDATA -m fast stop" >/dev/null
rm -f "$PGDATA/postmaster.pid"

echo "[init] Bootstrap concluido — supervisord assumira."