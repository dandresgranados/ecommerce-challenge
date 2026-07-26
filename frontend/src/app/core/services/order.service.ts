import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { CreateOrderRequest, Order } from '../models/order.model';
import { PagedResponse } from '../models/page.model';
import { PageQuery } from './product.service';

/**
 * Cliente HTTP para {@code /api/orders}.
 *
 * <ul>
 *   <li>{@link OrderService.create} — POST /api/orders</li>
 *   <li>{@link OrderService.myOrders} — GET /api/orders/my (paginado)</li>
 *   <li>{@link OrderService.getById} — GET /api/orders/:id</li>
 *   <li>{@link OrderService.pay} / {@link OrderService.cancel}
 *       — POST /api/orders/:id/{pay|cancel}</li>
 * </ul>
 */
@Injectable({ providedIn: 'root' })
export class OrderService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/orders`;

  create(request: CreateOrderRequest): Observable<Order> {
    return this.http.post<Order>(this.baseUrl, request);
  }

  myOrders(paging: PageQuery = {}): Observable<PagedResponse<Order>> {
    let params = new HttpParams();
    if (paging.page !== undefined) params = params.set('page', String(paging.page));
    if (paging.size !== undefined) params = params.set('size', String(paging.size));
    if (paging.sort) params = params.set('sort', paging.sort);
    return this.http.get<PagedResponse<Order>>(`${this.baseUrl}/my`, { params });
  }

  getById(id: number): Observable<Order> {
    return this.http.get<Order>(`${this.baseUrl}/${id}`);
  }

  pay(id: number): Observable<Order> {
    return this.http.post<Order>(`${this.baseUrl}/${id}/pay`, {});
  }

  cancel(id: number): Observable<Order> {
    return this.http.post<Order>(`${this.baseUrl}/${id}/cancel`, {});
  }
}
