import { Injectable, computed, signal } from '@angular/core';

import { Product } from '../models/product.model';

/**
 * Línea del carrito local (no persistida en el backend).
 * Se guarda un snapshot del precio y nombre para poder mostrar el total
 * sin re-consultar el producto cada vez.
 */
export interface CartItem {
  productId: number;
  sku: string;
  name: string;
  unitPrice: number;
  quantity: number;
  /** Stock disponible al momento de añadir; se usa para no sobrepasar. */
  maxStock: number;
}

const STORAGE_KEY = 'cart_items';

/**
 * Servicio singleton del carrito de compras. Todo el estado vive en un
 * {@link signal}, se persiste en {@link Storage.localStorage} para
 * sobrevivir a un F5, y se sincroniza automáticamente vía {@code effect}
 * (implícito al hacer {@code items.set(...)}).
 *
 * <p>El carrito NO se envía al backend en cada cambio. Solo cuando el
 * usuario confirma la orden se hace {@code POST /api/orders}.
 */
@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly _items = signal<CartItem[]>(this.readFromStorage());

  /** Items actuales, readonly desde fuera. */
  readonly items = this._items.asReadonly();

  /** Suma de cantidades — para el badge del toolbar. */
  readonly itemCount = computed(() => this._items().reduce((acc, item) => acc + item.quantity, 0));

  /** Suma monetaria sin descuentos aplicados (los descuentos los calcula el backend). */
  readonly subtotal = computed(() =>
    this._items().reduce((acc, item) => acc + item.unitPrice * item.quantity, 0),
  );

  /** {@code true} si el carrito no tiene ninguna línea. */
  readonly isEmpty = computed(() => this._items().length === 0);

  /**
   * Añade un producto o incrementa su cantidad si ya está.
   * Devuelve {@code true} si se pudo añadir; {@code false} si no queda stock.
   */
  add(product: Product, quantity = 1): boolean {
    const items = this._items();
    const existing = items.find((it) => it.productId === product.id);
    const stock = product.stock ?? 0;

    if (existing) {
      const newQty = existing.quantity + quantity;
      if (newQty > stock) return false;
      this._items.set(
        items.map((it) => (it.productId === product.id ? { ...it, quantity: newQty } : it)),
      );
    } else {
      if (quantity > stock) return false;
      this._items.set([
        ...items,
        {
          productId: product.id,
          sku: product.sku,
          name: product.name,
          unitPrice: product.price,
          quantity,
          maxStock: stock,
        },
      ]);
    }
    this.persist();
    return true;
  }

  /** Cambia la cantidad de un producto ya en el carrito (min 1, max stock). */
  updateQuantity(productId: number, quantity: number): void {
    if (quantity < 1) {
      this.remove(productId);
      return;
    }
    this._items.set(
      this._items().map((it) =>
        it.productId === productId ? { ...it, quantity: Math.min(quantity, it.maxStock) } : it,
      ),
    );
    this.persist();
  }

  /** Elimina una línea completa del carrito. */
  remove(productId: number): void {
    this._items.set(this._items().filter((it) => it.productId !== productId));
    this.persist();
  }

  /** Vacía el carrito (tras crear una orden o al hacer logout). */
  clear(): void {
    this._items.set([]);
    this.persist();
  }

  // ---------- Persistencia ----------

  private persist(): void {
    if (typeof localStorage === 'undefined') return;
    localStorage.setItem(STORAGE_KEY, JSON.stringify(this._items()));
  }

  private readFromStorage(): CartItem[] {
    if (typeof localStorage === 'undefined') return [];
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return [];
    try {
      return JSON.parse(raw) as CartItem[];
    } catch {
      localStorage.removeItem(STORAGE_KEY);
      return [];
    }
  }
}
