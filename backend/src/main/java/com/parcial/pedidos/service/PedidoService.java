package com.parcial.pedidos.service;

import com.parcial.pedidos.dto.DetalleRequest;
import com.parcial.pedidos.dto.PedidoRequest;
import com.parcial.pedidos.entity.*;
import com.parcial.pedidos.exception.RecursoNoEncontradoException;
import com.parcial.pedidos.exception.ReglaNegocioException;
import com.parcial.pedidos.repository.EntregaRepository;
import com.parcial.pedidos.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Aquí vive TODA la lógica de negocio de los pedidos:
 *  - cálculo del subtotal y del total (no se digitan manualmente)
 *  - control de existencias
 *  - validación para pasar a DESPACHADO
 */
@Service
@Transactional
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final EntregaRepository entregaRepository;
    private final ClienteService clienteService;
    private final ProductoService productoService;

    public PedidoService(PedidoRepository pedidoRepository, EntregaRepository entregaRepository,
                         ClienteService clienteService, ProductoService productoService) {
        this.pedidoRepository = pedidoRepository;
        this.entregaRepository = entregaRepository;
        this.clienteService = clienteService;
        this.productoService = productoService;
    }

    // ------------------------- CONSULTAS -------------------------

    @Transactional(readOnly = true)
    public List<Pedido> listar() {
        return pedidoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido con id " + id + " no existe"));
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPendientes() {
        return pedidoRepository.findByEstado(EstadoPedido.PENDIENTE);
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPorCliente(Long clienteId) {
        clienteService.buscarPorId(clienteId); // lanza 404 si el cliente no existe
        return pedidoRepository.findByClienteId(clienteId);
    }

    // ------------------------- CRUD -------------------------

    public Pedido crear(PedidoRequest request) {
        Pedido pedido = new Pedido();
        pedido.setCliente(clienteService.buscarPorId(request.getClienteId()));
        pedido.setEstado(EstadoPedido.PENDIENTE);
        for (DetalleRequest d : request.getDetalles()) {
            pedido.agregarDetalle(construirDetalle(d));
        }
        recalcularTotal(pedido);
        return pedidoRepository.save(pedido);
    }

    public Pedido actualizar(Long id, PedidoRequest request) {
        Pedido pedido = buscarPorId(id);
        validarEditable(pedido);
        pedido.setCliente(clienteService.buscarPorId(request.getClienteId()));

        // Devolvemos al inventario lo que tenían los detalles anteriores
        pedido.getDetalles().forEach(this::devolverExistencia);
        pedido.getDetalles().clear();

        for (DetalleRequest d : request.getDetalles()) {
            pedido.agregarDetalle(construirDetalle(d));
        }
        recalcularTotal(pedido);
        return pedidoRepository.save(pedido);
    }

    public void eliminar(Long id) {
        Pedido pedido = buscarPorId(id);
        if (entregaRepository.existsByPedidoId(id)) {
            throw new ReglaNegocioException("No se puede eliminar un pedido que ya tiene una entrega registrada");
        }
        if (pedido.getEstado() == EstadoPedido.PENDIENTE) {
            pedido.getDetalles().forEach(this::devolverExistencia);
        }
        pedidoRepository.delete(pedido);
    }

    // ------------------------- DETALLES -------------------------

    public Pedido agregarDetalle(Long pedidoId, DetalleRequest request) {
        Pedido pedido = buscarPorId(pedidoId);
        validarEditable(pedido);
        pedido.agregarDetalle(construirDetalle(request));
        recalcularTotal(pedido);
        return pedidoRepository.save(pedido);
    }

    public Pedido eliminarDetalle(Long pedidoId, Long detalleId) {
        Pedido pedido = buscarPorId(pedidoId);
        validarEditable(pedido);
        DetallePedido detalle = pedido.getDetalles().stream()
                .filter(d -> d.getId().equals(detalleId))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El detalle " + detalleId + " no pertenece al pedido " + pedidoId));
        devolverExistencia(detalle);
        pedido.getDetalles().remove(detalle);
        recalcularTotal(pedido);
        return pedidoRepository.save(pedido);
    }

    // ------------------------- REGLA DE NEGOCIO -------------------------

    /**
     * Cambia el pedido a DESPACHADO solo si:
     *  1) está PENDIENTE
     *  2) tiene al menos un detalle de pedido
     */
    public Pedido despachar(Long id) {
        Pedido pedido = buscarPorId(id);

        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new ReglaNegocioException("Solo se pueden despachar pedidos en estado PENDIENTE. Estado actual: "
                    + pedido.getEstado());
        }
        if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            throw new ReglaNegocioException(
                    "El pedido " + id + " no puede cambiar a DESPACHADO porque no tiene detalles de pedido");
        }

        pedido.setEstado(EstadoPedido.DESPACHADO);
        return pedidoRepository.save(pedido);
    }

    // ------------------------- MÉTODOS PRIVADOS -------------------------

    /** subtotal = cantidad x precio del producto */
    private DetallePedido construirDetalle(DetalleRequest request) {
        Producto producto = productoService.buscarPorId(request.getProductoId());
        if (producto.getExistencia() < request.getCantidad()) {
            throw new ReglaNegocioException("Existencia insuficiente para '" + producto.getNombre()
                    + "'. Disponible: " + producto.getExistencia() + ", solicitado: " + request.getCantidad());
        }
        producto.setExistencia(producto.getExistencia() - request.getCantidad());

        DetallePedido detalle = new DetallePedido();
        detalle.setProducto(producto);
        detalle.setCantidad(request.getCantidad());
        detalle.setSubtotal(producto.getPrecio().multiply(BigDecimal.valueOf(request.getCantidad())));
        return detalle;
    }

    /** total = suma de los subtotales */
    private void recalcularTotal(Pedido pedido) {
        BigDecimal total = pedido.getDetalles().stream()
                .map(DetallePedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        pedido.setTotal(total);
    }

    private void devolverExistencia(DetallePedido detalle) {
        Producto producto = detalle.getProducto();
        producto.setExistencia(producto.getExistencia() + detalle.getCantidad());
    }

    private void validarEditable(Pedido pedido) {
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new ReglaNegocioException("Solo se pueden modificar pedidos en estado PENDIENTE");
        }
    }
}
