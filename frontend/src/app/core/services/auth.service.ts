import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest } from '../models/auth.model';
import { Role } from '../models/role.model';
import { User } from '../models/user.model';

/**
 * Claves usadas en {@link Storage.localStorage} para persistir la sesión
 * entre recargas de página. Se leen al arrancar el servicio y se limpian
 * en {@link AuthService.logout}.
 */
const TOKEN_KEY = 'auth_token';
const USER_KEY = 'auth_user';

/**
 * Servicio singleton de autenticación.
 *
 * <ul>
 *   <li>Expone el usuario actual como {@code signal} para que los componentes
 *       reaccionen automáticamente a login/logout.</li>
 *   <li>Persiste token + usuario en {@code localStorage} para sobrevivir a un F5.</li>
 *   <li>El interceptor JWT lee el token via {@link authService.token}.</li>
 * </ul>
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  /** Signal privado, expuesto como readonly a los consumidores. */
  private readonly _currentUser = signal<User | null>(this.readUserFromStorage());
  private readonly _token = signal<string | null>(this.readTokenFromStorage());

  /** Usuario autenticado o {@code null}. Reactivo — usar con {@code computed}. */
  readonly currentUser = this._currentUser.asReadonly();

  /** {@code true} cuando hay un usuario en sesión. */
  readonly isAuthenticated = computed(() => this._currentUser() !== null);

  /** {@code true} cuando el usuario tiene el rol ADMIN. */
  readonly isAdmin = computed(() => this._currentUser()?.roles.includes('ADMIN') ?? false);

  /** Token JWT actual (leído por el interceptor). {@code null} si no hay sesión. */
  get token(): string | null {
    return this._token();
  }

  /** Autentica un usuario y persiste la sesión. */
  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/auth/login`, request)
      .pipe(tap((response) => this.storeSession(response)));
  }

  /** Registra un usuario nuevo (rol USER) y lo autentica. */
  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/auth/register`, request)
      .pipe(tap((response) => this.storeSession(response)));
  }

  /**
   * Limpia la sesión local y redirige a /login.
   *
   * <p>No llama al backend — el backend es stateless (JWT). El token queda
   * válido hasta expirar, pero al no estar en el cliente ya no se usa.
   */
  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this._token.set(null);
    this._currentUser.set(null);
    void this.router.navigate(['/login']);
  }

  /** Comprueba si el usuario tiene alguno de los roles indicados. */
  hasRole(...roles: Role[]): boolean {
    const user = this._currentUser();
    return user !== null && roles.some((r) => user.roles.includes(r));
  }

  // ---------- Internos ----------

  private storeSession(response: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, response.token);
    localStorage.setItem(USER_KEY, JSON.stringify(response.user));
    this._token.set(response.token);
    this._currentUser.set(response.user);
  }

  private readTokenFromStorage(): string | null {
    if (typeof localStorage === 'undefined') return null;
    return localStorage.getItem(TOKEN_KEY);
  }

  private readUserFromStorage(): User | null {
    if (typeof localStorage === 'undefined') return null;
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as User;
    } catch {
      // Storage corrupto: limpiar para evitar bucles.
      localStorage.removeItem(USER_KEY);
      localStorage.removeItem(TOKEN_KEY);
      return null;
    }
  }
}
