#!/bin/sh
set -e

# PostgreSQL
if ! pg_isready -q -h 127.0.0.1 -p 5432 -U listaih; then
    echo "FAIL: PostgreSQL nao responde"
    exit 1
fi

# Redis
if ! redis-cli -h 127.0.0.1 -p 6379 ping >/dev/null 2>&1; then
    echo "FAIL: Redis nao responde"
    exit 1
fi

# Backend HTTP health
if ! wget -qO- http://127.0.0.1:3000/api/health >/dev/null 2>&1; then
    echo "FAIL: Backend HTTP /api/health nao responde"
    exit 1
fi

echo "OK: todos os servicos saudaveis"
exit 0