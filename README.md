# Parcial Programación 4 — Gestión de Pedidos y Entregas

Solución backend con **Spring Boot + PostgreSQL** y un servicio **Node.js (Express + Axios)** que consume la API y genera un resumen.

## Estructura

```
parcial-programacion4/
├── backend/          Proyecto Spring Boot (Entity, Repository, Service, Controller)
├── node-service/     Servicio Node.js (Express + Axios) -> GET /resumen-pedidos
├── database/
│   └── script.sql    Tablas + datos de prueba
├── pruebas.http      Peticiones listas para probar la API
├── README.md
└── .gitignore
```

## Requisitos

- Java 17 o superior (JDK)
- Maven (incluido en IntelliJ)
- PostgreSQL 14+ (pgAdmin opcional)
- Node.js 18+

## 1. Base de datos

```sql
CREATE DATABASE pedidos_db;
```

Ajusta usuario y contraseña en `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/pedidos_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

Las tablas se crean automáticamente al arrancar Spring Boot (`ddl-auto=update`).
Después de arrancar por primera vez, ejecuta `database/script.sql` para cargar los datos de prueba
(también puede ejecutarse antes: crea las tablas con `IF NOT EXISTS`).

## 2. Backend Spring Boot

```bash
cd backend
mvn spring-boot:run
```

API disponible en `http://localhost:8080/api`.

### Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET/POST | `/api/clientes` | Listar / crear clientes |
| GET/PUT/DELETE | `/api/clientes/{id}` | Consultar / actualizar / eliminar |
| GET/POST | `/api/productos` | Listar / crear productos |
| GET/PUT/DELETE | `/api/productos/{id}` | Consultar / actualizar / eliminar |
| GET/POST | `/api/repartidores` | Listar / crear repartidores |
| GET/PUT/DELETE | `/api/repartidores/{id}` | Consultar / actualizar / eliminar |
| GET/POST | `/api/pedidos` | Listar / crear pedidos (total calculado) |
| GET/PUT/DELETE | `/api/pedidos/{id}` | Consultar / actualizar / eliminar |
| POST | `/api/pedidos/{id}/detalles` | Agregar detalle a un pedido |
| DELETE | `/api/pedidos/{id}/detalles/{detalleId}` | Quitar detalle |
| **GET** | **`/api/pedidos/pendientes`** | Pedidos en estado PENDIENTE |
| **GET** | **`/api/pedidos/cliente/{id}`** | Pedidos de un cliente |
| **PUT** | **`/api/pedidos/{id}/despachar`** | Cambia a DESPACHADO si tiene detalles |
| GET/POST | `/api/entregas` | Listar / crear entregas |
| GET/PUT/DELETE | `/api/entregas/{id}` | Consultar / actualizar / eliminar |
| **GET** | **`/api/entregas/repartidor/{id}`** | Entregas de un repartidor |
| PUT | `/api/entregas/{id}/entregar` | Marca entrega y pedido como entregados |

### Reglas de negocio (capa Service)

1. **Total automático:** `subtotal = cantidad × precio del producto` y `total = Σ subtotales`.
   El DTO `PedidoRequest` no tiene campo `total`, así que no se puede digitar.
2. **Despacho:** un pedido solo pasa a `DESPACHADO` si está `PENDIENTE` y tiene al menos un detalle.
   Si no, la API responde `400` con el mensaje de error.
3. Extras: control de existencias, solo se crean entregas para pedidos despachados,
   el repartidor debe estar activo, un pedido solo tiene una entrega.

Ejemplo para crear un pedido:

```json
POST /api/pedidos
{
  "clienteId": 1,
  "detalles": [
    { "productoId": 1, "cantidad": 10 },
    { "productoId": 3, "cantidad": 2 }
  ]
}
```

## 3. Servicio Node.js

```bash
cd node-service
npm install
npm start
```

Abrir `http://localhost:3000/resumen-pedidos`:

```json
{
  "totalPedidos": 3,
  "pendientes": 1,
  "despachados": 1,
  "entregados": 1,
  "cancelados": 0,
  "montoTotal": 21.5,
  "generadoEn": "2026-10-19T17:45:47.573Z"
}
```

Variables opcionales: `PORT` (por defecto 3000) y `API_URL` (por defecto `http://localhost:8080/api`).

## 4. Pruebas

El archivo `pruebas.http` contiene todas las peticiones (CRUD, endpoints especiales y casos de error).
Se puede ejecutar desde IntelliJ (HTTP Client) o copiar a Postman.

## 5. Evidencias

Capturas de las pruebas realizadas: ver [EVIDENCIAS.md](EVIDENCIAS.md).
