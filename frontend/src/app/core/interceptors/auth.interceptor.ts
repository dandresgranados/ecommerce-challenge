import { HttpInterceptorFn } from '@angular/common/http';

/**
 * Interceptor JWT. Añade el header Authorization: Bearer <token>
 * a todas las peticiones salientes cuando hay un token en localStorage.
 *
 * En la Fase 4.1 se completará la lectura del token desde AuthService
 * y el manejo de 401 (logout automático).
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = typeof localStorage !== 'undefined' ? localStorage.getItem('auth_token') : null;

  if (!token) {
    return next(req);
  }

  const authReq = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });

  return next(authReq);
};
