import { Component, input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

/**
 * Placeholder reutilizable para secciones aún no implementadas.
 * Muestra un card con el título, la fase que la implementará y una descripción.
 */
@Component({
  selector: 'app-placeholder',
  imports: [MatCardModule, MatIconModule],
  template: `
    <mat-card class="placeholder-card">
      <mat-card-header>
        <mat-icon mat-card-avatar>construction</mat-icon>
        <mat-card-title>{{ title() }}</mat-card-title>
        <mat-card-subtitle>Próximamente · {{ phase() }}</mat-card-subtitle>
      </mat-card-header>
      <mat-card-content>
        <p>{{ description() }}</p>
      </mat-card-content>
    </mat-card>
  `,
  styles: [
    `
      :host {
        display: block;
        max-width: 800px;
        margin: 0 auto;
      }
      .placeholder-card {
        padding: 1.5rem;
      }
      mat-icon[mat-card-avatar] {
        font-size: 2.5rem;
        width: 2.5rem;
        height: 2.5rem;
        color: var(--mat-sys-primary);
      }
      p {
        line-height: 1.6;
        color: var(--mat-sys-on-surface-variant);
      }
    `
  ]
})
export class PlaceholderComponent {
  readonly title = input.required<string>();
  readonly phase = input.required<string>();
  readonly description = input.required<string>();
}
