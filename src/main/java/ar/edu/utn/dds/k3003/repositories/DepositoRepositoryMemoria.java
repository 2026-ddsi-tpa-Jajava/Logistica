package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.model.Deposito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DepositoRepositoryMemoria implements DepositoRepository{

    private List<Deposito> depositos = new ArrayList<>();

    @Override
    public Optional<Deposito> findById(String id) {
        return depositos.stream().filter(d -> d.getId().equals(id)).findFirst();
    }

    @Override
    public Deposito save(Deposito deposito) {
        depositos.add(deposito);
        return deposito;
    }

    @Override
    public Optional<Deposito> deleteById(String id) {
        Optional<Deposito> deposito = depositos.stream().filter(d -> d.getId().equals(id)).findFirst();

        deposito.ifPresent(depositos::remove);

        return deposito;
    }


}
