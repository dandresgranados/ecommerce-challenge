import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { ReportService } from './report.service';
import { environment } from '../../../environments/environment';

describe('ReportService', () => {
  let service: ReportService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ReportService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('GET /reports/products/active', () => {
    service.activeProducts().subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/reports/products/active`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('GET /reports/products/top-selling with default limit=5', () => {
    service.topSellingProducts().subscribe();
    const req = httpMock.expectOne(
      (r) => r.url === `${environment.apiUrl}/reports/products/top-selling`,
    );
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('limit')).toBe('5');
    req.flush([]);
  });

  it('GET /reports/products/top-selling with custom limit', () => {
    service.topSellingProducts(20).subscribe();
    const req = httpMock.expectOne(
      (r) => r.url === `${environment.apiUrl}/reports/products/top-selling`,
    );
    expect(req.request.params.get('limit')).toBe('20');
    req.flush([]);
  });

  it('GET /reports/customers/frequent with default limit=5', () => {
    service.frequentCustomers().subscribe();
    const req = httpMock.expectOne(
      (r) => r.url === `${environment.apiUrl}/reports/customers/frequent`,
    );
    expect(req.request.params.get('limit')).toBe('5');
    req.flush([]);
  });
});
