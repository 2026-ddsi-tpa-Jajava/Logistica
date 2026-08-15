package ar.edu.utn.dds.k3003.model;

import jakarta.persistence.*;

@Entity
public class Asignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String idPaquete;
    private String idEntidad;
    private Integer cantidadAsignada;
    @Enumerated(EnumType.STRING)
    private EstadoAsignacion estado;
    private String origenAsignacion;
    private String necesidadID;


    public Asignacion() {
    }

    public Asignacion(String idPaquete, String idEntidad, String necesidadID, Integer cantidadAsignada, String origenAsignacion){

        this.idPaquete = idPaquete;
        this.idEntidad = idEntidad;
        this.cantidadAsignada = cantidadAsignada;
        this.estado = EstadoAsignacion.ASIGNADA;
        this.origenAsignacion = origenAsignacion;
        this.necesidadID = necesidadID;

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

    public Integer getCantidadAsignada() {return cantidadAsignada;}

    public EstadoAsignacion getEstado() {
        return estado;
    }

    public String getNecesidadID() {
        return necesidadID;
    }

    public String getOrigenAsignacion() {
        return origenAsignacion;
    }

    public void completarEntrega(){

        this.estado = EstadoAsignacion.COMPLETADA;
    }
}
