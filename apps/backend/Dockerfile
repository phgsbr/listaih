# Stage 1 — Admin WebUI build
FROM node:22-alpine AS admin-builder

WORKDIR /app

COPY apps/admin/package.json apps/admin/package-lock.json ./
RUN npm ci

COPY apps/admin ./
RUN npm run build

# Stage 2 — Backend build
FROM node:22-alpine AS builder

WORKDIR /app

RUN apk add --no-cache openssl libc6-compat

COPY apps/backend/package.json apps/backend/package-lock.json* ./
RUN npm ci

COPY apps/backend/prisma ./prisma
RUN npx prisma generate

COPY apps/backend/tsconfig.json apps/backend/nest-cli.json ./
COPY apps/backend/src ./src

RUN npx nest build

# Stage 3 — Production
FROM node:22-alpine AS production

WORKDIR /app

ENV NODE_ENV=production
ENV ADMIN_DIST_PATH=/app/admin/dist

RUN apk add --no-cache openssl libc6-compat

COPY apps/backend/package.json apps/backend/package-lock.json* ./
RUN npm ci --omit=dev

# Prisma runtime (engines + client + CLI para `migrate deploy`)
COPY --from=builder /app/node_modules/.prisma ./node_modules/.prisma
COPY --from=builder /app/node_modules/@prisma ./node_modules/@prisma
COPY --from=builder /app/node_modules/prisma ./node_modules/prisma

COPY --from=builder /app/dist ./dist
COPY apps/backend/prisma ./prisma

# Admin WebUI (servida pelo backend em /admin)
COPY --from=admin-builder /app/dist ./admin/dist

EXPOSE 3000

# Migrations aplicadas antes de subir o servidor
CMD ["sh", "-c", "npx prisma migrate deploy && node dist/main.js"]
