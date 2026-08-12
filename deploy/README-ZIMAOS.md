# Deploy Listaih no ZimaOS (home lab)

Backend (API + Admin WebUI), PostgreSQL e Redis rodando como stack Docker Compose no ZimaOS.

## Pré-requisitos
- ZimaOS com Docker Compose disponível (via terminal/SSH ou UI de Stacks)
- Porta **3000** liberada na rede local (alterável em `.env` com `BACKEND_PORT`)

## Passo a passo

### 1. Clonar o repositório (público — sem token)
```bash
git clone https://github.com/phgsbr/listaih.git listaih
cd listaih
```

### 2. Configurar o ambiente
```bash
cp .env.example .env
# edite .env: POSTGRES_PASSWORD e JWT_SECRET (gere com: openssl rand -hex 32)
```

### 3. Subir a stack
```bash
docker compose up -d --build
```

A primeira execução builda as imagens (Node 22 + Admin Vite) e pode levar alguns minutos.
As migrations do Prisma (6) são aplicadas automaticamente antes do backend iniciar.

### 4. Verificar
```bash
docker compose ps                 # todos healthy
curl http://localhost:3000/api/health
curl http://localhost:3000/api/external/health
# WebUI: http://<IP-DO-ZIMA>:3000/admin/
```

### 5. Primeiro acesso (setup)
Acesse `http://<IP-DO-ZIMA>:3000/admin/` e crie o admin (nome, email, senha, casa)
— ou via API:
```bash
curl -X POST http://<IP-DO-ZIMA>:3000/api/setup \
  -H "Content-Type: application/json" \
  -d '{"email":"voce@exemplo.com","name":"Seu Nome","password":"senha-forte","householdName":"Minha Casa"}'
```

### 6. Apps Android
- **Phone:** Configurações → Servidor → `http://<IP-DO-ZIMA>:3000` → Testar conexão
- **Wear OS:** (fase futura — hoje usa dados mock)

### 7. Atualizar depois
```bash
git pull
docker compose up -d --build
```

## Observações
- **Sem TLS nesta versão:** o tráfego é HTTP dentro da LAN. Para expor com HTTPS,
  edite o `Caddyfile` com seu domínio (exemplo comentado) e libere as portas 80/443.
- **Persistência:** dados ficam nos volumes `postgres_data` e `redis_data`;
  `docker compose down` NÃO apaga dados; `docker compose down -v` apaga.
- **Logs:** `docker compose logs -f backend`
- **Backup do banco:**
  ```bash
  docker exec listaih-postgres pg_dump -U listaih listaih > backup.sql
  ```

## Alternativa: imagem publicada (GHCR, sem build no ZimaOS)
Se o ZimaOS não tiver terminal/SSH ou se preferir só a imagem pronta:
```bash
# uma vez, na máquina com Docker (publicação)
docker build -f apps/backend/Dockerfile -t ghcr.io/phgsbr/listaih:0.1.0 .
docker push ghcr.io/phgsbr/listaih:0.1.0
```
No ZimaOS:
```bash
docker compose -f docker-compose.yml -f docker-compose.zimaos.yml up -d
```
O override (`docker-compose.zimaos.yml`) troca `build:` pela `image:` do GHCR.
