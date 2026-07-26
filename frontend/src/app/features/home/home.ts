import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

import { AuthService } from '../../core/services/auth.service';

/**
 * Landing autenticada. Placeholder mientras se implementan las fases
 * 4.3+ (productos, carrito, reportes, etc.).
 */
@Component({
  selector: 'app-home',
  imports: [CommonModule, MatButtonModule, MatCardModule, MatIconModule],
  templateUrl: './home.html',
  styleUrl: './home.scss'
})
export class HomeComponent {
  protected readonly auth = inject(AuthService);
}
