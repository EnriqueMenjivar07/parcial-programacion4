package com.parcial.pedidos.controller;

import com.parcial.pedidos.dto.DetalleRequest;
import com.parcial.pedidos.dto.PedidoRequest;
import com.parcial.pedidos.entity.Pedido;
import com.parcial.pedidos.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // ---------- CRUD ----------

    @GetMapping
    public List<Pedido> listar() {
        return pedidoService.listar();
    }

    @GetMapping("/{id}")
    public Pedido buscar(@PathVariable Long id) {
        return pedidoService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<Pedido> crear(@Valid @RequestBody PedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crear(request));
    }

    @PutMapping("/{id}")
    public Pedido actualizar(@PathVariable Long id, @Valid @RequestBody PedidoRequest request) {
        return pedidoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pedidoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Endpoints especiales ----------

    @GetMapping("/pendientes")
    public List<Pedido> pendientes() {
        return pedidoService.listarPendientes();
    }

    @GetMapping("/cliente/{id}")
    public List<Pedido> porCliente(@PathVariable Long id) {
        return pedidoService.listarPorCliente(id);
    }

    @PutMapping("/{id}/despachar")
    public Pedido despachar(@PathVariable Long id) {
        return pedidoService.despachar(id);
    }

    // ---------- Detalles del pedido ----------

    @PostMapping("/{id}/detalles")
    public ResponseEntity<Pedido> agregarDetalle(@PathVariable Long id, @Valid @RequestBody DetalleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.agregarDetalle(id, request));
    }

    @DeleteMapping("/{id}/detalles/{detalleId}")
    public Pedido eliminarDetalle(@PathVariable Long id, @PathVariable Long detalleId) {
        return pedidoService.eliminarDetalle(id, detalleId);
    }
}
