import { Role } from './role.model';

/**
 * Representación de un usuario — refleja el {@code UserResponse} del backend.
 */
export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string | null;
  active: boolean;
  roles: Role[];
  createdAt: string;
  updatedAt: string;
}

/** Payload de {@code POST /api/users} — creación por admin (con roles). */
export interface UserCreateRequest {
  username: string;
  email: string;
  password: string;
  fullName?: string;
  roles: Role[];
}

/** Payload de {@code PUT /api/users/:id} — actualización parcial. */
export interface UserUpdateRequest {
  email?: string;
  fullName?: string;
  active?: boolean;
  roles?: Role[];
}

/** Payload de {@code POST /api/users/:id/password}. */
export interface PasswordChangeRequest {
  newPassword: string;
}
