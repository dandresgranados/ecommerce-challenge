import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { ProductService } from './product.service';
import { environment } from '../../../environments/environment';
import { PagedResponse } from '../models/page.model';
import { Product } from '../models/product.model';

const fakeProduct: Product = {
  id: 1,
  sku: 'TEST-001',
  name: 'Teclado mecánico',
  description: 'Switches azules',
  price: 89.5,
  active: true,
  categoryId: 1,
  categoryName: 'Electrónica',
  stock: 20,
  createdAt: '2026-01-01T00:00:00Z',
  updatedAt: '2026-01-01T00:00:00Z',
};

const fakePage: PagedResponse<Product> = {
  content: [fakeProduct],
  page: {
    size: 10,
    number: 0,
    totalElements: 1,
    totalPages: 1,
  },
};

describe('ProductService', () => {
  let service: ProductService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ProductService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should call GET /products with no params when no filters', () => {
    service.search().subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/products`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.keys().length).toBe(0);
    req.flush(fakePage);
  });

  it('should include all provided filters as query params', () => {
    service
      .search(
        { name: '  teclado ', categoryId: 2, minPrice: 10, maxPrice: 100, active: true },
        { page: 1, size: 20, sort: 'price,desc' },
      )
      .subscribe();

    const req = httpMock.expectOne((r) => r.url === `${environment.apiUrl}/products`);
    expect(req.request.params.get('name')).toBe('teclado');
    expect(req.request.params.get('categoryId')).toBe('2');
    expect(req.request.params.get('minPrice')).toBe('10');
    expect(req.request.params.get('maxPrice')).toBe('100');
    expect(req.request.params.get('active')).toBe('true');
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('size')).toBe('20');
    expect(req.request.params.get('sort')).toBe('price,desc');
    req.flush(fakePage);
  });

  it('should skip empty name (only whitespace)', () => {
    service.search({ name: '   ' }).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/products`);
    expect(req.request.params.has('name')).toBe(false);
    req.flush(fakePage);
  });

  it('should POST to /products on create', () => {
    service
      .create({
        sku: 'NEW-1',
        name: 'Nuevo',
        price: 10,
        categoryId: 1,
        initialStock: 5,
        minStock: 1,
      })
      .subscribe((p) => expect(p.id).toBe(1));

    const req = httpMock.expectOne(`${environment.apiUrl}/products`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body.sku).toBe('NEW-1');
    req.flush(fakeProduct);
  });

  it('should PUT to /products/:id on update', () => {
    service.update(42, { price: 99 }).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/products/42`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body.price).toBe(99);
    req.flush(fakeProduct);
  });

  it('should DELETE /products/:id', () => {
    service.delete(7).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/products/7`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
