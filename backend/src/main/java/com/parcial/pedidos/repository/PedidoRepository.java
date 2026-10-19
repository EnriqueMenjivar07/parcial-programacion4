package com.parcial.pedidos.repository;

import com.parcial.pedidos.entity.EstadoPedido;
import com.parcial.pedidos.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // SELECT * FROM pedidos WHERE estado = ?
    List<Pedido> findByEstado(EstadoPedido estado);

    // SELECT * FROM pedidos WHERE cliente_id = ?
    List<Pedido> findByClienteId(Long clienteId);

    boolean existsByClienteId(Long clienteId);
}
