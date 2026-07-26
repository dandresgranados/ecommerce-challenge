/**
 * Estructura estándar de error que devuelve el backend
 * (GlobalExceptionHandler → ApiErrorResponse).
 */
export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path?: string;
  fieldErrors?: Record<string, string>;
}
