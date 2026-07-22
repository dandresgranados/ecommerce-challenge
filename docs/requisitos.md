# Requisitos originales (PRT-209)

Versión 2 · 21-ABR-2025

## 1. Objetivo

Evaluación de conocimientos y desempeño de los candidatos preseleccionados
para el perfil de desarrollador.

## 2. Alcance

Construir un servicio de e-commerce en **Spring Boot** y un framework de
frontend que implemente **Node.js + TypeScript** (Angular en este caso),
desde el cual se puedan gestionar:

- Productos
- Inventarios
- Órdenes

### MVP

- [ ] a) Login
- [ ] b) Creación de usuario
- [ ] c) Gestión de usuarios
- [ ] d) CRUD
- [ ] e) Reportes:
  - [ ] i. Productos activos
  - [ ] ii. Top 5 de lo más vendido
  - [ ] iii. Top 5 de los clientes frecuentes
- [ ] f) Búsqueda de productos por distintos criterios
- [ ] g) Implementación de auditoría

### Casos especiales de funcionamiento

- [ ] **a)** Dado un rango de tiempo parametrizado, todas las órdenes
  realizadas tendrán un descuento del **10 %**.
- [ ] **b)** Si se selecciona la función de *pedido aleatorio*, se brinda
  un descuento del **50 %** (solo aplica si la orden queda registrada
  dentro del rango de tiempo definido).
- [ ] **c)** Si el cliente es frecuente, se otorga un descuento adicional
  del **5 %**.

### Obligatorios

- [x] Manejo de metodologías de versionamiento (Git + convenciones)
- [ ] Pruebas unitarias

### Deseables

- [ ] CI/CD (GitHub Actions)
- [ ] Contenerización (Docker + docker-compose)
- [ ] Análisis estático de código (SonarQube / SpotBugs / ESLint)
- [x] Documentación (Markdown)

## 3. Entregables

- Link del repositorio de código fuente correspondiente a la aplicación y su
  base de datos.
- Video donde se evidencie el funcionamiento del sistema.
