# ============================================================
# Dockerfile del frontend (Angular 21)
# Multi-stage build:
#   1. builder → Node 22 compila el bundle de producción
#   2. runtime → nginx alpine sirve los estáticos y proxya /api
# ============================================================

# --------- STAGE 1: BUILD ---------
FROM node:22-alpine AS builder

WORKDIR /build

# Copiar package.json + lockfile primero para aprovechar la caché de npm ci.
COPY package*.json ./
RUN npm ci --no-audit --no-fund

# Copiar el resto y compilar en modo producción.
COPY . .
RUN npm run build

# --------- STAGE 2: RUNTIME ---------
FROM nginx:1.27-alpine AS runtime

# nginx.conf personalizado con:
#   - fallback SPA (todas las rutas devuelven index.html)
#   - proxy /api → backend:8080 (nombre del servicio en docker-compose)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Angular 21 emite en /dist/<projectName>/browser
COPY --from=builder /build/dist/frontend/browser /usr/share/nginx/html

EXPOSE 80

# nginx corre en foreground para que Docker vea el proceso principal.
CMD ["nginx", "-g", "daemon off;"]
