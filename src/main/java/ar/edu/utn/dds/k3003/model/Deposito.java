package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.catedra.dtos.logistica.TipoAlgoritmoEnum;
import jakarta.persistence.*;

@Entity
public class Deposito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String direccion;
    private Integer capacidadMaxima;
    private Integer stockActual;

    @Enumerated(EnumType.STRING)
    private TipoAlgoritmoEnum algoritmoMatchmaking;

    public Deposito() {
    }

    public Deposito(String nombre, String direccion, Integer capacidadMaxima) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.capacidadMaxima = capacidadMaxima;
        this.algoritmoMatchmaking = null;
    }


    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

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



