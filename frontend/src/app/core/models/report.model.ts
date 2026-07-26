/** Fila del reporte "Top N productos más vendidos". */
export interface TopSellingProduct {
  productId: number;
  sku: string;
  name: string;
  totalSold: number;
}

/** Fila del reporte "Top N clientes frecuentes". */
export interface FrequentCustomer {
  userId: number;
  username: string;
  fullName: string | null;
  email: string;
  orderCount: number;
  totalSpent: number;
}
