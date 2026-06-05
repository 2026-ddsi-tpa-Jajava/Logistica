package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.model.Asignacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AsignacionRepository extends JpaRepository<Asignacion, Long> {

    Optional<Asignacion> findByIdPaquete(String idPaquete);
}
