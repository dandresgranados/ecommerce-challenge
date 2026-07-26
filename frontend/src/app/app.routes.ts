import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login').then((m) => m.LoginComponent),
    title: 'Iniciar sesión · TGS',
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register/register').then((m) => m.RegisterComponent),
    title: 'Crear cuenta · TGS',
  },
  {
    // Todas las rutas autenticadas se envuelven en el LayoutComponent.
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/layout').then((m) => m.LayoutComponent),
    children: [
      {
        path: '',
        loadComponent: () => import('./features/home/home').then((m) => m.HomeComponent),
        title: 'Inicio · TGS',
      },
      {
        path: 'products',
        loadComponent: () =>
          import('./features/products/products-list/products-list').then(
            (m) => m.ProductsListComponent,
          ),
        title: 'Productos · TGS',
      },
      {
        path: 'orders',
        loadComponent: () =>
          import('./features/orders/my-orders/my-orders').then((m) => m.MyOrdersComponent),
        title: 'Mis órdenes · TGS',
      },
      {
        path: 'checkout',
        loadComponent: () =>
          import('./features/orders/checkout/checkout').then((m) => m.CheckoutComponent),
        title: 'Carrito · TGS',
      },
      {
        path: 'reports',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/reports/reports-dashboard/reports-dashboard').then(
            (m) => m.ReportsDashboardComponent,
          ),
        title: 'Reportes · TGS',
      },
      {
        path: 'admin',
        canActivate: [adminGuard],
        children: [
          {
            path: 'users',
            loadComponent: () =>
              import('./features/admin/users/users-management').then(
                (m) => m.UsersManagementComponent,
              ),
            title: 'Usuarios · TGS',
          },
          {
            path: 'categories',
            loadComponent: () =>
              import('./features/admin/categories/categories-management').then(
                (m) => m.CategoriesManagementComponent,
              ),
            title: 'Categorías · TGS',
          },
          {
            path: 'discount-windows',
            loadComponent: () =>
              import('./features/admin/discount-windows/discount-windows-management').then(
                (m) => m.DiscountWindowsManagementComponent,
              ),
            title: 'Ventanas de descuento · TGS',
          },
          {
            path: 'audit',
            loadComponent: () =>
              import('./features/admin/audit-log/audit-log').then((m) => m.AuditLogComponent),
            title: 'Auditoría · TGS',
          },
        ],
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
