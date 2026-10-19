package com.parcial.pedidos.controller;

import com.parcial.pedidos.dto.EntregaRequest;
import com.parcial.pedidos.entity.Entrega;
import com.parcial.pedidos.service.EntregaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entregas")
public class EntregaController {

    private final EntregaService entregaService;

    public EntregaController(EntregaService entregaService) {
        this.entregaService = entregaService;
    }

    @GetMapping
    public List<Entrega> listar() {
        return entregaService.listar();
    }

    @GetMapping("/{id}")
    public Entrega buscar(@PathVariable Long id) {
        return entregaService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<Entrega> crear(@Valid @RequestBody EntregaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(entregaService.crear(request));
    }

    @PutMapping("/{id}")
    public Entrega actualizar(@PathVariable Long id, @Valid @RequestBody EntregaRequest request) {
        return entregaService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        entregaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoint especial
    @GetMapping("/repartidor/{id}")
    public List<Entrega> porRepartidor(@PathVariable Long id) {
        return entregaService.listarPorRepartidor(id);
    }

    @PutMapping("/{id}/entregar")
    public Entrega entregar(@PathVariable Long id) {
        return entregaService.marcarEntregada(id);
    }
}
