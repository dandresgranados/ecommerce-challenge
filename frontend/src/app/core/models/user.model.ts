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
