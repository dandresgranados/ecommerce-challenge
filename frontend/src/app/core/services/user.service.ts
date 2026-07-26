import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { PagedResponse } from '../models/page.model';
import {
  PasswordChangeRequest,
  User,
  UserCreateRequest,
  UserUpdateRequest
} from '../models/user.model';
import { PageQuery } from './product.service';

/**
 * Cliente HTTP para {@code /api/users}. Toda la clase es ADMIN-only en
 * el backend — el {@code adminGuard} de las rutas se encarga en el cliente.
 */
@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/users`;

  list(paging: PageQuery = {}): Observable<PagedResponse<User>> {
    let params = new HttpParams();
    if (paging.page !== undefined) params = params.set('page', String(paging.page));
    if (paging.size !== undefined) params = params.set('size', String(paging.size));
    if (paging.sort) params = params.set('sort', paging.sort);
    return this.http.get<PagedResponse<User>>(this.baseUrl, { params });
  }

  getById(id: number): Observable<User> {
    return this.http.get<User>(`${this.baseUrl}/${id}`);
  }

  create(request: UserCreateRequest): Observable<User> {
    return this.http.post<User>(this.baseUrl, request);
  }

  update(id: number, request: UserUpdateRequest): Observable<User> {
    return this.http.put<User>(`${this.baseUrl}/${id}`, request);
  }

  changePassword(id: number, request: PasswordChangeRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${id}/password`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
