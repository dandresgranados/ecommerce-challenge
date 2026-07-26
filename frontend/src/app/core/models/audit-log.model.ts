/**
 * Espejo del enum {@code AuditAction} del backend — todas las acciones
 * auditables del sistema.
 */
export type AuditAction =
  | 'LOGIN'
  | 'LOGIN_FAILED'
  | 'REGISTER'
  | 'CREATE'
  | 'UPDATE'
  | 'DELETE'
  | 'PASSWORD_CHANGE'
  | 'PAY'
  | 'CANCEL';

export interface AuditLog {
  id: number;
  action: AuditAction;
  entityType: string | null;
  entityId: number | null;
  performedBy: string;
  performedAt: string;
  details: string | null;
}

/** Filtros combinables enviados como query params al backend. */
export interface AuditLogSearchCriteria {
  entityType?: string;
  entityId?: number;
  performedBy?: string;
  action?: AuditAction;
  from?: string;
  to?: string;
}
