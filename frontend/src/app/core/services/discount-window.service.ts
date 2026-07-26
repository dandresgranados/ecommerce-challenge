import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  DiscountWindow,
  DiscountWindowRequest
} from '../models/discount-window.model';

/**
 * Cliente HTTP para {@code /api/discount-windows}. Solo admin puede
 * gestionar ventanas — su alta/baja afecta el precio final de todas
 * las órdenes que caen en el rango temporal.
 */
@Injectable({ providedIn: 'root' })
export class DiscountWindowService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/discount-windows`;

  list(): Observable<DiscountWindow[]> {
    return this.http.get<DiscountWindow[]>(this.baseUrl);
  }

  getById(id: number): Observable<DiscountWindow> {
    return this.http.get<DiscountWindow>(`${this.baseUrl}/${id}`);
  }

  create(request: DiscountWindowRequest): Observable<DiscountWindow> {
    return this.http.post<DiscountWindow>(this.baseUrl, request);
  }

  update(id: number, request: DiscountWindowRequest): Observable<DiscountWindow> {
    return this.http.put<DiscountWindow>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
