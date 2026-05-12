package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.catedra.dtos.logistica.TipoAlgoritmoEnum;

public class Deposito {

    private String id;
    private String nombre;
    private String direccion;
    private Integer capacidadMaxima;
    private Integer stockActual;
    private TipoAlgoritmoEnum algoritmoMatchmaking;

    public Deposito(String id, String nombre, String direccion, Integer capacidadMaxima){

        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.capacidadMaxima = capacidadMaxima;
        this.algoritmoMatchmaking = null;

    }

    public String getId() {return id;}

    public void setId(String id) {this.id = id;}

    public String getNombre() {return nombre;}

    public void setNombre(String nombre) {this.nombre = nombre;}

    public String getDireccion() {return direccion;}

    public void setDireccion(String direccion) {this.direccion = direccion;}

    public Integer getCapacidadMaxima() {return capacidadMaxima;}

    public void setCapacidadMaxima(Integer capacidadMaxima) {this.capacidadMaxima = capacidadMaxima;}

    public Integer getStockActual() {return stockActual;}

    public void setStockActual(Integer stockActual) {this.stockActual = stockActual;}

    public TipoAlgoritmoEnum getAlgoritmoMatchmaking() {return algoritmoMatchmaking;}

    public void setAlgoritmoMatchmaking(TipoAlgoritmoEnum algoritmoMatchmaking) {this.algoritmoMatchmaking = algoritmoMatchmaking;}


    // modificación
    public void modificarStributos(String nombre, String direccion, Integer capacidadMaxima) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.capacidadMaxima = capacidadMaxima;
    }




}



