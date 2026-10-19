-- =====================================================================
-- Parcial Programación 4 - Gestión de Pedidos y Entregas
-- Script de base de datos PostgreSQL
--
-- Uso:
--   1) Crear la base:   CREATE DATABASE pedidos_db;
--   2) Conectarse a ella y ejecutar este script.
--
-- Nota: Spring Boot (ddl-auto=update) también puede crear estas tablas
-- a partir de las entidades JPA. Este script documenta el modelo y
-- carga datos de prueba. Las tablas usan IF NOT EXISTS para convivir
-- con Hibernate.
-- =====================================================================

-- ------------------------- TABLAS -------------------------

CREATE TABLE IF NOT EXISTS clientes (
    id        BIGSERIAL PRIMARY KEY,
    nombre    VARCHAR(100) NOT NULL,
    correo    VARCHAR(120) NOT NULL UNIQUE,
    telefono  VARCHAR(20)  NOT NULL
);

CREATE TABLE IF NOT EXISTS productos (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(100)   NOT NULL,
    precio      NUMERIC(10,2)  NOT NULL CHECK (precio > 0),
    existencia  INTEGER        NOT NULL CHECK (existencia >= 0)
);

CREATE TABLE IF NOT EXISTS pedidos (
    id          BIGSERIAL PRIMARY KEY,
    fecha       TIMESTAMP      NOT NULL DEFAULT NOW(),
    estado      VARCHAR(20)    NOT NULL DEFAULT 'PENDIENTE'
                CHECK (estado IN ('PENDIENTE','DESPACHADO','ENTREGADO','CANCELADO')),
    total       NUMERIC(12,2)  NOT NULL DEFAULT 0,
    cliente_id  BIGINT         NOT NULL REFERENCES clientes(id)          -- FK: muchos pedidos -> 1 cliente
);

CREATE TABLE IF NOT EXISTS detalles_pedido (
    id           BIGSERIAL PRIMARY KEY,
    cantidad     INTEGER        NOT NULL CHECK (cantidad > 0),
    subtotal     NUMERIC(12,2)  NOT NULL,
    pedido_id    BIGINT         NOT NULL REFERENCES pedidos(id) ON DELETE CASCADE, -- FK: muchos detalles -> 1 pedido
    producto_id  BIGINT         NOT NULL REFERENCES productos(id)                  -- FK: cada detalle -> 1 producto
);

CREATE TABLE IF NOT EXISTS repartidores (
    id        BIGSERIAL PRIMARY KEY,
    nombre    VARCHAR(100) NOT NULL,
    telefono  VARCHAR(20)  NOT NULL,
    activo    BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS entregas (
    id             BIGSERIAL PRIMARY KEY,
    fecha_salida   TIMESTAMP,
    fecha_entrega  TIMESTAMP,
    estado         VARCHAR(20) NOT NULL
                   CHECK (estado IN ('ASIGNADA','EN_CAMINO','ENTREGADA')),
    pedido_id      BIGINT NOT NULL UNIQUE REFERENCES pedidos(id),     -- FK UNIQUE: 1 pedido -> 1 entrega
    repartidor_id  BIGINT NOT NULL REFERENCES repartidores(id)        -- FK: muchas entregas -> 1 repartidor
);

-- ------------------------- DATOS DE PRUEBA -------------------------
-- Se insertan solo clientes, productos y repartidores.
-- Los pedidos conviene crearlos desde la API (POST /api/pedidos) para que
-- el Service calcule subtotal y total automáticamente.

INSERT INTO clientes (nombre, correo, telefono) VALUES
  ('Ana López',     'ana.lopez@correo.com',   '7012-3456'),
  ('Carlos Pérez',  'carlos.perez@correo.com','7123-4567'),
  ('María Gómez',   'maria.gomez@correo.com', '7234-5678')
ON CONFLICT (correo) DO NOTHING;

INSERT INTO productos (nombre, precio, existencia) VALUES
  ('Agua embotellada 600ml', 0.75, 500),
  ('Arroz 1 lb',             1.10, 300),
  ('Aceite 1 L',             3.50, 120),
  ('Azúcar 2 lb',            1.95, 200),
  ('Café molido 400g',       4.25,  80);

INSERT INTO repartidores (nombre, telefono, activo) VALUES
  ('Luis Hernández', '7345-6789', TRUE),
  ('José Martínez',  '7456-7890', TRUE),
  ('Pedro Ramírez',  '7567-8901', FALSE);   -- inactivo, para probar validación

-- ------------------------- CONSULTAS ÚTILES PARA LA DEFENSA -------------------------
-- SELECT * FROM pedidos ORDER BY id;
-- SELECT p.id, c.nombre, p.estado, p.total FROM pedidos p JOIN clientes c ON c.id = p.cliente_id;
-- SELECT d.*, pr.nombre, pr.precio FROM detalles_pedido d JOIN productos pr ON pr.id = d.producto_id;
-- SELECT e.*, r.nombre FROM entregas e JOIN repartidores r ON r.id = e.repartidor_id;
