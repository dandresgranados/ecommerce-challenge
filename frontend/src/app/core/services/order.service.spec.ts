import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { OrderService } from './order.service';
import { environment } from '../../../environments/environment';
import { Order, OrderStatus } from '../models/order.model';

const fakeOrder: Order = {
  id: 1,
  orderNumber: 'ORD-2026-0001',
  userId: 2,
  username: 'user',
  status: 'CREATED' as OrderStatus,
  randomOrder: false,
  subtotal: 100,
  discountRate: 0.1,
  total: 90,
  discountBreakdown: { globalRate: 0.1, randomRate: 0, loyaltyRate: 0, totalRate: 0.1 },
  items: [],
  createdAt: '',
  updatedAt: ''
};

describe('OrderService', () => {
  let service: OrderService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(OrderService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('POST /orders on create', () => {
    service
      .create({ items: [{ productId: 1, quantity: 2 }], randomOrder: true })
      .subscribe((o) => expect(o.orderNumber).toBe('ORD-2026-0001'));
    const req = httpMock.expectOne(`${environment.apiUrl}/orders`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body.randomOrder).toBe(true);
    expect(req.request.body.items.length).toBe(1);
    req.flush(fakeOrder);
  });

  it('GET /orders/my with paging', () => {
    service.myOrders({ page: 0, size: 5, sort: 'createdAt,desc' }).subscribe();
    const req = httpMock.expectOne((r) => r.url === `${environment.apiUrl}/orders/my`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('5');
    expect(req.request.params.get('sort')).toBe('createdAt,desc');
    req.flush({ content: [fakeOrder], page: { size: 5, number: 0, totalElements: 1, totalPages: 1 } });
  });

  it('POST /orders/:id/pay', () => {
    service.pay(7).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/orders/7/pay`);
    expect(req.request.method).toBe('POST');
    req.flush({ ...fakeOrder, id: 7, status: 'PAID' });
  });

  it('POST /orders/:id/cancel', () => {
    service.cancel(9).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/orders/9/cancel`);
    expect(req.request.method).toBe('POST');
    req.flush({ ...fakeOrder, id: 9, status: 'CANCELED' });
  });

  it('GET /orders/:id', () => {
    service.getById(3).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/orders/3`);
    expect(req.request.method).toBe('GET');
    req.flush(fakeOrder);
  });
});
