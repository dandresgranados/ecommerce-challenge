# Frontend — TGS E-commerce

SPA en **Angular 21** con **standalone components**, **Signals**, **Angular Material**
(tema Azure Blue) y **TypeScript strict**.

> Para la visión general del proyecto ver el [README raíz](../README.md).

## 🚀 Arranque local

```powershell
npm install
npm start
```

UI en → http://localhost:4200 (proxya `/api/*` al backend en `:8080`).

## 📦 Scripts npm

| Comando                | Qué hace                                 |
| ---------------------- | ---------------------------------------- |
| `npm start`            | Dev server con HMR (`ng serve`)          |
| `npm run build`        | Build de producción (`ng build`)         |
| `npm test`             | Tests en modo watch (Vitest)             |
| `npm run test:ci`      | Una pasada con coverage v8 (HTML + lcov) |
| `npm run lint`         | ESLint sobre todo el código              |
| `npm run lint:fix`     | ESLint con autofix                       |
| `npm run format`       | Aplica Prettier                          |
| `npm run format:check` | Verifica formato sin aplicar             |

## 🧩 Estructura por feature

```
src/app/
├── core/                             ← Servicios/modelos/guards singleton
│   ├── guards/
│   │   ├── auth.guard.ts             ← redirige a /login si no hay JWT
│   │   └── admin.guard.ts            ← redirige a / si no es ADMIN
│   ├── interceptors/
│   │   └── auth.interceptor.ts       ← añade Bearer token + maneja 401
│   ├── models/                       ← 10 interfaces tipadas
│   └── services/                     ← 8 servicios HTTP + CartService
├── shared/
│   ├── components/
│   │   ├── confirm-dialog.component.ts  ← reutilizado en todos los delete
│   │   └── placeholder.component.ts
│   └── pipes/
├── layout/
│   └── layout.ts                     ← MatSidenav responsive + toolbar
└── features/                         ← rutas lazy-loaded
    ├── auth/
    │   ├── login/
    │   └── register/
    ├── home/                         ← landing con tiles por rol
    ├── products/
    │   ├── products-list/            ← MatTable + filtros + paginator
    │   └── product-form-dialog/
    ├── orders/
    │   ├── checkout/                 ← carrito + toggle randomOrder
    │   ├── my-orders/                ← historial + pagar/cancelar
    │   └── order-detail-dialog/      ← desglose de descuentos
    ├── reports/
    │   └── reports-dashboard/        ← 3 KPIs + 2 tablas con barras
    └── admin/
        ├── users/                    ← CRUD + password dialog
        ├── categories/               ← CRUD compacto
        ├── discount-windows/         ← CRUD con datetime + rate como %
        └── audit-log/                ← consulta filtrable
```

## 🧪 Tests (32 total)

| Suite            | Tests |
| ---------------- | ----- |
| `AppComponent`   | 2     |
| `AuthService`    | 6     |
| `ProductService` | 6     |
| `CartService`    | 9     |
| `OrderService`   | 5     |
| `ReportService`  | 4     |

**Cobertura**: 93 % líneas · 89 % branches · 92 % funciones · 91 % statements

## 🎨 Angular Material tema Azure Blue

Definido en [src/styles.scss](src/styles.scss):

```scss
@include mat.theme(
  (
    color: (
      primary: mat.$azure-palette,
      tertiary: mat.$blue-palette,
    ),
    typography: Roboto,
    density: 0,
  )
);
```

Todos los componentes usan variables del sistema `--mat-sys-*` para adaptarse
automáticamente al esquema light/dark.

## 🔌 Conexión con el backend

- **Dev**: `frontend/proxy.conf.json` redirige `/api/*` → `http://localhost:8080`
- **Prod (Docker)**: `frontend/nginx.conf` proxya `/api/*` → `http://backend:8080`

En ambos casos el código **usa la misma URL relativa** `/api/...` (definida en
`src/environments/environment*.ts`), sin CORS ni configuración extra.

## 🎯 Patrones aplicados

- **Standalone components** (Angular 15+) — sin NgModules
- **Signals** para estado local + `computed()` para derivados
- **`inject()` function** en vez de constructor injection
- **Functional guards** e **interceptors** (Angular 15+)
- **Lazy loading** con `loadComponent`
- **Reactive Forms** tipados con `FormBuilder.nonNullable`
- **`input.required<T>()`** para inputs obligatorios
- **`takeUntilDestroyed(DestroyRef)`** para cleanup de subscripciones
- **`withComponentInputBinding()`** para enlazar `data:` de rutas a inputs

## 📚 Referencias útiles

- [Angular v21 docs](https://angular.dev)
- [Angular Material components](https://material.angular.dev)
- [Vitest](https://vitest.dev)
