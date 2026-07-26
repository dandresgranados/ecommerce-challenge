import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, AfterViewInit, ViewChild, inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';

import { PageQuery } from '../../../core/services/product.service';
import { OrderService } from '../../../core/services/order.service';
import { Order, OrderStatus } from '../../../core/models/order.model';
import { ApiError } from '../../../core/models/api-error.model';
import {
  ConfirmDialogComponent,
  ConfirmDialogData,
} from '../../../shared/components/confirm-dialog.component';
import { OrderDetailDialogComponent } from '../order-detail-dialog/order-detail-dialog';

/**
 * Historial de órdenes del usuario autenticado ({@code GET /api/orders/my}).
 * Cada fila permite:
 * <ul>
 *   <li>Ver detalle (modal con desglose de descuentos)</li>
 *   <li>Pagar (solo en estado CREATED)</li>
 *   <li>Cancelar (solo en estado CREATED)</li>
 * </ul>
 *
 * <p>Si viene {@code ?highlight=123} en la URL (tras crear una orden en
 * checkout), esa fila se resalta visualmente unos segundos.
 */
@Component({
  selector: 'app-my-orders',
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatIconModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatTableModule,
    MatTooltipModule,
  ],
  templateUrl: './my-orders.html',
  styleUrl: './my-orders.scss',
})
export class MyOrdersComponent implements AfterViewInit, OnInit {
  private readonly orderService = inject(OrderService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly route = inject(ActivatedRoute);

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  protected readonly loading = signal(false);
  protected readonly orders = signal<Order[]>([]);
  protected readonly totalElements = signal(0);
  protected readonly highlightId = signal<number | null>(null);

  protected readonly displayedColumns = [
    'orderNumber',
    'createdAt',
    'items',
    'total',
    'status',
    'actions',
  ];

  protected readonly statusColor: Record<OrderStatus, 'primary' | 'accent' | 'warn'> = {
    CREATED: 'accent',
    PAID: 'primary',
    CANCELED: 'warn',
  };

  protected readonly statusLabel: Record<OrderStatus, string> = {
    CREATED: 'Pendiente',
    PAID: 'Pagada',
    CANCELED: 'Cancelada',
  };

  /** Wrappers tipados para que los templates estrictos no fallen. */
  protected getStatusColor(order: Order): 'primary' | 'accent' | 'warn' {
    return this.statusColor[order.status];
  }

  protected getStatusLabel(order: Order): string {
    return this.statusLabel[order.status];
  }

  private paging: PageQuery = { page: 0, size: 10, sort: 'createdAt,desc' };

  ngOnInit(): void {
    const raw = this.route.snapshot.queryParamMap.get('highlight');
    if (raw) {
      this.highlightId.set(Number(raw));
      // Se limpia el resaltado tras 4 segundos.
      setTimeout(() => this.highlightId.set(null), 4000);
    }
  }

  ngAfterViewInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.orderService.myOrders(this.paging).subscribe({
      next: (page) => {
        this.orders.set(page.content);
        this.totalElements.set(page.page.totalElements);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        const apiError = err.error as ApiError | undefined;
        this.snackBar.open(apiError?.message ?? 'Error al cargar tus órdenes', 'Cerrar', {
          duration: 4000,
        });
      },
    });
  }

  onPage(event: PageEvent): void {
    this.paging = { ...this.paging, page: event.pageIndex, size: event.pageSize };
    this.reload();
  }

  openDetail(order: Order): void {
    // Recargamos por ID para tener siempre el estado actual + desglose completo.
    this.orderService.getById(order.id).subscribe({
      next: (fullOrder) => {
        this.dialog.open(OrderDetailDialogComponent, {
          data: fullOrder,
          width: '720px',
        });
      },
      error: (err: HttpErrorResponse) => {
        const apiError = err.error as ApiError | undefined;
        this.snackBar.open(apiError?.message ?? 'No se pudo cargar el detalle', 'Cerrar', {
          duration: 4000,
        });
      },
    });
  }

  confirmPay(order: Order): void {
    const ref = this.dialog.open<ConfirmDialogComponent, ConfirmDialogData, boolean>(
      ConfirmDialogComponent,
      {
        data: {
          title: 'Confirmar pago',
          message: `¿Confirmas el pago de la orden ${order.orderNumber} por un total de $${order.total.toFixed(2)}?`,
          confirmLabel: 'Pagar',
          color: 'primary',
          icon: 'payments',
        },
      },
    );
    ref.afterClosed().subscribe((ok) => {
      if (!ok) return;
      this.orderService.pay(order.id).subscribe({
        next: () => {
          this.snackBar.open(`Orden ${order.orderNumber} pagada`, 'Cerrar', {
            duration: 3000,
          });
          this.reload();
        },
        error: (err: HttpErrorResponse) => {
          const apiError = err.error as ApiError | undefined;
          this.snackBar.open(apiError?.message ?? 'No se pudo pagar la orden', 'Cerrar', {
            duration: 4000,
          });
        },
      });
    });
  }

  confirmCancel(order: Order): void {
    const ref = this.dialog.open<ConfirmDialogComponent, ConfirmDialogData, boolean>(
      ConfirmDialogComponent,
      {
        data: {
          title: 'Cancelar orden',
          message: `¿Seguro que quieres cancelar la orden ${order.orderNumber}? El stock volverá a estar disponible.`,
          confirmLabel: 'Cancelar orden',
          cancelLabel: 'No, mantener',
          color: 'warn',
          icon: 'cancel',
        },
      },
    );
    ref.afterClosed().subscribe((ok) => {
      if (!ok) return;
      this.orderService.cancel(order.id).subscribe({
        next: () => {
          this.snackBar.open(`Orden ${order.orderNumber} cancelada`, 'Cerrar', {
            duration: 3000,
          });
          this.reload();
        },
        error: (err: HttpErrorResponse) => {
          const apiError = err.error as ApiError | undefined;
          this.snackBar.open(apiError?.message ?? 'No se pudo cancelar la orden', 'Cerrar', {
            duration: 4000,
          });
        },
      });
    });
  }
}
