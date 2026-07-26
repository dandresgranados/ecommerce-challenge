import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/**
 * Componente raíz. Su única responsabilidad es renderizar el
 * {@code <router-outlet />} principal. Todo el chrome (toolbar,
 * sidenav, menú de usuario) vive en {@code LayoutComponent} para
 * poder aislar las páginas públicas de auth (login/register).
 */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  template: '<router-outlet />',
  styles: [':host { display: block; min-height: 100vh; }']
})
export class App {}
