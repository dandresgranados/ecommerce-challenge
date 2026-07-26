import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  AfterViewInit,
  Component,
  DestroyRef,
  ViewChild,
  inject,
  signal,
  OnInit,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { debounceTime, distinctUntilChanged } from 'rxjs';

import { ApiError } from '../../../core/models/api-error.model';
import {
  AuditAction,
  AuditLog,
  AuditLogSearchCriteria,
} from '../../../core/models/audit-log.model';
import { AuditLogService } from '../../../core/services/audit-log.service';
import { PageQuery } from '../../../core/services/product.service';

/**
 * Vista de la auditoría (solo ADMIN, solo lectura).
 * Filtros combinables: acción, tipo/id de entidad, usuario, rango de fechas.
 * Server-side paging con MatPaginator.
 */
@Component({
  selector: 'app-audit-log',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatSelectModule,
    MatTableModule,
    MatTooltipModule,
  ],
  templateUrl: './audit-log.html',
  styleUrl: './audit-log.scss',
})
export class AuditLogComponent implements AfterViewInit, OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(AuditLogService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  protected readonly loading = signal(false);
  protected readonly logs = signal<AuditLog[]>([]);
  protected readonly totalElements = signal(0);

  protected readonly displayedColumns = [
    'performedAt',
    'action',
    'entity',
    'performedBy',
    'details',
  ];

  protected readonly actions: AuditAction[] = [
    'LOGIN',
    'LOGIN_FAILED',
    'REGISTER',
    'CREATE',
    'UPDATE',
    'DELETE',
    'PASSWORD_CHANGE',
    'PAY',
    'CANCEL',
  ];

  protected readonly entityTypes = ['User', 'Product', 'Category', 'Order', 'DiscountWindow'];

  protected readonly filterForm = this.fb.nonNullable.group({
    action: [null as AuditAction | null],
    entityType: [null as string | null],
    entityId: [null as number | null],
    performedBy: [''],
    from: [''],
    to: [''],
  });

  private paging: PageQuery = { page: 0, size: 20, sort: 'performedAt,desc' };

  ngOnInit(): void {
    this.filterForm.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.paging = { ...this.paging, page: 0 };
        if (this.paginator) this.paginator.firstPage();
        this.reload();
      });
  }

  ngAfterViewInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    const v = this.filterForm.getRawValue();
    const criteria: AuditLogSearchCriteria = {
      action: v.action ?? undefined,
      entityType: v.entityType ?? undefined,
      entityId: v.entityId ?? undefined,
      performedBy: v.performedBy || undefined,
      from: v.from ? new Date(v.from).toISOString() : undefined,
      to: v.to ? new Date(v.to).toISOString() : undefined,
    };
    this.service.search(criteria, this.paging).subscribe({
      next: (page) => {
        this.logs.set(page.content);
        this.totalElements.set(page.page.totalElements);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        const apiError = err.error as ApiError | undefined;
        this.snackBar.open(apiError?.message ?? 'Error al cargar auditoría', 'Cerrar', {
          duration: 4000,
        });
      },
    });
  }

  onPage(event: PageEvent): void {
    this.paging = { ...this.paging, page: event.pageIndex, size: event.pageSize };
    this.reload();
  }

  clearFilters(): void {
    this.filterForm.reset({
      action: null,
      entityType: null,
      entityId: null,
      performedBy: '',
      from: '',
      to: '',
    });
  }

  /** Color del chip según la naturaleza de la acción. */
  actionColor(action: AuditAction): 'primary' | 'accent' | 'warn' | undefined {
    switch (action) {
      case 'LOGIN_FAILED':
      case 'DELETE':
      case 'CANCEL':
        return 'warn';
      case 'CREATE':
      case 'PAY':
        return 'primary';
      case 'UPDATE':
      case 'PASSWORD_CHANGE':
      case 'REGISTER':
        return 'accent';
      default:
        return undefined;
    }
  }
}
