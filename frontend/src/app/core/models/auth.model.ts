import { Role } from './role.model';
import { User } from './user.model';

/** Payload de {@code POST /api/auth/login}. */
export interface LoginRequest {
  username: string;
  password: string;
}

/** Payload de {@code POST /api/auth/register}. */
export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  fullName?: string;
}

/** Respuesta de {@code POST /api/auth/login} y {@code /api/auth/register}. */
export interface AuthResponse {
  token: string;
  tokenType: string;
  expiresInMs: number;
  user: User;
  roles: Role[];
}
