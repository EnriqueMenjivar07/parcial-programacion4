package com.parcial.pedidos.repository;

import com.parcial.pedidos.entity.Entrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntregaRepository extends JpaRepository<Entrega, Long> {

    // SELECT * FROM entregas WHERE repartidor_id = ?
    List<Entrega> findByRepartidorId(Long repartidorId);

    boolean existsByPedidoId(Long pedidoId);

    boolean existsByRepartidorId(Long repartidorId);
}
