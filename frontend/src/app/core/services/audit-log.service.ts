import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuditLog, AuditLogSearchCriteria } from '../models/audit-log.model';
import { PagedResponse } from '../models/page.model';
import { PageQuery } from './product.service';

/**
 * Cliente HTTP para {@code /api/audit-logs}. Solo ADMIN.
 *
 * <p>El backend ordena por {@code performedAt DESC} por defecto (los más
 * recientes primero) — se puede sobrescribir con {@code sort}.
 */
@Injectable({ providedIn: 'root' })
export class AuditLogService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/audit-logs`;

  search(
    criteria: AuditLogSearchCriteria = {},
    paging: PageQuery = {},
  ): Observable<PagedResponse<AuditLog>> {
    let params = new HttpParams();

    if (criteria.entityType?.trim()) {
      params = params.set('entityType', criteria.entityType.trim());
    }
    if (criteria.entityId !== undefined && criteria.entityId !== null) {
      params = params.set('entityId', String(criteria.entityId));
    }
    if (criteria.performedBy?.trim()) {
      params = params.set('performedBy', criteria.performedBy.trim());
    }
    if (criteria.action) params = params.set('action', criteria.action);
    if (criteria.from) params = params.set('from', criteria.from);
    if (criteria.to) params = params.set('to', criteria.to);

    if (paging.page !== undefined) params = params.set('page', String(paging.page));
    if (paging.size !== undefined) params = params.set('size', String(paging.size));
    if (paging.sort) params = params.set('sort', paging.sort);

    return this.http.get<PagedResponse<AuditLog>>(this.baseUrl, { params });
  }
}
