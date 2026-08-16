# Listaih — Deploy Self-Hosted

Listaih rodando como **imagem all-in-one**: backend (NestJS) + PostgreSQL + Redis em um único container, gerenciados por supervisord.

## Instalação (1 container, 1 clique)

### Opção A — UI de Stacks (ZimaOS, Portainer, etc.)

Cole este YAML na sua UI de Stacks:

```yaml
services:
  listaih:
    image: ghcr.io/phgsbr/listaih:v0.1.0
    container_name: listaih
    restart: unless-stopped
    environment:
      POSTGRES_PASSWORD: SUA-SENHA-FORTE
      JWT_SECRET: SEU-JWT-SECRET-FORTE
    volumes:
      - listaih_data:/var/lib/postgresql/data
    ports:
      - "3000:3000"

volumes:
  listaih_data:
```

Troque `POSTGRES_PASSWORD` e `JWT_SECRET` por valores fortes antes de aplicar.

### Opção B — Docker Compose (linha de comando)

```bash
# Criar docker-compose.yml (use o template acima)
# Editar senhas
docker compose up -d
```

### Opção C — docker run (simples)

```bash
docker run -d \
  --name listaih \
  -p 3000:3000 \
  -e POSTGRES_PASSWORD=SUAGTnsenha \
  -e JWT_SECRET=seuJWTsecret \
  -v listaih_data:/var/lib/postgresql/data \
  --restart unless-stopped \
  ghcr.io/phgsbr/listaih:v0.1.0
```

## Primeiro acesso

1. Abra `http://<IP-DO-SERVIDOR>:3000/admin/`
2. Crie o admin (nome, email, senha, nome da casa)

Ou via API:
```bash
curl -X POST http://<IP>:3000/api/setup \
  -H "Content-Type: application/json" \
  -d '{"email":"voce@exemplo.com","name":"Seu Nome","password":"senha-forte","householdName":"Minha Casa"}'
```

## Verificar saúde

```bash
curl http://<IP>:3000/api/health
# Esperado: {"status":"ok","services":{"database":"up","redis":"up"}}
```

O container tem healthcheck embutido — o Docker mostra `healthy` quando os 3 serviços (postgres, redis, backend) respondem.

## Atualizar (sem rebuild)

```bash
docker compose pull
docker compose up -d
# Ou: docker pull ghcr.io/phgsbr/listaih:latest && docker restart listaih
```

As migrations do Prisma rodam automaticamente no start do container.

## Variáveis de ambiente

| Variável | Default | Descrição |
|----------|---------|-----------|
| `POSTGRES_PASSWORD` | `listaih` | Senha do banco interno |
| `JWT_SECRET` | `change-me-in-production` | Chave de assinatura dos tokens JWT |
| `POSTGRES_USER` | `listaih` | Usuário do banco (raramente mudado) |
| `POSTGRES_DB` | `listaih` | Nome do banco (raramente mudado) |
| `JWT_ACCESS_EXPIRATION` | `15m` | Validade do access token |
| `JWT_REFRESH_EXPIRATION` | `7d` | Validade do refresh token |
| `PORT` | `3000` | Porta do backend (interna) |
| `CORS_ORIGIN` | `*` | Origem permitida para CORS |

## Persistência

- Volume: `/var/lib/postgresql/data` → dados do PostgreSQL
- `docker compose down` NÃO apaga dados
- `docker compose down -v` apaga tudo (reset completo)

## Backup

```bash
docker exec listaih pg_dump -U listaih listaih > backup.sql
```

## Restore

```bash
docker exec -i listaih psql -U listaih listaih < backup.sql
```

## Logs

```bash
docker logs listaih
docker exec listaih cat /var/log/backend.log
docker exec listaih cat /var/log/postgres.log
docker exec listaih cat /var/log/redis.log
```

## Apps Android

- **Phone:** Configurações → Servidor → `http://<IP>:3000` → Testar conexão
- **Wear OS:** usa dados mock na v0.1.0

## TLS / HTTPS (opcional)

Para HTTPS, adicione um reverse proxy (Caddy, Traefik, Nginx) na frente:

```yaml
services:
  listaih:
    # ... (config acima)

  caddy:
    image: caddy:2-alpine
    restart: unless-stopped
    ports:
      - "443:443"
    volumes:
      - ./Caddyfile:/etc/caddy/Caddyfile
    depends_on:
      - listaih
```

`Caddyfile`:
```
listaih.seu-dominio.com {
    reverse_proxy listaih:3000
}
```

## Tags disponíveis no GHCR

| Tag | Descrição |
|-----|-----------|
| `v0.1.0` | Versão fixa |
| `latest` | Último release estável |
| `all-in-one` | Alias para `v0.1.0` (legado) |

## Requisitos

- Docker 20+ (ou Docker Compose v2)
- 512 MB de RAM mínimo
- ~200 MB de disco (imagem + dados)
- Porta 3000 liberada na rede local