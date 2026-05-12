package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.model.Asignacion;

import java.util.Optional;

public interface AsignacionRepository {

    Optional<Asignacion> findById(String idPaquete);

    Optional<Asignacion> findByPaqueteId(String id);

    Asignacion save(Asignacion asignacion);

}
