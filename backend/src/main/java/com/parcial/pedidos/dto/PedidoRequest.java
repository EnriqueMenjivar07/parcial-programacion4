package com.parcial.pedidos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Datos para crear/actualizar un pedido.
 * Fíjate que NO hay campo "total": así es imposible digitarlo manualmente.
 */
public class PedidoRequest {

    @NotNull(message = "El clienteId es obligatorio")
    private Long clienteId;

    @Valid
    private List<DetalleRequest> detalles = new ArrayList<>();

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public List<DetalleRequest> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleRequest> detalles) { this.detalles = detalles; }
}
