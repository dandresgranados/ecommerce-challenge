/** Espejo del {@code OrderStatus} del backend. */
export type OrderStatus = 'CREATED' | 'PAID' | 'CANCELED';

/** Payload de una línea al crear una orden. */
export interface OrderLineRequest {
  productId: number;
  quantity: number;
}

/** Payload de {@code POST /api/orders}. */
export interface CreateOrderRequest {
  items: OrderLineRequest[];
  randomOrder: boolean;
}

/** Línea de una orden en la respuesta (snapshot). */
export interface OrderLineResponse {
  id: number;
  productId: number;
  productName: string;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
}

/**
 * Desglose de descuentos aplicados. Todas las tasas son fracciones
 * (0.10 = 10 %). {@code totalRate} está capado en el backend a 0.95.
 */
export interface DiscountBreakdown {
  globalRate: number;
  randomRate: number;
  loyaltyRate: number;
  totalRate: number;
}

/** Respuesta pública de una orden. */
export interface Order {
  id: number;
  orderNumber: string;
  userId: number;
  username: string;
  status: OrderStatus;
  randomOrder: boolean;
  subtotal: number;
  discountRate: number;
  total: number;
  discountBreakdown: DiscountBreakdown;
  items: OrderLineResponse[];
  createdAt: string;
  updatedAt: string;
}
