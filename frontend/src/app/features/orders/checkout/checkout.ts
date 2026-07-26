import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';

import { CartService } from '../../../core/services/cart.service';
import { OrderService } from '../../../core/services/order.service';
import { ApiError } from '../../../core/models/api-error.model';
import { CreateOrderRequest } from '../../../core/models/order.model';

/**
 * Página del carrito y confirmación de la orden.
 *
 * <ul>
 *   <li>Lista los productos añadidos con controles +/– y eliminar.</li>
 *   <li>Toggle "Pedido aleatorio" que dispara el descuento del 50 %
 *       (si hay una ventana RANDOM activa en el backend).</li>
 *   <li>Al confirmar hace {@code POST /api/orders} y redirige a
 *       {@code /orders} con la orden creada resaltada.</li>
 * </ul>
 */
@Component({
  selector: 'app-checkout',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatDividerModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSlideToggleModule,
    MatTableModule,
    MatTooltipModule,
  ],
  templateUrl: './checkout.html',
  styleUrl: './checkout.scss',
})
export class CheckoutComponent {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly orderService = inject(OrderService);
  private readonly snackBar = inject(MatSnackBar);
  protected readonly cart = inject(CartService);

  protected readonly loading = signal(false);
  protected readonly displayedColumns = ['name', 'unitPrice', 'quantity', 'lineTotal', 'actions'];

  protected readonly form = this.fb.nonNullable.group({
    randomOrder: [false],
  });

  /** Total mostrado — el desglose final de descuentos lo calcula el backend. */
  protected readonly subtotal = this.cart.subtotal;

  /** Estimación pre-orden mostrando el 50 % si el usuario marca randomOrder. */
  protected readonly estimatedDiscountLabel = computed(() =>
    this.form.controls.randomOrder.value ? '~50 % (RANDOM)' : '—',
  );

  onQuantityChange(productId: number, event: Event): void {
    const value = Number((event.target as HTMLInputElement).value);
    if (!isNaN(value)) this.cart.updateQuantity(productId, value);
  }

  increment(productId: number, current: number, max: number): void {
    if (current < max) this.cart.updateQuantity(productId, current + 1);
  }

  decrement(productId: number, current: number): void {
    this.cart.updateQuantity(productId, current - 1);
  }

  remove(productId: number): void {
    this.cart.remove(productId);
  }

  clearCart(): void {
    this.cart.clear();
  }

  confirmOrder(): void {
    if (this.cart.isEmpty() || this.loading()) return;
    this.loading.set(true);

    const request: CreateOrderRequest = {
      items: this.cart.items().map((it) => ({
        productId: it.productId,
        quantity: it.quantity,
      })),
      randomOrder: this.form.controls.randomOrder.value,
    };

    this.orderService.create(request).subscribe({
      next: (order) => {
        this.cart.clear();
        this.snackBar.open(
          `Orden ${order.orderNumber} creada (total $${order.total.toFixed(2)})`,
          'Cerrar',
          { duration: 4000 },
        );
        void this.router.navigate(['/orders'], {
          queryParams: { highlight: order.id },
        });
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        const apiError = err.error as ApiError | undefined;
        this.snackBar.open(apiError?.message ?? 'No se pudo crear la orden', 'Cerrar', {
          duration: 5000,
        });
      },
    });
  }
}
