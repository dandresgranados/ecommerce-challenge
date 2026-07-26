import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';

import { AuthService } from '../../../core/services/auth.service';
import { ApiError } from '../../../core/models/api-error.model';

/**
 * Formulario de registro público. Crea un usuario con rol USER en el backend
 * ({@code POST /api/auth/register}) y autentica automáticamente al usuario.
 */
@Component({
  selector: 'app-register',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './register.html',
  styleUrl: './register.scss'
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly loading = signal(false);
  protected readonly hidePassword = signal(true);

  protected readonly form = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(64)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(128)]],
    fullName: ['', [Validators.maxLength(128)]],
    password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(100)]]
  });

  onSubmit(): void {
    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    const { username, email, password, fullName } = this.form.getRawValue();
    this.auth
      .register({
        username,
        email,
        password,
        fullName: fullName?.trim() || undefined
      })
      .subscribe({
        next: () => {
          this.snackBar.open('¡Bienvenido! Tu cuenta se creó correctamente.', 'Cerrar', {
            duration: 4000
          });
          void this.router.navigateByUrl('/');
        },
        error: (err: HttpErrorResponse) => {
          this.loading.set(false);
          const apiError = err.error as ApiError | undefined;
          const message =
            apiError?.fieldErrors && Object.values(apiError.fieldErrors)[0]
              ? Object.values(apiError.fieldErrors)[0]
              : (apiError?.message ?? 'No se pudo registrar');
          this.snackBar.open(message, 'Cerrar', { duration: 5000 });
        }
      });
  }
}
