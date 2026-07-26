import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';

import { ApiError } from '../../../core/models/api-error.model';
import {
  DiscountWindow,
  DiscountWindowType
} from '../../../core/models/discount-window.model';
import { DiscountWindowService } from '../../../core/services/discount-window.service';
import {
  ConfirmDialogComponent,
  ConfirmDialogData
} from '../../../shared/components/confirm-dialog.component';
import {
  DiscountWindowFormDialogComponent,
  DiscountWindowFormDialogData
} from './discount-window-form-dialog/discount-window-form-dialog';

@Component({
  selector: 'app-discount-windows-management',
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatIconModule,
    MatProgressBarModule,
    MatTableModule,
    MatTooltipModule
  ],
  templateUrl: './discount-windows-management.html',
  styleUrl: './discount-windows-management.scss'
})
export class DiscountWindowsManagementComponent {
  private readonly service = inject(DiscountWindowService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly loading = signal(false);
  protected readonly windows = signal<DiscountWindow[]>([]);
  protected readonly displayedColumns = [
    'name',
    'type',
    'rate',
    'startAt',
    'endAt',
    'status',
    'actions'
  ];

  /** Cuenta cuántas ventanas están activas Y vigentes ahora. */
  protected readonly currentlyActive = computed(() => {
    const now = new Date();
    return this.windows().filter(
      (w) => w.active && new Date(w.startAt) <= now && now <= new Date(w.endAt)
    ).length;
  });

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.service.list().subscribe({
      next: (list) => {
        this.windows.set(list);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        const apiError = err.error as ApiError | undefined;
        this.snackBar.open(
          apiError?.message ?? 'Error al cargar ventanas',
          'Cerrar',
          { duration: 4000 }
        );
      }
    });
  }

  openCreate(): void {
    const ref = this.dialog.open<
      DiscountWindowFormDialogComponent,
      DiscountWindowFormDialogData,
      DiscountWindow
    >(DiscountWindowFormDialogComponent, { data: { window: null } });
    ref.afterClosed().subscribe((w) => {
      if (w) this.reload();
    });
  }

  openEdit(window: DiscountWindow): void {
    const ref = this.dialog.open<
      DiscountWindowFormDialogComponent,
      DiscountWindowFormDialogData,
      DiscountWindow
    >(DiscountWindowFormDialogComponent, { data: { window } });
    ref.afterClosed().subscribe((w) => {
      if (w) this.reload();
    });
  }

  confirmDelete(w: DiscountWindow): void {
    const ref = this.dialog.open<ConfirmDialogComponent, ConfirmDialogData, boolean>(
      ConfirmDialogComponent,
      {
        data: {
          title: 'Eliminar ventana',
          message: `¿Seguro que deseas eliminar la ventana "${w.name}"? Las órdenes futuras dejarán de recibir este descuento.`,
          confirmLabel: 'Eliminar',
          color: 'warn',
          icon: 'delete_forever'
        }
      }
    );
    ref.afterClosed().subscribe((ok) => {
      if (!ok) return;
      this.service.delete(w.id).subscribe({
        next: () => {
          this.snackBar.open('Ventana eliminada', 'Cerrar', { duration: 3000 });
          this.reload();
        },
        error: (err: HttpErrorResponse) => {
          const apiError = err.error as ApiError | undefined;
          this.snackBar.open(
            apiError?.message ?? 'No se pudo eliminar',
            'Cerrar',
            { duration: 4000 }
          );
        }
      });
    });
  }

  isCurrentlyActive(w: DiscountWindow): boolean {
    const now = new Date();
    return w.active && new Date(w.startAt) <= now && now <= new Date(w.endAt);
  }

  typeColor(t: DiscountWindowType): 'primary' | 'accent' {
    return t === 'GLOBAL' ? 'primary' : 'accent';
  }
}
