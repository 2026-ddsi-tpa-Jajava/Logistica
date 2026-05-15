package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.model.Deposito;

import java.util.List;
import java.util.Optional;

public interface DepositoRepository {

    Optional<Deposito> findById(String id);

    Deposito save(Deposito deposito);

    Optional<Deposito> deleteById(String id);

    List<Deposito> findAll();
}
