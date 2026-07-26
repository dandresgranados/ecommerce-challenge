import { Component, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

import { AuthService } from '../../core/services/auth.service';
import { Role } from '../../core/models/role.model';

interface HomeTile {
  title: string;
  description: string;
  icon: string;
  route: string;
  color: 'primary' | 'accent';
  roles?: Role[];
}

/**
 * Landing autenticada. Muestra un saludo con el usuario actual y tiles
 * navegables a las secciones principales, filtrados por rol.
 */
@Component({
  selector: 'app-home',
  imports: [CommonModule, RouterLink, MatCardModule, MatIconModule, MatButtonModule],
  templateUrl: './home.html',
  styleUrl: './home.scss'
})
export class HomeComponent {
  protected readonly auth = inject(AuthService);

  private readonly allTiles: HomeTile[] = [
    {
      title: 'Productos',
      description: 'Explora el catálogo y busca por nombre, categoría o precio',
      icon: 'inventory_2',
      route: '/products',
      color: 'primary'
    },
    {
      title: 'Mis órdenes',
      description: 'Crea nuevas órdenes y consulta tu historial de compras',
      icon: 'receipt_long',
      route: '/orders',
      color: 'primary'
    },
    {
      title: 'Reportes',
      description: 'Productos activos, top ventas y clientes frecuentes',
      icon: 'bar_chart',
      route: '/reports',
      color: 'accent',
      roles: ['ADMIN']
    },
    {
      title: 'Usuarios',
      description: 'Gestiona cuentas, roles y estado de los usuarios',
      icon: 'group',
      route: '/admin/users',
      color: 'accent',
      roles: ['ADMIN']
    },
    {
      title: 'Ventanas de descuento',
      description: 'Configura promociones GLOBAL 10 % y RANDOM 50 %',
      icon: 'local_offer',
      route: '/admin/discount-windows',
      color: 'accent',
      roles: ['ADMIN']
    },
    {
      title: 'Auditoría',
      description: 'Consulta todos los eventos del sistema',
      icon: 'shield',
      route: '/admin/audit',
      color: 'accent',
      roles: ['ADMIN']
    }
  ];

  protected readonly tiles = computed<HomeTile[]>(() =>
    this.allTiles.filter((t) => !t.roles || this.auth.hasRole(...t.roles))
  );
}
