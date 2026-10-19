# Evidencias de funcionamiento

Pruebas ejecutadas el 19/10/2026 con el backend Spring Boot (puerto 8080), PostgreSQL 17 (`pedidos_db`), el servicio Node.js (puerto 3000) y Postman (colección `Parcial_Pedidos.postman_collection.json`).

## Resumen

| # | Evidencia | Método | Resultado |
|---|---|---|---|
| 1 | Servicio Node.js corriendo | — | Puerto 3000 |
| 2 | Datos de prueba en PostgreSQL | SQL | 3 clientes |
| 3 | Listar clientes | GET | 200 |
| 4 | Total ignorado si se envía manualmente | POST | 201, total 5.50 (no 99999) |
| 5 | Pedido con detalles, total calculado | POST | 201, total 14.50 |
| 6 | Pedido sin detalles | POST | 201, total 0 |
| 7 | Pedidos de un cliente | GET | 200 |
| 8 | Despachar pedido con detalles | PUT | 200, DESPACHADO |
| 9 | **Regla de negocio: despachar sin detalles** | PUT | **400** |
| 10 | Despachar segundo pedido | PUT | 200 |
| 11 | Crear entrega | POST | 201, ASIGNADA |
| 12 | Pedido con entrega existente | POST | 400 |
| 13 | Entregas de un repartidor | GET | 200 |
| 14 | Marcar entrega como entregada | PUT | 200, ENTREGADA |
| 15 | Resumen Node.js (intermedio) | GET | 3 pedidos, monto 20 |
| 16 | Pedidos pendientes | GET | 200 |
| 17 | Repartidor inactivo | POST | 400 |
| 18 | Crear clientes | POST | 201 (ids 4 y 5) |
| 19 | Actualizar cliente | PUT | 200 |
| 20 | Eliminar cliente | DELETE | 204 |
| 21 | Cliente con datos inválidos | POST | 400 |
| 22 | Crear producto | POST | 201 |
| 23 | Actualizar producto | PUT | 200 |
| 24 | Precio negativo | POST | 400 |
| 25 | Modificar pedido ya despachado | POST | 400 |
| 26 | Agregar detalle a pedido pendiente | POST | 201, total 5.85 |
| 27 | Actualizar pedido (recalcula total) | PUT | 200, total 7.25 |
| 28 | Existencia insuficiente | POST | 400 |
| 29 | Eliminar pedido con entrega | DELETE | 400 |
| 30 | Eliminar pedido | DELETE | 204 |
| 31 | Resumen Node.js final | GET | 2 pedidos, monto 20 |

---

# A. Puesta en marcha

## 1. Servicio Node.js en ejecución
Se ejecutó `npm start` dentro de `node-service`. El servicio escucha en `http://localhost:3000` y consume la API en `http://localhost:8080/api`.

![](evidencias/01-node-corriendo.png)

## 2. Base de datos con registros de prueba
Se ejecutó `database/script.sql` sobre `pedidos_db`. Las tablas fueron generadas por JPA a partir de las entidades.

![](evidencias/02-pgadmin-clientes.png)

## 3. GET `/api/clientes`
El backend lee los datos desde PostgreSQL.

![](evidencias/03-api-get-clientes.png)

---

# B. Regla principal: total calculado automáticamente

## 4. El total enviado manualmente se ignora
Se envió `"total": 99999`. `PedidoRequest` no tiene ese campo, así que se descarta. El Service calcula 5 × $1.10 = **$5.50**.

![](evidencias/04-post-pedido-total-ignorado.png)

## 5. Total = suma de subtotales
- Subtotal 1: 10 × $0.75 = $7.50
- Subtotal 2: 2 × $3.50 = $7.00
- **Total = $14.50**

![](evidencias/05-post-pedido-con-detalles.png)

## 6. Pedido sin detalles
Se crea el pedido 3 vacío (total 0) para demostrar después la validación del despacho.

![](evidencias/06-post-pedido-sin-detalles.png)

---

# C. Endpoints especiales

## 7. GET `/api/pedidos/cliente/{id}`
![](evidencias/07-get-pedidos-cliente.png)

## 8. PUT `/api/pedidos/{id}/despachar`, pedido con detalles
![](evidencias/08-put-despachar-pedido-1.png)

## 9. Validación obligatoria provocada intencionalmente
El pedido 3 no tiene detalles. `PedidoService.despachar()` lanza `ReglaNegocioException` y la API responde **400**:

> "El pedido 3 no puede cambiar a DESPACHADO porque no tiene detalles de pedido"

![](evidencias/09-regla-negocio-400.png)

## 10. Despachar el pedido 2
![](evidencias/10-put-despachar-pedido-2.png)

## 16. GET `/api/pedidos/pendientes`
Solo aparece el pedido 3, porque los pedidos 1 y 2 ya no están pendientes.

![](evidencias/16-get-pedidos-pendientes.png)

## 13. GET `/api/entregas/repartidor/{id}`
![](evidencias/13-get-entregas-repartidor.png)

---

# D. Entregas

## 11. Crear entrega
Pedido 1 (DESPACHADO), asignado al repartidor 1. Estado inicial: **ASIGNADA**.

![](evidencias/11-post-crear-entrega.png)

## 12. Un pedido solo puede tener una entrega
Relación uno a uno Pedido–Entrega: *"El pedido 1 ya tiene una entrega"*.

![](evidencias/12-validacion-entrega-duplicada.png)

## 17. El repartidor debe estar activo
*"El repartidor Pedro Ramírez no está activo"*.

![](evidencias/17-validacion-repartidor-inactivo.png)

## 14. Marcar entrega como entregada
La entrega pasa a **ENTREGADA** con `fechaEntrega`, y el pedido 1 cambia a **ENTREGADO**.

![](evidencias/14-put-marcar-entregada.png)

---

# E. CRUD de clientes y productos

## 18. POST `/api/clientes`
El `id` lo genera PostgreSQL (`@GeneratedValue`). Los campos que no existen en la entidad (`idCliente`, `ciudad`) se ignoran.

![](evidencias/18a-post-crear-cliente-4.png)
![](evidencias/18b-post-crear-cliente-5.png)

## 19. PUT `/api/clientes/5`
![](evidencias/19-put-actualizar-cliente.png)

## 20. DELETE `/api/clientes/4`, 204 No Content
![](evidencias/20-delete-cliente-204.png)

## 21. Validación con `@NotBlank` y `@Email`
![](evidencias/21-validacion-cliente-400.png)

## 22. POST `/api/productos`
![](evidencias/22-post-crear-producto.png)

## 23. PUT `/api/productos/6`
![](evidencias/23-put-actualizar-producto.png)

## 24. Validación con `@Positive`
![](evidencias/24-validacion-precio-negativo-400.png)

---

# F. Reglas adicionales de pedidos

## 25. Solo se modifican pedidos PENDIENTES
![](evidencias/25-regla-modificar-pedido-despachado-400.png)

## 26. Agregar detalle: el total se recalcula
3 × $1.95 = **$5.85**

![](evidencias/26-post-agregar-detalle-pedido-3.png)

## 27. PUT `/api/pedidos/3`: reemplaza detalles y recalcula
4 × $0.75 + 1 × $4.25 = **$7.25**

![](evidencias/27-put-actualizar-pedido-3.png)

## 28. Control de existencias
La existencia del café era 80. La actualización anterior descontó 1, por eso la API reporta *"Disponible: 79"*.

![](evidencias/28-validacion-existencia-insuficiente-400.png)

## 29. No se elimina un pedido con entrega registrada
![](evidencias/29-validacion-eliminar-pedido-con-entrega-400.png)

## 30. DELETE `/api/pedidos/3`, 204 No Content
Al eliminar un pedido pendiente, la existencia de sus productos se devuelve al inventario.

![](evidencias/30-delete-pedido-3-204.png)

---

# G. Servicio Node.js: resumen consolidado

## 15. Resumen intermedio
| Pedido | Estado | Total |
|---|---|---|
| 1 | ENTREGADO | 5.50 |
| 2 | DESPACHADO | 14.50 |
| 3 | PENDIENTE | 0.00 |
| | **Monto total** | **20.00** |

![](evidencias/15-resumen-node-con-datos.png)

## 31. Resumen final (después de eliminar el pedido 3)
2 pedidos: 0 pendientes, 1 despachado, 1 entregado, monto total 20.

![](evidencias/31-resumen-node-final.png)
