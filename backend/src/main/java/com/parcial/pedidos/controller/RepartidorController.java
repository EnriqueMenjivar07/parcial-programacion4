package com.parcial.pedidos.controller;

import com.parcial.pedidos.entity.Repartidor;
import com.parcial.pedidos.service.RepartidorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repartidores")
public class RepartidorController {

    private final RepartidorService repartidorService;

    public RepartidorController(RepartidorService repartidorService) {
        this.repartidorService = repartidorService;
    }

    @GetMapping
    public List<Repartidor> listar() {
        return repartidorService.listar();
    }

    @GetMapping("/{id}")
    public Repartidor buscar(@PathVariable Long id) {
        return repartidorService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<Repartidor> crear(@Valid @RequestBody Repartidor repartidor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repartidorService.crear(repartidor));
    }

    @PutMapping("/{id}")
    public Repartidor actualizar(@PathVariable Long id, @Valid @RequestBody Repartidor repartidor) {
        return repartidorService.actualizar(id, repartidor);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        repartidorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
