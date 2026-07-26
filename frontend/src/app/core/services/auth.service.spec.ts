import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter, Router } from '@angular/router';

import { AuthService } from './auth.service';
import { AuthResponse } from '../models/auth.model';
import { environment } from '../../../environments/environment';

const fakeResponse: AuthResponse = {
  token: 'jwt-token',
  tokenType: 'Bearer',
  expiresInMs: 3_600_000,
  roles: ['USER'],
  user: {
    id: 1,
    username: 'admin',
    email: 'admin@test.local',
    fullName: 'Admin User',
    active: true,
    roles: ['ADMIN', 'USER'],
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
  },
};

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should start with no user when localStorage is empty', () => {
    expect(service.currentUser()).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
    expect(service.isAdmin()).toBe(false);
    expect(service.token).toBeNull();
  });

  it('should store session on successful login', () => {
    service.login({ username: 'admin', password: 'admin123' }).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush(fakeResponse);

    expect(service.token).toBe('jwt-token');
    expect(service.currentUser()?.username).toBe('admin');
    expect(service.isAuthenticated()).toBe(true);
    expect(service.isAdmin()).toBe(true);
    expect(localStorage.getItem('auth_token')).toBe('jwt-token');
  });

  it('should clear session and redirect on logout', () => {
    const router = TestBed.inject(Router);
    const navSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    // Poblar la sesión.
    service.login({ username: 'admin', password: 'admin123' }).subscribe();
    httpMock.expectOne(`${environment.apiUrl}/auth/login`).flush(fakeResponse);
    expect(service.isAuthenticated()).toBe(true);

    service.logout();

    expect(service.currentUser()).toBeNull();
    expect(service.token).toBeNull();
    expect(localStorage.getItem('auth_token')).toBeNull();
    expect(localStorage.getItem('auth_user')).toBeNull();
    expect(navSpy).toHaveBeenCalledWith(['/login']);
  });

  it('should identify roles correctly via hasRole', () => {
    service.login({ username: 'admin', password: 'admin123' }).subscribe();
    httpMock.expectOne(`${environment.apiUrl}/auth/login`).flush(fakeResponse);

    expect(service.hasRole('ADMIN')).toBe(true);
    expect(service.hasRole('USER')).toBe(true);
    expect(service.hasRole('ADMIN', 'USER')).toBe(true);
  });

  it('should return false for hasRole when not authenticated', () => {
    expect(service.hasRole('ADMIN')).toBe(false);
  });

  it('should restore session from localStorage on service init', () => {
    localStorage.setItem('auth_token', 'persisted-token');
    localStorage.setItem('auth_user', JSON.stringify(fakeResponse.user));

    // Reset del TestBed para forzar una nueva instancia del servicio.
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    const restored = TestBed.inject(AuthService);

    expect(restored.token).toBe('persisted-token');
    expect(restored.currentUser()?.username).toBe('admin');
    expect(restored.isAuthenticated()).toBe(true);
  });
});
