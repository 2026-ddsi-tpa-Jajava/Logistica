package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.model.Asignacion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AsignacionRepositoryMemoria implements AsignacionRepository {

    private List<Asignacion> asignaciones = new ArrayList<>();

    @Override
    public Optional<Asignacion> findById(String id) {
        return asignaciones.stream().filter(a -> a.getId().equals(id)).findFirst();
    }

    @Override
    public Optional<Asignacion> findByPaqueteId(String paqueteId) {
        return asignaciones.stream().filter(a -> a.getIdPaquete().equals(paqueteId)).findFirst();
    }

    @Override
    public Asignacion save(Asignacion asignacion) {
        asignaciones.add(asignacion);
        return asignacion;
    }
}