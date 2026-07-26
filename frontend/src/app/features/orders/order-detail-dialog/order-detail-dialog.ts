import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle
} from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';

import { Order } from '../../../core/models/order.model';

/**
 * Diálogo con el detalle completo de una orden: líneas, subtotal,
 * desglose de cada descuento (global / random / fidelidad) y total final.
 */
@Component({
  selector: 'app-order-detail-dialog',
  imports: [
    CommonModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatButtonModule,
    MatChipsModule,
    MatDividerModule,
    MatIconModule,
    MatTableModule
  ],
  templateUrl: './order-detail-dialog.html',
  styleUrl: './order-detail-dialog.scss'
})
export class OrderDetailDialogComponent {
  protected readonly order = inject<Order>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<OrderDetailDialogComponent>);

  protected readonly displayedColumns = ['productName', 'unitPrice', 'quantity', 'lineTotal'];

  protected readonly statusColor: Record<string, 'primary' | 'accent' | 'warn'> = {
    CREATED: 'accent',
    PAID: 'primary',
    CANCELED: 'warn'
  };

  close(): void {
    this.dialogRef.close();
  }
}
