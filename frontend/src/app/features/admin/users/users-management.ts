import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { AfterViewInit, Component, ViewChild, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';

import { PageQuery } from '../../../core/services/product.service';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';
import { ApiError } from '../../../core/models/api-error.model';
import { User } from '../../../core/models/user.model';
import {
  ConfirmDialogComponent,
  ConfirmDialogData,
} from '../../../shared/components/confirm-dialog.component';
import { UserFormDialogComponent, UserFormDialogData } from './user-form-dialog/user-form-dialog';
import { UserPasswordDialogComponent } from './user-password-dialog/user-password-dialog';

/**
 * Gestión de usuarios (ADMIN). CRUD + cambio de contraseña.
 *
 * <p>El admin no puede eliminarse a sí mismo — se detecta comparando con
 * {@code auth.currentUser()?.username}.
 */
@Component({
  selector: 'app-users-management',
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatIconModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatTableModule,
    MatTooltipModule,
  ],
  templateUrl: './users-management.html',
  styleUrl: './users-management.scss',
})
export class UsersManagementComponent implements AfterViewInit {
  private readonly userService = inject(UserService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  protected readonly auth = inject(AuthService);

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  protected readonly loading = signal(false);
  protected readonly users = signal<User[]>([]);
  protected readonly totalElements = signal(0);

  protected readonly displayedColumns = [
    'username',
    'email',
    'fullName',
    'roles',
    'active',
    'actions',
  ];

  private paging: PageQuery = { page: 0, size: 10, sort: 'username,asc' };

  ngAfterViewInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.userService.list(this.paging).subscribe({
      next: (page) => {
        this.users.set(page.content);
        this.totalElements.set(page.page.totalElements);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        const apiError = err.error as ApiError | undefined;
        this.snackBar.open(apiError?.message ?? 'Error al cargar usuarios', 'Cerrar', {
          duration: 4000,
        });
      },
    });
  }

  onPage(event: PageEvent): void {
    this.paging = { ...this.paging, page: event.pageIndex, size: event.pageSize };
    this.reload();
  }

  openCreate(): void {
    const ref = this.dialog.open<UserFormDialogComponent, UserFormDialogData, User>(
      UserFormDialogComponent,
      { data: { user: null }, width: '460px' },
    );
    ref.afterClosed().subscribe((u) => {
      if (u) this.reload();
    });
  }

  openEdit(user: User): void {
    const ref = this.dialog.open<UserFormDialogComponent, UserFormDialogData, User>(
      UserFormDialogComponent,
      { data: { user }, width: '460px' },
    );
    ref.afterClosed().subscribe((u) => {
      if (u) this.reload();
    });
  }

  openPasswordChange(user: User): void {
    this.dialog.open(UserPasswordDialogComponent, { data: user });
  }

  isSelf(user: User): boolean {
    return this.auth.currentUser()?.username === user.username;
  }

  confirmDelete(user: User): void {
    if (this.isSelf(user)) {
      this.snackBar.open('No puedes eliminar tu propio usuario', 'Cerrar', {
        duration: 3000,
      });
      return;
    }
    const ref = this.dialog.open<ConfirmDialogComponent, ConfirmDialogData, boolean>(
      ConfirmDialogComponent,
      {
        data: {
          title: 'Eliminar usuario',
          message: `¿Seguro que deseas eliminar (desactivar) al usuario "${user.username}"?`,
          confirmLabel: 'Eliminar',
          color: 'warn',
          icon: 'person_remove',
        },
      },
    );
    ref.afterClosed().subscribe((ok) => {
      if (!ok) return;
      this.userService.delete(user.id).subscribe({
        next: () => {
          this.snackBar.open('Usuario eliminado', 'Cerrar', { duration: 3000 });
          this.reload();
        },
        error: (err: HttpErrorResponse) => {
          const apiError = err.error as ApiError | undefined;
          this.snackBar.open(apiError?.message ?? 'No se pudo eliminar', 'Cerrar', {
            duration: 4000,
          });
        },
      });
    });
  }
}
