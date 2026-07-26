import { TestBed } from '@angular/core/testing';

import { CartService } from './cart.service';
import { Product } from '../models/product.model';

const p1: Product = {
  id: 1,
  sku: 'ELEC-001',
  name: 'Auriculares',
  description: null,
  price: 149.99,
  active: true,
  categoryId: 1,
  categoryName: 'Electrónica',
  stock: 5,
  createdAt: '',
  updatedAt: ''
};

const p2: Product = {
  ...p1,
  id: 2,
  sku: 'LIB-001',
  name: 'Clean Code',
  price: 35,
  stock: 3
};

describe('CartService', () => {
  let service: CartService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
    service = TestBed.inject(CartService);
  });

  afterEach(() => localStorage.clear());

  it('starts empty', () => {
    expect(service.isEmpty()).toBe(true);
    expect(service.itemCount()).toBe(0);
    expect(service.subtotal()).toBe(0);
  });

  it('adds a product and updates count/subtotal', () => {
    expect(service.add(p1, 2)).toBe(true);
    expect(service.items().length).toBe(1);
    expect(service.itemCount()).toBe(2);
    expect(service.subtotal()).toBeCloseTo(299.98);
  });

  it('increments quantity when adding same product twice', () => {
    service.add(p1, 1);
    service.add(p1, 2);
    expect(service.items().length).toBe(1);
    expect(service.items()[0].quantity).toBe(3);
  });

  it('rejects adding beyond stock', () => {
    service.add(p1, 4);
    expect(service.add(p1, 2)).toBe(false); // 4+2 > 5
    expect(service.items()[0].quantity).toBe(4);
  });

  it('updateQuantity caps at maxStock', () => {
    service.add(p1, 1);
    service.updateQuantity(1, 999);
    expect(service.items()[0].quantity).toBe(5);
  });

  it('updateQuantity to 0 removes the item', () => {
    service.add(p1, 1);
    service.updateQuantity(1, 0);
    expect(service.isEmpty()).toBe(true);
  });

  it('remove drops a line', () => {
    service.add(p1, 1);
    service.add(p2, 1);
    service.remove(p1.id);
    expect(service.items().length).toBe(1);
    expect(service.items()[0].productId).toBe(p2.id);
  });

  it('clear empties everything and persists', () => {
    service.add(p1, 1);
    service.clear();
    expect(service.isEmpty()).toBe(true);
    expect(localStorage.getItem('cart_items')).toBe('[]');
  });

  it('restores state from localStorage on init', () => {
    localStorage.setItem(
      'cart_items',
      JSON.stringify([
        { productId: 1, sku: 'X', name: 'X', unitPrice: 10, quantity: 3, maxStock: 5 }
      ])
    );
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({});
    const restored = TestBed.inject(CartService);
    expect(restored.itemCount()).toBe(3);
    expect(restored.subtotal()).toBe(30);
  });
});
