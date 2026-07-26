import { CommonModule } from '@angular/common';
import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { filter, map } from 'rxjs/operators';

import { AuthService } from '../core/services/auth.service';
import { Role } from '../core/models/role.model';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  roles?: Role[];
}

/**
 * Shell principal de la aplicación autenticada.
 *
 * <p>Estructura:
 * <ul>
 *   <li>{@code <mat-toolbar>} superior con marca, botón hamburguesa y menú usuario.</li>
 *   <li>{@code <mat-sidenav>} lateral con navegación filtrada por rol.</li>
 *   <li>{@code <mat-sidenav-content>} donde se renderiza la ruta hija.</li>
 * </ul>
 *
 * <p>El sidenav es <em>side</em> (fijo, empuja el contenido) en desktop
 * y <em>over</em> (overlay, se cierra al navegar) en móvil.
 */
@Component({
  selector: 'app-layout',
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatSidenavModule,
    MatToolbarModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    MatDividerModule
  ],
  templateUrl: './layout.html',
  styleUrl: './layout.scss'
})
export class LayoutComponent {
  private readonly breakpoint = inject(BreakpointObserver);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly auth = inject(AuthService);

  /** Signal que se recalcula cuando cambia el tamaño de pantalla. */
  protected readonly isMobile = signal(false);

  /** Estado abierto/cerrado del sidenav. */
  protected readonly sidenavOpen = signal(true);

  /**
   * Todos los items del menú lateral. El {@code roles} opcional filtra:
   * si es undefined, el item es visible para todos los autenticados;
   * si es un array, sólo se muestra cuando el usuario tiene alguno de esos roles.
   */
  private readonly allNavItems: NavItem[] = [
    { label: 'Inicio', icon: 'home', route: '/' },
    { label: 'Productos', icon: 'inventory_2', route: '/products' },
    { label: 'Mis órdenes', icon: 'receipt_long', route: '/orders' },
    { label: 'Reportes', icon: 'bar_chart', route: '/reports', roles: ['ADMIN'] },
    { label: 'Usuarios', icon: 'group', route: '/admin/users', roles: ['ADMIN'] },
    { label: 'Categorías', icon: 'category', route: '/admin/categories', roles: ['ADMIN'] },
    { label: 'Ventanas de descuento', icon: 'local_offer', route: '/admin/discount-windows', roles: ['ADMIN'] },
    { label: 'Auditoría', icon: 'shield', route: '/admin/audit', roles: ['ADMIN'] }
  ];

  /** Items filtrados por el rol del usuario actual. */
  protected readonly navItems = computed<NavItem[]>(() =>
    this.allNavItems.filter(
      (item) => !item.roles || this.auth.hasRole(...item.roles)
    )
  );

  constructor() {
    // Observamos el breakpoint móvil (Handset).
    this.breakpoint
      .observe([Breakpoints.Handset])
      .pipe(
        map((result) => result.matches),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((matches) => {
        this.isMobile.set(matches);
        this.sidenavOpen.set(!matches);
      });

    // Cerrar sidenav automáticamente al navegar en móvil.
    this.router.events
      .pipe(
        filter((e) => e instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        if (this.isMobile()) {
          this.sidenavOpen.set(false);
        }
      });
  }

  toggleSidenav(): void {
    this.sidenavOpen.set(!this.sidenavOpen());
  }

  logout(): void {
    this.auth.logout();
  }
}
