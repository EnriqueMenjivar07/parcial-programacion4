package com.parcial.pedidos.service;

import com.parcial.pedidos.entity.Repartidor;
import com.parcial.pedidos.exception.RecursoNoEncontradoException;
import com.parcial.pedidos.exception.ReglaNegocioException;
import com.parcial.pedidos.repository.EntregaRepository;
import com.parcial.pedidos.repository.RepartidorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RepartidorService {

    private final RepartidorRepository repartidorRepository;
    private final EntregaRepository entregaRepository;

    public RepartidorService(RepartidorRepository repartidorRepository, EntregaRepository entregaRepository) {
        this.repartidorRepository = repartidorRepository;
        this.entregaRepository = entregaRepository;
    }

    @Transactional(readOnly = true)
    public List<Repartidor> listar() {
        return repartidorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Repartidor buscarPorId(Long id) {
        return repartidorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Repartidor con id " + id + " no existe"));
    }

    public Repartidor crear(Repartidor repartidor) {
        repartidor.setId(null);
        return repartidorRepository.save(repartidor);
    }

    public Repartidor actualizar(Long id, Repartidor datos) {
        Repartidor repartidor = buscarPorId(id);
        repartidor.setNombre(datos.getNombre());
        repartidor.setTelefono(datos.getTelefono());
        repartidor.setActivo(datos.getActivo());
        return repartidorRepository.save(repartidor);
    }

    public void eliminar(Long id) {
        Repartidor repartidor = buscarPorId(id);
        if (entregaRepository.existsByRepartidorId(id)) {
            throw new ReglaNegocioException("No se puede eliminar el repartidor porque tiene entregas asignadas");
        }
        repartidorRepository.delete(repartidor);
    }
}
