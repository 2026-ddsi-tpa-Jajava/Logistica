package ar.edu.utn.dds.k3003.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Paquete {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String donacionID;
    private String producto;
    private Integer cantidad;

    public Paquete(){

    }

    public Paquete(String donacionID, String producto, Integer cantidad) {

        this.donacionID = donacionID;
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public Long getId(){return id;}

    public String getDonacionID() {return donacionID;}

    public String getProducto() {return producto;}

    public Integer getCantidad() {return cantidad;}


}

