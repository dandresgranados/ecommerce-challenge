import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';

import { ReportService } from '../../../core/services/report.service';
import { Product } from '../../../core/models/product.model';
import {
  FrequentCustomer,
  TopSellingProduct
} from '../../../core/models/report.model';
import { ApiError } from '../../../core/models/api-error.model';

/**
 * Dashboard con los 3 reportes gerenciales que exige el reto:
 * <ol>
 *   <li>KPI de productos activos.</li>
 *   <li>Top N productos más vendidos (con barras de progreso relativas).</li>
 *   <li>Top N clientes frecuentes (con órdenes y gasto total).</li>
 * </ol>
 *
 * <p>El selector de {@code limit} recarga los 2 reportes tabulares (el
 * KPI de activos no depende del límite).
 */
@Component({
  selector: 'app-reports-dashboard',
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatProgressBarModule,
    MatSelectModule,
    MatTableModule
  ],
  templateUrl: './reports-dashboard.html',
  styleUrl: './reports-dashboard.scss'
})
export class ReportsDashboardComponent {
  private readonly reportService = inject(ReportService);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly loadingActive = signal(false);
  protected readonly loadingTop = signal(false);
  protected readonly loadingCustomers = signal(false);

  protected readonly activeProducts = signal<Product[]>([]);
  protected readonly topProducts = signal<TopSellingProduct[]>([]);
  protected readonly customers = signal<FrequentCustomer[]>([]);

  protected readonly limit = new FormControl<number>(5, { nonNullable: true });

  /** Máximo de ventas — usado para dimensionar las barras del top vendidos. */
  protected readonly maxSold = computed(() =>
    this.topProducts().reduce((max, p) => Math.max(max, p.totalSold), 0)
  );

  /** Máximo de gasto — usado para las barras del top de clientes. */
  protected readonly maxSpent = computed(() =>
    this.customers().reduce((max, c) => Math.max(max, c.totalSpent), 0)
  );

  protected readonly topColumns = ['rank', 'product', 'totalSold', 'bar'];
  protected readonly customerColumns = ['rank', 'customer', 'orderCount', 'totalSpent'];

  ngOnInit(): void {
    this.reloadAll();
    // Cuando cambia el selector, recargamos los dos rankings.
    this.limit.valueChanges.subscribe(() => {
      this.loadTopSelling();
      this.loadCustomers();
    });
  }

  reloadAll(): void {
    this.loadActive();
    this.loadTopSelling();
    this.loadCustomers();
  }

  private loadActive(): void {
    this.loadingActive.set(true);
    this.reportService.activeProducts().subscribe({
      next: (list) => {
        this.activeProducts.set(list);
        this.loadingActive.set(false);
      },
      error: (err) => this.handleError(err, 'productos activos', this.loadingActive)
    });
  }

  private loadTopSelling(): void {
    this.loadingTop.set(true);
    this.reportService.topSellingProducts(this.limit.value).subscribe({
      next: (list) => {
        this.topProducts.set(list);
        this.loadingTop.set(false);
      },
      error: (err) => this.handleError(err, 'top vendidos', this.loadingTop)
    });
  }

  private loadCustomers(): void {
    this.loadingCustomers.set(true);
    this.reportService.frequentCustomers(this.limit.value).subscribe({
      next: (list) => {
        this.customers.set(list);
        this.loadingCustomers.set(false);
      },
      error: (err) => this.handleError(err, 'clientes frecuentes', this.loadingCustomers)
    });
  }

  private handleError(
    err: HttpErrorResponse,
    label: string,
    loadingSignal: { set: (v: boolean) => void }
  ): void {
    loadingSignal.set(false);
    const apiError = err.error as ApiError | undefined;
    this.snackBar.open(
      apiError?.message ?? `No se pudo cargar el reporte de ${label}`,
      'Cerrar',
      { duration: 4000 }
    );
  }

  /** Ancho de la barra en %, relativo al máximo. Al menos 4 % para visibilidad. */
  protected barWidth(value: number, max: number): number {
    if (max === 0) return 0;
    return Math.max(4, (value / max) * 100);
  }
}
