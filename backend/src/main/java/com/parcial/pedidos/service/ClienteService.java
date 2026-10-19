package com.parcial.pedidos.service;

import com.parcial.pedidos.entity.Cliente;
import com.parcial.pedidos.exception.RecursoNoEncontradoException;
import com.parcial.pedidos.exception.ReglaNegocioException;
import com.parcial.pedidos.repository.ClienteRepository;
import com.parcial.pedidos.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;

    public ClienteService(ClienteRepository clienteRepository, PedidoRepository pedidoRepository) {
        this.clienteRepository = clienteRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar() {
        return clienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente con id " + id + " no existe"));
    }

    public Cliente crear(Cliente cliente) {
        cliente.setId(null);
        return clienteRepository.save(cliente);
    }

    public Cliente actualizar(Long id, Cliente datos) {
        Cliente cliente = buscarPorId(id);
        cliente.setNombre(datos.getNombre());
        cliente.setCorreo(datos.getCorreo());
        cliente.setTelefono(datos.getTelefono());
        return clienteRepository.save(cliente);
    }

    public void eliminar(Long id) {
        Cliente cliente = buscarPorId(id);
        if (pedidoRepository.existsByClienteId(id)) {
            throw new ReglaNegocioException("No se puede eliminar el cliente porque tiene pedidos registrados");
        }
        clienteRepository.delete(cliente);
    }
}
