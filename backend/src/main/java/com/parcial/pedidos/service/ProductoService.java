package com.parcial.pedidos.service;

import com.parcial.pedidos.entity.Producto;
import com.parcial.pedidos.exception.RecursoNoEncontradoException;
import com.parcial.pedidos.exception.ReglaNegocioException;
import com.parcial.pedidos.repository.DetallePedidoRepository;
import com.parcial.pedidos.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final DetallePedidoRepository detallePedidoRepository;

    public ProductoService(ProductoRepository productoRepository, DetallePedidoRepository detallePedidoRepository) {
        this.productoRepository = productoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
    }

    @Transactional(readOnly = true)
    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con id " + id + " no existe"));
    }

    public Producto crear(Producto producto) {
        producto.setId(null);
        return productoRepository.save(producto);
    }

    public Producto actualizar(Long id, Producto datos) {
        Producto producto = buscarPorId(id);
        producto.setNombre(datos.getNombre());
        producto.setPrecio(datos.getPrecio());
        producto.setExistencia(datos.getExistencia());
        return productoRepository.save(producto);
    }

    public void eliminar(Long id) {
        Producto producto = buscarPorId(id);
        if (detallePedidoRepository.existsByProductoId(id)) {
            throw new ReglaNegocioException("No se puede eliminar el producto porque aparece en pedidos");
        }
        productoRepository.delete(producto);
    }
}
