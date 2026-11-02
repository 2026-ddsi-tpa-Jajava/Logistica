package ar.edu.utn.dds.k3003.model;

import java.io.Serializable;

public class MensajeConsulta implements Serializable {

    private String origen;
    private String tipoEvento;
    private Object payload;

    public MensajeConsulta() {
    }

    public MensajeConsulta(String origen, String tipoEvento, Object payload) {

        this.origen = origen;
        this.tipoEvento = tipoEvento;
        this.payload = payload;

    }

    public String getOrigen() {
        return origen;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public Object getPayload() {
        return payload;
    }
}