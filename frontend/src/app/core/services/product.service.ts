import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { PagedResponse } from '../models/page.model';
import {
  Product,
  ProductRequest,
  ProductSearchCriteria,
  ProductUpdateRequest,
} from '../models/product.model';

/** Parámetros de paginación estándar de Spring Data. */
export interface PageQuery {
  page?: number;
  size?: number;
  /** Formato Spring: {@code "name,asc"} o {@code "price,desc"}. */
  sort?: string;
}

/**
 * Cliente HTTP para {@code /api/products}. Todas las operaciones devuelven
 * {@link Observable}; los componentes son responsables de suscribirse
 * (con {@code | async} en templates o {@code .subscribe()} en TS).
 */
@Injectable({ providedIn: 'root' })
export class ProductService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/products`;

  /**
   * Búsqueda paginada con filtros dinámicos.
   * Sólo se envían los campos definidos (no {@code undefined} ni cadenas vacías).
   */
  search(
    criteria: ProductSearchCriteria = {},
    paging: PageQuery = {},
  ): Observable<PagedResponse<Product>> {
    let params = new HttpParams();

    if (criteria.name?.trim()) params = params.set('name', criteria.name.trim());
    if (criteria.categoryId !== undefined && criteria.categoryId !== null) {
      params = params.set('categoryId', String(criteria.categoryId));
    }
    if (criteria.minPrice !== undefined && criteria.minPrice !== null) {
      params = params.set('minPrice', String(criteria.minPrice));
    }
    if (criteria.maxPrice !== undefined && criteria.maxPrice !== null) {
      params = params.set('maxPrice', String(criteria.maxPrice));
    }
    if (criteria.active !== undefined && criteria.active !== null) {
      params = params.set('active', String(criteria.active));
    }
    if (paging.page !== undefined) params = params.set('page', String(paging.page));
    if (paging.size !== undefined) params = params.set('size', String(paging.size));
    if (paging.sort) params = params.set('sort', paging.sort);

    return this.http.get<PagedResponse<Product>>(this.baseUrl, { params });
  }

  getById(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.baseUrl}/${id}`);
  }

  create(request: ProductRequest): Observable<Product> {
    return this.http.post<Product>(this.baseUrl, request);
  }

  update(id: number, request: ProductUpdateRequest): Observable<Product> {
    return this.http.put<Product>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
