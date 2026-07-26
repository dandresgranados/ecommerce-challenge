import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle,
} from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ApiError } from '../../../../core/models/api-error.model';
import { Category, CategoryRequest } from '../../../../core/models/category.model';
import { CategoryService } from '../../../../core/services/category.service';

export interface CategoryFormDialogData {
  category: Category | null;
}

/** Diálogo compacto de creación / edición de categoría. */
@Component({
  selector: 'app-category-form-dialog',
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
  ],
  template: `
    <h2 mat-dialog-title>
      <mat-icon>{{ isEdit ? 'edit' : 'add_box' }}</mat-icon>
      {{ isEdit ? 'Editar categoría' : 'Nueva categoría' }}
    </h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="cat-form">
        <mat-form-field appearance="outline">
          <mat-label>Nombre</mat-label>
          <input matInput formControlName="name" />
          @if (form.controls.name.hasError('required') && form.controls.name.touched) {
            <mat-error>El nombre es obligatorio</mat-error>
          }
          @if (form.controls.name.hasError('minlength') && form.controls.name.touched) {
            <mat-error>Mínimo 2 caracteres</mat-error>
          }
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Descripción (opcional)</mat-label>
          <textarea matInput rows="2" formControlName="description"></textarea>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="cancel()" [disabled]="loading()">Cancelar</button>
      <button mat-flat-button color="primary" (click)="onSubmit()" [disabled]="loading()">
        @if (loading()) {
          <mat-spinner diameter="20"></mat-spinner>
        } @else {
          <ng-container>{{ isEdit ? 'Guardar' : 'Crear' }}</ng-container>
        }
      </button>
    </mat-dialog-actions>
  `,
  styles: [
    `
      h2[mat-dialog-title] {
        display: flex;
        align-items: center;
        gap: 0.5rem;
      }
      .cat-form {
        display: flex;
        flex-direction: column;
        gap: 0.25rem;
        min-width: 420px;
        padding-top: 0.5rem;
      }
      @media (max-width: 500px) {
        .cat-form {
          min-width: unset;
        }
      }
    `,
  ],
})
export class CategoryFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly categoryService = inject(CategoryService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialogRef = inject(
    MatDialogRef<CategoryFormDialogComponent, Category | undefined>,
  );

  protected readonly data = inject<CategoryFormDialogData>(MAT_DIALOG_DATA);
  protected readonly loading = signal(false);
  protected readonly isEdit = this.data.category !== null;

  protected readonly form = this.fb.nonNullable.group({
    name: [
      this.data.category?.name ?? '',
      [Validators.required, Validators.minLength(2), Validators.maxLength(64)],
    ],
    description: [this.data.category?.description ?? '', [Validators.maxLength(255)]],
  });

  onSubmit(): void {
    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    const values = this.form.getRawValue();
    const request: CategoryRequest = {
      name: values.name,
      description: values.description || undefined,
    };

    const request$ = this.isEdit
      ? this.categoryService.update(this.data.category!.id, request)
      : this.categoryService.create(request);

    request$.subscribe({
      next: (cat) => {
        this.snackBar.open(this.isEdit ? 'Categoría actualizada' : 'Categoría creada', 'Cerrar', {
          duration: 3000,
        });
        this.dialogRef.close(cat);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        const apiError = err.error as ApiError | undefined;
        this.snackBar.open(apiError?.message ?? 'No se pudo guardar', 'Cerrar', { duration: 5000 });
      },
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
