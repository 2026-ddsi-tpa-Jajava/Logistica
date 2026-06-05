package ar.edu.utn.dds.k3003.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Asignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String idPaquete;
    private String idEntidad;
    private EstadoAsignacion estado;

    public Asignacion() {
    }

    public Asignacion(String idPaquete, String idEntidad){

        this.idPaquete = idPaquete;
        this.idEntidad = idEntidad;
        this.estado = EstadoAsignacion.ASIGNADA;

    }

    public Long getId() {
        return id;
    }

    public String getIdPaquete() {
        return idPaquete;
    }

    public String getIdEntidad() {
        return idEntidad;
    }

    public EstadoAsignacion getEstado() {
        return estado;
    }

    public void completarEntrega(){

        this.estado = EstadoAsignacion.COMPLETADA;
    }
}
