const express = require('express');
const axios = require('axios');

const app = express();
const PORT = process.env.PORT || 3000;
const API_URL = process.env.API_URL || 'http://localhost:8080/api';

// Cliente Axios con la URL base del backend Spring Boot
const api = axios.create({ baseURL: API_URL, timeout: 5000 });

/**
 * Procesa la lista de pedidos y genera el resumen consolidado.
 * Se separa en una función para que sea fácil de explicar y probar.
 */
function construirResumen(pedidos) {
  const contar = (estado) => pedidos.filter((p) => p.estado === estado).length;

  const montoTotal = pedidos.reduce((suma, p) => suma + Number(p.total || 0), 0);

  return {
    totalPedidos: pedidos.length,
    pendientes: contar('PENDIENTE'),
    despachados: contar('DESPACHADO'),
    entregados: contar('ENTREGADO'),
    cancelados: contar('CANCELADO'),
    montoTotal: Number(montoTotal.toFixed(2)),
    generadoEn: new Date().toISOString(),
  };
}

app.get('/', (req, res) => {
  res.json({ mensaje: 'Servicio de resumen de pedidos', endpoint: 'GET /resumen-pedidos' });
});

app.get('/resumen-pedidos', async (req, res) => {
  try {
    // 1) Consumir la API de Spring Boot con Axios
    const { data: pedidos } = await api.get('/pedidos');

    // 2) Procesar la información
    const resumen = construirResumen(pedidos);

    // 3) Responder con el resumen consolidado
    res.json(resumen);
  } catch (error) {
    console.error('Error al consultar la API de Spring Boot:', error.message);
    res.status(502).json({
      error: 'No se pudo obtener la información del backend Spring Boot',
      detalle: error.message,
    });
  }
});

app.listen(PORT, () => {
  console.log(`Servicio Node escuchando en http://localhost:${PORT}`);
  console.log(`Consumiendo API Spring Boot en ${API_URL}`);
});
