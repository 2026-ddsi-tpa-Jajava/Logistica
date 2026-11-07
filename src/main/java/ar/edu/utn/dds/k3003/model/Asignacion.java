package ar.edu.utn.dds.k3003.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

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
    private String depositoID;
    private String donacionID;
    private String productoID;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEntrega;


    public Asignacion() {
    }

    public Asignacion(String idPaquete, String idEntidad, String necesidadID, Integer cantidadAsignada, String origenAsignacion){

        this.idPaquete = idPaquete;
        this.idEntidad = idEntidad;
        this.cantidadAsignada = cantidadAsignada;
        this.estado = EstadoAsignacion.ASIGNADA;
        this.origenAsignacion = origenAsignacion;
        this.necesidadID = necesidadID;
        this.fechaCreacion = LocalDateTime.now();

    }

    public Asignacion(String idPaquete, String idEntidad, String necesidadID, Integer cantidadAsignada,
                      String origenAsignacion, String depositoID, String donacionID, String productoID){

        this(idPaquete, idEntidad, necesidadID, cantidadAsignada, origenAsignacion);
        this.depositoID = depositoID;
        this.donacionID = donacionID;
        this.productoID = productoID;
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

    public String getDepositoID() {return depositoID;}

    public String getDonacionID() {return donacionID;}

    public String getProductoID() {return productoID;}

    public LocalDateTime getFechaCreacion() {return fechaCreacion;}

    public LocalDateTime getFechaEntrega() {return fechaEntrega;}

    public void completarEntrega(){

        this.estado = EstadoAsignacion.COMPLETADA;
        this.fechaEntrega = LocalDateTime.now();
    }
}
