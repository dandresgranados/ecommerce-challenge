-- ============================================================
-- Datos semilla — solo perfil dev (application-dev.yml lo carga).
-- Se ejecuta DESPUÉS de que Hibernate cree las tablas.
-- ============================================================

INSERT INTO roles (id, name) VALUES (1, 'ADMIN');
INSERT INTO roles (id, name) VALUES (2, 'USER');
