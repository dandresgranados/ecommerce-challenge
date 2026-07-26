import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { AfterViewInit, Component, ViewChild, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSort, MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { debounceTime, distinctUntilChanged, forkJoin } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DestroyRef } from '@angular/core';

import { AuthService } from '../../../core/services/auth.service';
import { CartService } from '../../../core/services/cart.service';
import { CategoryService } from '../../../core/services/category.service';
import { PageQuery, ProductService } from '../../../core/services/product.service';
import { Category } from '../../../core/models/category.model';
import {
  Product,
  ProductSearchCriteria
} from '../../../core/models/product.model';
import {
  ConfirmDialogComponent,
  ConfirmDialogData
} from '../../../shared/components/confirm-dialog.component';
import {
  ProductFormDialogComponent,
  ProductFormDialogData
} from '../product-form-dialog/product-form-dialog';
import { ApiError } from '../../../core/models/api-error.model';

/**
 * Listado principal de productos con:
 * <ul>
 *   <li>Búsqueda por texto (debounce 300 ms) y filtros por categoría,
 *       precio y estado.</li>
 *   <li>Paginación server-side y ordenamiento.</li>
 *   <li>CRUD (crear / editar / eliminar) sólo visible para ADMIN.</li>
 * </ul>
 */
@Component({
  selector: 'app-products-list',
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
    MatSlideToggleModule,
    MatSortModule,
    MatTableModule,
    MatTooltipModule
  ],
  templateUrl: './products-list.html',
  styleUrl: './products-list.scss'
})
export class ProductsListComponent implements AfterViewInit {
  private readonly fb = inject(FormBuilder);
  private readonly productService = inject(ProductService);
  private readonly categoryService = inject(CategoryService);
  private readonly cart = inject(CartService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly auth = inject(AuthService);

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  protected readonly loading = signal(false);
  protected readonly products = signal<Product[]>([]);
  protected readonly totalElements = signal(0);
  protected readonly categories = signal<Category[]>([]);

  /** Columnas visibles según el rol (los botones de acción sólo para admin). */
  protected readonly displayedColumns = signal<string[]>([]);

  protected readonly filterForm = this.fb.nonNullable.group({
    name: [''],
    categoryId: [null as number | null],
    minPrice: [null as number | null],
    maxPrice: [null as number | null],
    onlyActive: [true]
  });

  private paging: PageQuery = { page: 0, size: 10, sort: 'name,asc' };

  ngOnInit(): void {
    // La columna "actions" se muestra siempre: contiene "añadir al carrito"
    // para todos y, adicionalmente, editar/eliminar para admin (condicionado
    // dentro de la celda con @if auth.isAdmin()).
    this.displayedColumns.set(
      ['sku', 'name', 'category', 'price', 'stock', 'active', 'actions']
    );

    this.categoryService.list().subscribe({
      next: (cats) => this.categories.set(cats),
      error: () => this.snackBar.open('No se pudieron cargar las categorías', 'Cerrar', { duration: 4000 })
    });

    // Reactividad: cualquier cambio en el formulario reinicia la página y busca.
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
    const filters = this.filterForm.getRawValue();
    const criteria: ProductSearchCriteria = {
      name: filters.name || undefined,
      categoryId: filters.categoryId ?? undefined,
      minPrice: filters.minPrice ?? undefined,
      maxPrice: filters.maxPrice ?? undefined,
      active: filters.onlyActive ? true : undefined
    };
    this.productService.search(criteria, this.paging).subscribe({
      next: (page) => {
        this.products.set(page.content);
        this.totalElements.set(page.page.totalElements);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        const apiError = err.error as ApiError | undefined;
        this.snackBar.open(
          apiError?.message ?? 'Error al cargar productos',
          'Cerrar',
          { duration: 4000 }
        );
      }
    });
  }

  onPage(event: PageEvent): void {
    this.paging = { ...this.paging, page: event.pageIndex, size: event.pageSize };
    this.reload();
  }

  onSort(sortState: Sort): void {
    this.paging = {
      ...this.paging,
      page: 0,
      sort: sortState.direction ? `${sortState.active},${sortState.direction}` : undefined
    };
    if (this.paginator) this.paginator.firstPage();
    this.reload();
  }

  clearFilters(): void {
    this.filterForm.reset({
      name: '',
      categoryId: null,
      minPrice: null,
      maxPrice: null,
      onlyActive: true
    });
  }

  openCreate(): void {
    const ref = this.dialog.open<ProductFormDialogComponent, ProductFormDialogData, Product>(
      ProductFormDialogComponent,
      { data: { product: null, categories: this.categories() }, width: '520px' }
    );
    ref.afterClosed().subscribe((created) => {
      if (created) this.reload();
    });
  }

  openEdit(product: Product): void {
    const ref = this.dialog.open<ProductFormDialogComponent, ProductFormDialogData, Product>(
      ProductFormDialogComponent,
      { data: { product, categories: this.categories() }, width: '520px' }
    );
    ref.afterClosed().subscribe((updated) => {
      if (updated) this.reload();
    });
  }

  confirmDelete(product: Product): void {
    const ref = this.dialog.open<ConfirmDialogComponent, ConfirmDialogData, boolean>(
      ConfirmDialogComponent,
      {
        data: {
          title: 'Eliminar producto',
          message: `¿Seguro que deseas eliminar el producto "${product.name}" (SKU ${product.sku})? Esta acción no se puede deshacer.`,
          confirmLabel: 'Eliminar',
          color: 'warn',
          icon: 'delete_forever'
        }
      }
    );
    ref.afterClosed().subscribe((confirmed) => {
      if (!confirmed) return;
      this.productService.delete(product.id).subscribe({
        next: () => {
          this.snackBar.open('Producto eliminado', 'Cerrar', { duration: 3000 });
          this.reload();
        },
        error: (err: HttpErrorResponse) => {
          const apiError = err.error as ApiError | undefined;
          this.snackBar.open(
            apiError?.message ?? 'No se pudo eliminar el producto',
            'Cerrar',
            { duration: 4000 }
          );
        }
      });
    });
  }

  // Placeholder para Fase 4.4 — hoy sólo muestra un mensaje.
  addToCart(product: Product): void {
    const added = this.cart.add(product, 1);
    if (added) {
      this.snackBar.open(`"${product.name}" añadido al carrito`, 'Cerrar', {
        duration: 2500
      });
    } else {
      this.snackBar.open(
        `No hay más stock disponible de "${product.name}"`,
        'Cerrar',
        { duration: 3000 }
      );
    }
  }
}
