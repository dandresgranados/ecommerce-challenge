import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle,
} from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ApiError } from '../../../../core/models/api-error.model';
import { User } from '../../../../core/models/user.model';
import { UserService } from '../../../../core/services/user.service';

/**
 * Diálogo para que un admin cambie la contraseña de otro usuario.
 * El backend expone {@code POST /api/users/:id/password} con
 * {@code { newPassword }}.
 */
@Component({
  selector: 'app-user-password-dialog',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <h2 mat-dialog-title>
      <mat-icon>key</mat-icon>
      Cambiar contraseña de {{ data.username }}
    </h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="pwd-form">
        <mat-form-field appearance="outline">
          <mat-label>Nueva contraseña</mat-label>
          <input
            matInput
            [type]="hidePassword() ? 'password' : 'text'"
            formControlName="newPassword"
            autocomplete="new-password"
          />
          <button
            type="button"
            mat-icon-button
            matSuffix
            (click)="hidePassword.set(!hidePassword())"
          >
            <mat-icon>{{ hidePassword() ? 'visibility_off' : 'visibility' }}</mat-icon>
          </button>
          @if (
            form.controls.newPassword.hasError('required') && form.controls.newPassword.touched
          ) {
            <mat-error>Requerida</mat-error>
          }
          @if (
            form.controls.newPassword.hasError('minlength') && form.controls.newPassword.touched
          ) {
            <mat-error>Mínimo 6 caracteres</mat-error>
          }
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="cancel()" [disabled]="loading()">Cancelar</button>
      <button mat-flat-button color="primary" (click)="onSubmit()" [disabled]="loading()">
        @if (loading()) {
          <mat-spinner diameter="20"></mat-spinner>
        } @else {
          <ng-container>Cambiar contraseña</ng-container>
        }
      </button>
    </mat-dialog-actions>
  `,
  styles: [
    `
      h2[mat-dialog-title] {
        display: flex;
        align-items: center;
        gap: 0.5rem;
      }
      .pwd-form {
        min-width: 380px;
        padding-top: 0.5rem;
      }
    `,
  ],
})
export class UserPasswordDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly userService = inject(UserService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialogRef = inject(MatDialogRef<UserPasswordDialogComponent, boolean>);

  protected readonly data = inject<User>(MAT_DIALOG_DATA);
  protected readonly loading = signal(false);
  protected readonly hidePassword = signal(true);

  protected readonly form = this.fb.nonNullable.group({
    newPassword: ['', [Validators.required, Validators.minLength(6)]],
  });

  onSubmit(): void {
    if (this.form.invalid || this.loading()) return;
    this.loading.set(true);
    this.userService
      .changePassword(this.data.id, { newPassword: this.form.controls.newPassword.value })
      .subscribe({
        next: () => {
          this.snackBar.open(`Contraseña de ${this.data.username} actualizada`, 'Cerrar', {
            duration: 3000,
          });
          this.dialogRef.close(true);
        },
        error: (err: HttpErrorResponse) => {
          this.loading.set(false);
          const apiError = err.error as ApiError | undefined;
          this.snackBar.open(apiError?.message ?? 'No se pudo cambiar la contraseña', 'Cerrar', {
            duration: 5000,
          });
        },
      });
  }

  cancel(): void {
    this.dialogRef.close(false);
  }
}
