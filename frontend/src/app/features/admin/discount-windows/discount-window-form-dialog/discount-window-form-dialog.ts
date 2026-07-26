import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle
} from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ApiError } from '../../../../core/models/api-error.model';
import {
  DiscountWindow,
  DiscountWindowRequest,
  DiscountWindowType
} from '../../../../core/models/discount-window.model';
import { DiscountWindowService } from '../../../../core/services/discount-window.service';

export interface DiscountWindowFormDialogData {
  window: DiscountWindow | null;
}

/**
 * Diálogo CRUD de una ventana de descuento.
 *
 * <p>El usuario introduce el porcentaje como número entero (0-99); se
 * convierte a fracción (0.10) antes de enviar al backend. Análogo al
 * leer: 0.10 → 10.
 *
 * <p>Los inputs {@code datetime-local} devuelven strings sin timezone
 * (formato {@code yyyy-MM-ddTHH:mm}), que convertimos a ISO-8601 UTC
 * antes de enviar.
 */
@Component({
  selector: 'app-discount-window-form-dialog',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatSlideToggleModule
  ],
  templateUrl: './discount-window-form-dialog.html',
  styleUrl: './discount-window-form-dialog.scss'
})
export class DiscountWindowFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(DiscountWindowService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialogRef = inject(
    MatDialogRef<DiscountWindowFormDialogComponent, DiscountWindow | undefined>
  );

  protected readonly data = inject<DiscountWindowFormDialogData>(MAT_DIALOG_DATA);
  protected readonly loading = signal(false);
  protected readonly isEdit = this.data.window !== null;

  protected readonly types: DiscountWindowType[] = ['GLOBAL', 'RANDOM'];

  protected readonly form = this.fb.nonNullable.group({
    name: [
      this.data.window?.name ?? '',
      [Validators.required, Validators.minLength(2), Validators.maxLength(128)]
    ],
    type: [
      this.data.window?.type ?? ('GLOBAL' as DiscountWindowType),
      [Validators.required]
    ],
    // Porcentaje 0-99 (entero); se convierte a fracción al enviar.
    ratePercent: [
      this.data.window ? Math.round(this.data.window.rate * 100) : 10,
      [Validators.required, Validators.min(1), Validators.max(99)]
    ],
    startAt: [
      this.toLocalInput(this.data.window?.startAt) ?? this.toLocalInput(new Date().toISOString())!,
      [Validators.required]
    ],
    endAt: [
      this.toLocalInput(this.data.window?.endAt) ?? this.toLocalInput(this.oneYearFromNow())!,
      [Validators.required]
    ],
    active: [this.data.window?.active ?? true]
  });

  onSubmit(): void {
    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();

    // Validación cruzada — el backend también la hace pero mejor UX.
    if (new Date(v.startAt) >= new Date(v.endAt)) {
      this.snackBar.open('La fecha de fin debe ser posterior a la de inicio', 'Cerrar', {
        duration: 4000
      });
      return;
    }

    this.loading.set(true);
    const request: DiscountWindowRequest = {
      name: v.name,
      type: v.type,
      rate: v.ratePercent / 100,
      startAt: new Date(v.startAt).toISOString(),
      endAt: new Date(v.endAt).toISOString(),
      active: v.active
    };

    const request$ = this.isEdit
      ? this.service.update(this.data.window!.id, request)
      : this.service.create(request);

    request$.subscribe({
      next: (w) => {
        this.snackBar.open(
          this.isEdit ? 'Ventana actualizada' : 'Ventana creada',
          'Cerrar',
          { duration: 3000 }
        );
        this.dialogRef.close(w);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        const apiError = err.error as ApiError | undefined;
        this.snackBar.open(
          apiError?.message ?? 'No se pudo guardar la ventana',
          'Cerrar',
          { duration: 5000 }
        );
      }
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }

  // ---------- Helpers de fechas ----------

  /**
   * Convierte un ISO-8601 UTC a {@code yyyy-MM-ddTHH:mm} en la zona horaria
   * local del navegador — que es lo que espera el input datetime-local.
   */
  private toLocalInput(iso: string | undefined): string | null {
    if (!iso) return null;
    const d = new Date(iso);
    if (isNaN(d.getTime())) return null;
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }

  private oneYearFromNow(): string {
    const d = new Date();
    d.setFullYear(d.getFullYear() + 1);
    return d.toISOString();
  }
}
