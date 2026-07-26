# 🤝 Contribuir a TGS E-commerce

## 🌱 Flujo de trabajo

1. Crea una rama a partir de `main`:
   ```bash
   git checkout -b feat/nombre-de-la-feature
   ```
2. Escribe tests **antes o junto** a la implementación (TDD-friendly).
3. Commits pequeños con [Conventional Commits](#-conventional-commits).
4. Antes de abrir el PR, valida localmente:
   ```powershell
   # Backend
   cd backend
   .\mvnw.cmd verify

   # Frontend
   cd ../frontend
   npm run lint
   npm run format:check
   npm run test:ci
   npm run build
   ```
5. Abre un PR contra `main`. GitHub Actions ejecutará los mismos checks; el
   PR **no puede mergearse hasta que todos estén verdes**.
6. Al mergear, usa **Squash and merge** para mantener el historial limpio.

## 📝 Conventional Commits

Formato:

```
<tipo>(<scope opcional>): <descripción corta en imperativo, minúscula>

<cuerpo opcional con más detalle,
puede ocupar varias líneas>

<footer opcional: BREAKING CHANGE, Closes #123>
```

### Tipos soportados

| Tipo | Cuándo usar |
|---|---|
| `feat` | Nueva funcionalidad para el usuario |
| `fix` | Corrección de un bug |
| `docs` | Solo cambios de documentación |
| `style` | Formato, punto y coma faltantes… (no afecta lógica) |
| `refactor` | Cambio de código que no arregla bug ni añade feature |
| `test` | Añadir o corregir tests |
| `chore` | Tareas de mantenimiento, dependencias, config |
| `ci` | Cambios en workflows de CI |
| `build` | Cambios en el sistema de build (pom.xml, Dockerfile) |
| `perf` | Mejora de rendimiento |

### Scopes habituales

`backend`, `frontend`, `auth`, `products`, `orders`, `reports`, `admin`, `ci`,
`docker`, `deps-backend`, `deps-frontend`.

### Ejemplos reales del proyecto

```
feat(orders): add discount breakdown to order response

feat(frontend): implement cart with localStorage persistence

fix(backend): persist per-component discount rates in order

fix(ci): repair backend test isolation and Docker build

chore(deps-frontend): bump @angular/material to 21.2.15

docs: add DEMO.md with end-to-end test script

refactor(products): extract search criteria to Specification
```

### Reglas rápidas

- Descripción en **imperativo**: "add", no "added" ni "adds"
- Minúscula al inicio (después del `:`)
- Sin punto final
- Máximo ~72 caracteres en la línea principal
- Si el commit rompe algo, añade `BREAKING CHANGE:` en el footer

## 🎨 Estilo de código

- **Backend**: enforced por Checkstyle + SpotBugs (configs en `backend/`)
- **Frontend**: enforced por ESLint + Prettier

Antes de commit, corre los autofix si dudas:

```powershell
# Backend — no hay autofix, revisa los warnings
cd backend
.\mvnw.cmd checkstyle:check

# Frontend
cd frontend
npm run lint:fix
npm run format
```

## 🧪 Qué debe cubrir cada test

| Tipo | Ejemplo | Herramienta |
|---|---|---|
| **Unit** — lógica pura | `DiscountCalculatorTest` | JUnit + Mockito |
| **Slice** — capa JPA aislada | `ProductRepositoryTest` con `@DataJpaTest` | Spring Boot Test |
| **Slice** — controller aislado | `AuthControllerTest` con `@WebMvcTest` | MockMvc |
| **Service** — TS puro | `CartService` (signal + localStorage) | Vitest |
| **HTTP client** — Angular | `AuthService`, `ProductService` | HttpTestingController |

Nueva funcionalidad → al menos 1 test unitario que documente el "happy path"
y 1 que cubra un caso de error.

## 🔀 Naming de ramas

```
feat/<nombre-en-kebab-case>       ← nuevas features
fix/<nombre>                       ← bug fixes
chore/<nombre>                     ← mantenimiento
docs/<nombre>                      ← solo docs
```

## 🚫 Qué NO commitear

- `target/`, `node_modules/`, `dist/`, `.angular/`
- `.env` con credenciales reales (usar `.env.example` como plantilla)
- Archivos generados de IDE (`.idea/`, `.vscode/settings.json` local)
- Los `*.iml`, `*.log`, `*.class`

Todo esto ya está en `.gitignore`.
