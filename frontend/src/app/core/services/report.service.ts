import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Product } from '../models/product.model';
import { FrequentCustomer, TopSellingProduct } from '../models/report.model';

/**
 * Cliente HTTP para {@code /api/reports/*}. Todos los endpoints requieren
 * rol ADMIN — el {@code adminGuard} en las rutas evita renderizar el
 * dashboard a usuarios normales.
 */
@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/reports`;

  /** Productos actualmente activos (mismo formato que /api/products). */
  activeProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.baseUrl}/products/active`);
  }

  /** Top N productos por unidades totales vendidas (default 5). */
  topSellingProducts(limit = 5): Observable<TopSellingProduct[]> {
    const params = new HttpParams().set('limit', String(limit));
    return this.http.get<TopSellingProduct[]>(`${this.baseUrl}/products/top-selling`, { params });
  }

  /** Top N clientes por número de órdenes (default 5). */
  frequentCustomers(limit = 5): Observable<FrequentCustomer[]> {
    const params = new HttpParams().set('limit', String(limit));
    return this.http.get<FrequentCustomer[]>(`${this.baseUrl}/customers/frequent`, { params });
  }
}
