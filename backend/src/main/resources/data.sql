-- ============================================================
-- Datos semilla — solo perfil dev (application-dev.yml lo carga).
-- Se ejecuta DESPUÉS de que Hibernate cree las tablas.
-- ============================================================

-- Roles del sistema
INSERT INTO roles (id, name) VALUES (1, 'ADMIN');
INSERT INTO roles (id, name) VALUES (2, 'USER');

-- Categorías de ejemplo
INSERT INTO categories (id, name, description, created_at, updated_at, created_by, updated_by)
VALUES (1, 'Electrónica', 'Dispositivos electrónicos y accesorios', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system');
INSERT INTO categories (id, name, description, created_at, updated_at, created_by, updated_by)
VALUES (2, 'Ropa', 'Prendas de vestir para hombre y mujer', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system');
INSERT INTO categories (id, name, description, created_at, updated_at, created_by, updated_by)
VALUES (3, 'Libros', 'Libros físicos y digitales', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system');

-- Productos de ejemplo
INSERT INTO products (id, sku, name, description, price, active, category_id, version, created_at, updated_at, created_by, updated_by)
VALUES (1, 'ELEC-001', 'Auriculares Bluetooth', 'Auriculares inalámbricos con cancelación de ruido', 149.9900, TRUE, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system');
INSERT INTO products (id, sku, name, description, price, active, category_id, version, created_at, updated_at, created_by, updated_by)
VALUES (2, 'ELEC-002', 'Teclado mecánico', 'Teclado gaming RGB con switches azules', 89.5000, TRUE, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system');
INSERT INTO products (id, sku, name, description, price, active, category_id, version, created_at, updated_at, created_by, updated_by)
VALUES (3, 'ROPA-001', 'Camiseta básica', 'Camiseta 100% algodón', 19.9900, TRUE, 2, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system');
INSERT INTO products (id, sku, name, description, price, active, category_id, version, created_at, updated_at, created_by, updated_by)
VALUES (4, 'LIB-001', 'Clean Code', 'Libro de Robert C. Martin', 35.0000, TRUE, 3, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system');
INSERT INTO products (id, sku, name, description, price, active, category_id, version, created_at, updated_at, created_by, updated_by)
VALUES (5, 'ELEC-003', 'Mouse antiguo', 'Producto descatalogado', 5.0000, FALSE, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system');

-- Inventario para cada producto
INSERT INTO inventory (id, product_id, quantity, min_stock, version, created_at, updated_at, created_by, updated_by)
VALUES (1, 1, 50, 10, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system');
INSERT INTO inventory (id, product_id, quantity, min_stock, version, created_at, updated_at, created_by, updated_by)
VALUES (2, 2, 30, 5, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system');
INSERT INTO inventory (id, product_id, quantity, min_stock, version, created_at, updated_at, created_by, updated_by)
VALUES (3, 3, 100, 20, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system');
INSERT INTO inventory (id, product_id, quantity, min_stock, version, created_at, updated_at, created_by, updated_by)
VALUES (4, 4, 15, 3, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system');
INSERT INTO inventory (id, product_id, quantity, min_stock, version, created_at, updated_at, created_by, updated_by)
VALUES (5, 5, 0, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system');

-- ============================================================
-- Reiniciar las secuencias auto-increment más allá de los IDs
-- semilla para evitar colisiones cuando Hibernate genere nuevos IDs
-- vía save(). Sintaxis compatible con PostgreSQL (H2 lo entiende
-- gracias a MODE=PostgreSQL).
-- ============================================================
ALTER TABLE roles       ALTER COLUMN id RESTART WITH 100;
ALTER TABLE categories  ALTER COLUMN id RESTART WITH 100;
ALTER TABLE products    ALTER COLUMN id RESTART WITH 100;
ALTER TABLE inventory   ALTER COLUMN id RESTART WITH 100;