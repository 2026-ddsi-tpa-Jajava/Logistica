package ar.edu.utn.dds.k3003.model;

public class Asignacion {

    private String id;
    private String idPaquete;
    private String idEntidad;
    private EstadoAsignacion estado;

    public Asignacion(String id, String idPaquete, String idEntidad){

        this.id = id;
        this.idPaquete = idPaquete;
        this.idEntidad = idEntidad;
        this.estado = EstadoAsignacion.ASIGNADA;

    }

    public String getId() {
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
