package com.parcial.pedidos.service;

import com.parcial.pedidos.dto.EntregaRequest;
import com.parcial.pedidos.entity.*;
import com.parcial.pedidos.exception.RecursoNoEncontradoException;
import com.parcial.pedidos.exception.ReglaNegocioException;
import com.parcial.pedidos.repository.EntregaRepository;
import com.parcial.pedidos.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class EntregaService {

    private final EntregaRepository entregaRepository;
    private final PedidoRepository pedidoRepository;
    private final PedidoService pedidoService;
    private final RepartidorService repartidorService;

    public EntregaService(EntregaRepository entregaRepository, PedidoRepository pedidoRepository,
                          PedidoService pedidoService, RepartidorService repartidorService) {
        this.entregaRepository = entregaRepository;
        this.pedidoRepository = pedidoRepository;
        this.pedidoService = pedidoService;
        this.repartidorService = repartidorService;
    }

    @Transactional(readOnly = true)
    public List<Entrega> listar() {
        return entregaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Entrega buscarPorId(Long id) {
        return entregaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Entrega con id " + id + " no existe"));
    }

    @Transactional(readOnly = true)
    public List<Entrega> listarPorRepartidor(Long repartidorId) {
        repartidorService.buscarPorId(repartidorId); // 404 si no existe
        return entregaRepository.findByRepartidorId(repartidorId);
    }

    public Entrega crear(EntregaRequest request) {
        Pedido pedido = pedidoService.buscarPorId(request.getPedidoId());
        if (pedido.getEstado() != EstadoPedido.DESPACHADO) {
            throw new ReglaNegocioException("Solo se puede crear una entrega para pedidos DESPACHADOS");
        }
        if (entregaRepository.existsByPedidoId(pedido.getId())) {
            throw new ReglaNegocioException("El pedido " + pedido.getId() + " ya tiene una entrega");
        }
        Repartidor repartidor = obtenerRepartidorActivo(request.getRepartidorId());

        Entrega entrega = new Entrega();
        entrega.setPedido(pedido);
        entrega.setRepartidor(repartidor);
        entrega.setEstado(EstadoEntrega.ASIGNADA);
        entrega.setFechaSalida(request.getFechaSalida() != null ? request.getFechaSalida() : LocalDateTime.now());
        return entregaRepository.save(entrega);
    }

    public Entrega actualizar(Long id, EntregaRequest request) {
        Entrega entrega = buscarPorId(id);
        if (entrega.getEstado() == EstadoEntrega.ENTREGADA) {
            throw new ReglaNegocioException("No se puede modificar una entrega ya completada");
        }
        entrega.setRepartidor(obtenerRepartidorActivo(request.getRepartidorId()));
        if (request.getFechaSalida() != null) {
            entrega.setFechaSalida(request.getFechaSalida());
        }
        return entregaRepository.save(entrega);
    }

    /** Marca la entrega como ENTREGADA y el pedido como ENTREGADO. */
    public Entrega marcarEntregada(Long id) {
        Entrega entrega = buscarPorId(id);
        if (entrega.getEstado() == EstadoEntrega.ENTREGADA) {
            throw new ReglaNegocioException("La entrega ya fue completada");
        }
        entrega.setEstado(EstadoEntrega.ENTREGADA);
        entrega.setFechaEntrega(LocalDateTime.now());

        Pedido pedido = entrega.getPedido();
        pedido.setEstado(EstadoPedido.ENTREGADO);
        pedidoRepository.save(pedido);

        return entregaRepository.save(entrega);
    }

    public void eliminar(Long id) {
        Entrega entrega = buscarPorId(id);
        entregaRepository.delete(entrega);
    }

    private Repartidor obtenerRepartidorActivo(Long repartidorId) {
        Repartidor repartidor = repartidorService.buscarPorId(repartidorId);
        if (!Boolean.TRUE.equals(repartidor.getActivo())) {
            throw new ReglaNegocioException("El repartidor " + repartidor.getNombre() + " no está activo");
        }
        return repartidor;
    }
}
