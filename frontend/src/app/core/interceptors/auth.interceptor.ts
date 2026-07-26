import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { AuthService } from '../services/auth.service';

/**
 * Interceptor JWT (funcional). Se ejecuta en cada petición HTTP:
 *
 * <ol>
 *   <li>Añade {@code Authorization: Bearer <token>} cuando el {@link AuthService}
 *       tiene un token activo.</li>
 *   <li>Ante un 401 (token expirado o inválido) hace logout automático
 *       y redirige a /login. Evita bucles ignorando el propio endpoint de login.</li>
 * </ol>
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const token = auth.token;

  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError((error: unknown) => {
      if (
        error instanceof HttpErrorResponse &&
        error.status === 401 &&
        !req.url.includes('/auth/login') &&
        !req.url.includes('/auth/register')
      ) {
        auth.logout();
        void router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
