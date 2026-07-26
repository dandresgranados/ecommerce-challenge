import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';

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
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/home/home').then((m) => m.HomeComponent),
    title: 'Inicio · TGS'
  },
  { path: '**', redirectTo: '' }
];
