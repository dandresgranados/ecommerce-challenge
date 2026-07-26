export interface Product {
  id: number;
  sku: string;
  name: string;
  description: string | null;
  price: number;
  active: boolean;
  categoryId: number;
  categoryName: string;
  stock: number | null;
  createdAt: string;
  updatedAt: string;
}

/** Payload para {@code POST /api/products}. */
export interface ProductRequest {
  sku: string;
  name: string;
  description?: string;
  price: number;
  categoryId: number;
  active?: boolean;
  initialStock?: number;
  minStock?: number;
}

/** Payload para {@code PUT /api/products/:id}. Todos los campos son opcionales. */
export interface ProductUpdateRequest {
  name?: string;
  description?: string;
  price?: number;
  categoryId?: number;
  active?: boolean;
}

/** Filtros de búsqueda enviados a {@code GET /api/products?...} */
export interface ProductSearchCriteria {
  name?: string;
  categoryId?: number;
  minPrice?: number;
  maxPrice?: number;
  active?: boolean;
}
