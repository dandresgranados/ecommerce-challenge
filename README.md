# E-commerce Challenge — PRT-209

Prueba técnica de Ingeniero de Desarrollo.

Sistema de e-commerce compuesto por:

- **Backend:** Spring Boot 3 + Java 17 (API REST)
- **Frontend:** Angular 21 + TypeScript
- **Base de datos:** H2 (desarrollo) / PostgreSQL (producción)

## Estructura del repositorio

```
ecommerce-challenge/
├── backend/         # API REST Spring Boot
├── frontend/        # SPA Angular
├── docs/            # Documentación adicional (diagramas, decisiones)
├── docker-compose.yml   # (Fase 5) Orquesta backend + frontend + PostgreSQL
└── .github/workflows/   # (Fase 6) CI/CD
```

## Requisitos previos

| Herramienta | Versión |
|---|---|
| JDK          | 17+     |
| Node.js      | 18+     |
| Angular CLI  | 17+     |
| Git          | 2.30+   |
| Docker       | 24+ *(opcional, para producción)* |

Maven no es necesario: el backend incluye Maven Wrapper (`mvnw`).

## Cómo ejecutar (desarrollo)

### Backend

```powershell
cd backend
./mvnw spring-boot:run
# Disponible en http://localhost:8080
# Consola H2: http://localhost:8080/h2-console
```

### Frontend

```powershell
cd frontend
npm install
npm start
# Disponible en http://localhost:4200
```

## Documentación

- [Requisitos originales](docs/requisitos.md)
- [Decisiones de arquitectura](docs/arquitectura.md) *(pendiente)*
- [Modelo de datos](docs/modelo-datos.md) *(pendiente)*
