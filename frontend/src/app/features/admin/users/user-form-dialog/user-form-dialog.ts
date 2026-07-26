import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle
} from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ApiError } from '../../../../core/models/api-error.model';
import { Role } from '../../../../core/models/role.model';
import {
  User,
  UserCreateRequest,
  UserUpdateRequest
} from '../../../../core/models/user.model';
import { UserService } from '../../../../core/services/user.service';

export interface UserFormDialogData {
  user: User | null;
}

/**
 * Diálogo de creación o edición de usuarios (solo admin).
 *
 * <p>En modo crear pide contraseña; en modo editar el username queda
 * bloqueado y la contraseña se cambia en un diálogo aparte.
 */
@Component({
  selector: 'app-user-form-dialog',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatButtonModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSlideToggleModule
  ],
  templateUrl: './user-form-dialog.html',
  styleUrl: './user-form-dialog.scss'
})
export class UserFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly userService = inject(UserService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialogRef = inject(
    MatDialogRef<UserFormDialogComponent, User | undefined>
  );

  protected readonly data = inject<UserFormDialogData>(MAT_DIALOG_DATA);
  protected readonly loading = signal(false);
  protected readonly hidePassword = signal(true);
  protected readonly isEdit = this.data.user !== null;

  protected readonly form = this.fb.nonNullable.group({
    username: [
      { value: this.data.user?.username ?? '', disabled: this.isEdit },
      [Validators.required, Validators.minLength(3), Validators.maxLength(64)]
    ],
    email: [
      this.data.user?.email ?? '',
      [Validators.required, Validators.email, Validators.maxLength(128)]
    ],
    password: ['', this.isEdit ? [] : [Validators.required, Validators.minLength(6)]],
    fullName: [this.data.user?.fullName ?? '', [Validators.maxLength(128)]],
    active: [this.data.user?.active ?? true],
    roleAdmin: [this.data.user?.roles.includes('ADMIN') ?? false],
    roleUser: [this.data.user?.roles.includes('USER') ?? true]
  });

  onSubmit(): void {
    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      return;
    }
    // Al menos un rol.
    if (!this.form.controls.roleAdmin.value && !this.form.controls.roleUser.value) {
      this.snackBar.open('Debes asignar al menos un rol', 'Cerrar', { duration: 3000 });
      return;
    }

    this.loading.set(true);
    const values = this.form.getRawValue();
    const roles: Role[] = [];
    if (values.roleAdmin) roles.push('ADMIN');
    if (values.roleUser) roles.push('USER');

    const request$ = this.isEdit
      ? this.userService.update(this.data.user!.id, {
          email: values.email,
          fullName: values.fullName || undefined,
          active: values.active,
          roles
        } satisfies UserUpdateRequest)
      : this.userService.create({
          username: values.username,
          email: values.email,
          password: values.password,
          fullName: values.fullName || undefined,
          roles
        } satisfies UserCreateRequest);

    request$.subscribe({
      next: (user) => {
        this.snackBar.open(
          this.isEdit ? 'Usuario actualizado' : 'Usuario creado',
          'Cerrar',
          { duration: 3000 }
        );
        this.dialogRef.close(user);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        const apiError = err.error as ApiError | undefined;
        const message =
          (apiError?.fieldErrors && Object.values(apiError.fieldErrors)[0]) ??
          apiError?.message ??
          'No se pudo guardar el usuario';
        this.snackBar.open(message, 'Cerrar', { duration: 5000 });
      }
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
