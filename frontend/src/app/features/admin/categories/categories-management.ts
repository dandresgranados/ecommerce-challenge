import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';

import { ApiError } from '../../../core/models/api-error.model';
import { Category } from '../../../core/models/category.model';
import { CategoryService } from '../../../core/services/category.service';
import {
  ConfirmDialogComponent,
  ConfirmDialogData
} from '../../../shared/components/confirm-dialog.component';
import {
  CategoryFormDialogComponent,
  CategoryFormDialogData
} from './category-form-dialog/category-form-dialog';

@Component({
  selector: 'app-categories-management',
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressBarModule,
    MatTableModule,
    MatTooltipModule
  ],
  templateUrl: './categories-management.html',
  styleUrl: './categories-management.scss'
})
export class CategoriesManagementComponent {
  private readonly categoryService = inject(CategoryService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly loading = signal(false);
  protected readonly categories = signal<Category[]>([]);
  protected readonly displayedColumns = ['id', 'name', 'description', 'actions'];

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.categoryService.list().subscribe({
      next: (list) => {
        this.categories.set(list);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        const apiError = err.error as ApiError | undefined;
        this.snackBar.open(
          apiError?.message ?? 'Error al cargar categorías',
          'Cerrar',
          { duration: 4000 }
        );
      }
    });
  }

  openCreate(): void {
    const ref = this.dialog.open<CategoryFormDialogComponent, CategoryFormDialogData, Category>(
      CategoryFormDialogComponent,
      { data: { category: null } }
    );
    ref.afterClosed().subscribe((c) => {
      if (c) this.reload();
    });
  }

  openEdit(category: Category): void {
    const ref = this.dialog.open<CategoryFormDialogComponent, CategoryFormDialogData, Category>(
      CategoryFormDialogComponent,
      { data: { category } }
    );
    ref.afterClosed().subscribe((c) => {
      if (c) this.reload();
    });
  }

  confirmDelete(category: Category): void {
    const ref = this.dialog.open<ConfirmDialogComponent, ConfirmDialogData, boolean>(
      ConfirmDialogComponent,
      {
        data: {
          title: 'Eliminar categoría',
          message: `¿Seguro que deseas eliminar la categoría "${category.name}"? Puede fallar si tiene productos asociados.`,
          confirmLabel: 'Eliminar',
          color: 'warn',
          icon: 'delete_forever'
        }
      }
    );
    ref.afterClosed().subscribe((ok) => {
      if (!ok) return;
      this.categoryService.delete(category.id).subscribe({
        next: () => {
          this.snackBar.open('Categoría eliminada', 'Cerrar', { duration: 3000 });
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
}
