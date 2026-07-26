import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { PlaceholderComponent } from './shared/components/placeholder.component';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login').then((m) => m.LoginComponent),
    title: 'Iniciar sesión · TGS'
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register/register').then((m) => m.RegisterComponent),
    title: 'Crear cuenta · TGS'
  },
  {
    // Todas las rutas autenticadas se envuelven en el LayoutComponent.
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./layout/layout').then((m) => m.LayoutComponent),
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/home/home').then((m) => m.HomeComponent),
        title: 'Inicio · TGS'
      },
      {
        path: 'products',
        loadComponent: () =>
          import('./features/products/products-list/products-list').then(
            (m) => m.ProductsListComponent
          ),
        title: 'Productos · TGS'
      },
      {
        path: 'orders',
        component: PlaceholderComponent,
        data: {
          title: 'Mis órdenes',
          phase: 'Fase 4.4',
          description:
            'Aquí verás tu carrito, podrás crear órdenes (con los descuentos automáticos aplicados) y consultar el historial de pedidos anteriores.'
        },
        title: 'Mis órdenes · TGS'
      },
      {
        path: 'reports',
        canActivate: [adminGuard],
        component: PlaceholderComponent,
        data: {
          title: 'Reportes',
          phase: 'Fase 4.5',
          description:
            'Dashboard con los 3 reportes: productos activos, top 5 más vendidos y top 5 clientes más frecuentes.'
        },
        title: 'Reportes · TGS'
      },
      {
        path: 'admin',
        canActivate: [adminGuard],
        children: [
          {
            path: 'users',
            component: PlaceholderComponent,
            data: {
              title: 'Gestión de usuarios',
              phase: 'Fase 4.6',
              description:
                'Listado de usuarios registrados con opciones para activar/desactivar, cambiar roles y auditar accesos.'
            },
            title: 'Usuarios · TGS'
          },
          {
            path: 'categories',
            component: PlaceholderComponent,
            data: {
              title: 'Categorías',
              phase: 'Fase 4.6',
              description: 'CRUD de categorías de productos.'
            },
            title: 'Categorías · TGS'
          },
          {
            path: 'discount-windows',
            component: PlaceholderComponent,
            data: {
              title: 'Ventanas de descuento',
              phase: 'Fase 4.6',
              description:
                'CRUD de ventanas de descuento GLOBAL (10 %) y RANDOM (50 %) con vigencia temporal.'
            },
            title: 'Ventanas de descuento · TGS'
          },
          {
            path: 'audit',
            component: PlaceholderComponent,
            data: {
              title: 'Auditoría',
              phase: 'Fase 4.6',
              description:
                'Consulta filtrable de eventos del sistema: LOGIN, CREATE, UPDATE, DELETE, PAY, CANCEL, etc.'
            },
            title: 'Auditoría · TGS'
          }
        ]
      }
    ]
  },
  { path: '**', redirectTo: '' }
];
