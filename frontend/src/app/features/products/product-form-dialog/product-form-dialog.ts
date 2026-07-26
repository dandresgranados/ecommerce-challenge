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
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';

import { Category } from '../../../core/models/category.model';
import { ApiError } from '../../../core/models/api-error.model';
import { Product, ProductRequest, ProductUpdateRequest } from '../../../core/models/product.model';
import { ProductService } from '../../../core/services/product.service';

/** Datos que espera el diálogo. Si {@code product} es null → modo crear. */
export interface ProductFormDialogData {
  product: Product | null;
  categories: Category[];
}

/**
 * Modal para crear o editar productos.
 *
 * <ul>
 *   <li><b>Crear:</b> el SKU es editable y {@code initialStock} +
 *       {@code minStock} son visibles.</li>
 *   <li><b>Editar:</b> SKU y stock quedan deshabilitados (el backend
 *       no permite cambiar SKU y el stock se gestiona vía órdenes).</li>
 * </ul>
 *
 * Devuelve el {@link Product} creado/actualizado vía {@code afterClosed()}
 * o {@code undefined} si el usuario cancela.
 */
@Component({
  selector: 'app-product-form-dialog',
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
    MatSelectModule,
    MatSlideToggleModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './product-form-dialog.html',
  styleUrl: './product-form-dialog.scss',
})
export class ProductFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly productService = inject(ProductService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialogRef = inject(
    MatDialogRef<ProductFormDialogComponent, Product | undefined>,
  );

  protected readonly data = inject<ProductFormDialogData>(MAT_DIALOG_DATA);
  protected readonly loading = signal(false);
  protected readonly isEdit = this.data.product !== null;

  protected readonly form = this.fb.nonNullable.group({
    sku: [
      { value: this.data.product?.sku ?? '', disabled: this.isEdit },
      [Validators.required, Validators.minLength(2), Validators.maxLength(64)],
    ],
    name: [
      this.data.product?.name ?? '',
      [Validators.required, Validators.minLength(2), Validators.maxLength(128)],
    ],
    description: [this.data.product?.description ?? '', [Validators.maxLength(512)]],
    price: [this.data.product?.price ?? 0, [Validators.required, Validators.min(0.0001)]],
    categoryId: [this.data.product?.categoryId ?? 0, [Validators.required, Validators.min(1)]],
    active: [this.data.product?.active ?? true],
    initialStock: [{ value: 0, disabled: this.isEdit }, [Validators.min(0)]],
    minStock: [{ value: 0, disabled: this.isEdit }, [Validators.min(0)]],
  });

  onSubmit(): void {
    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    const values = this.form.getRawValue();

    const request$ = this.isEdit
      ? this.productService.update(this.data.product!.id, {
          name: values.name,
          description: values.description || undefined,
          price: values.price,
          categoryId: values.categoryId,
          active: values.active,
        } satisfies ProductUpdateRequest)
      : this.productService.create({
          sku: values.sku,
          name: values.name,
          description: values.description || undefined,
          price: values.price,
          categoryId: values.categoryId,
          active: values.active,
          initialStock: values.initialStock,
          minStock: values.minStock,
        } satisfies ProductRequest);

    request$.subscribe({
      next: (product) => {
        this.snackBar.open(this.isEdit ? 'Producto actualizado' : 'Producto creado', 'Cerrar', {
          duration: 3000,
        });
        this.dialogRef.close(product);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        const apiError = err.error as ApiError | undefined;
        const message =
          (apiError?.fieldErrors && Object.values(apiError.fieldErrors)[0]) ??
          apiError?.message ??
          'No se pudo guardar el producto';
        this.snackBar.open(message, 'Cerrar', { duration: 5000 });
      },
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
