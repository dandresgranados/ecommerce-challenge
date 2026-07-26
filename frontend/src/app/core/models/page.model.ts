/**
 * Estructura que devuelve Spring Boot 4 cuando el endpoint retorna
 * {@code Page<T>}. En Spring Boot 3.3+, los metadatos de paginación se
 * empaquetan en un objeto {@code page} anidado (en versiones anteriores
 * estaban en la raíz).
 *
 * @see backend/src/main/java/.../ProductController#search
 */
export interface PagedResponse<T> {
  content: T[];
  page: {
    size: number;
    number: number; // índice de la página actual (0-based)
    totalElements: number;
    totalPages: number;
  };
}

