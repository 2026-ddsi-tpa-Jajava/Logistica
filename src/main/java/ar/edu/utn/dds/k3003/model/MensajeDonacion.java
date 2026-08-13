package ar.edu.utn.dds.k3003.model;

import java.io.Serializable;

public class MensajeDonacion implements Serializable {

    private String depositoID;
    private String donacionID;
    private String productoID;
    private Integer cantidad;

    public MensajeDonacion(){
    }

    public MensajeDonacion(String depositoID, String donacionID, String productoID, Integer cantidad){

        this.depositoID = depositoID;
        this.donacionID = donacionID;
        this.productoID = productoID;
        this.cantidad = cantidad;
    }

    public String getDepositoID() {
        return depositoID;
    }

    public String getDonacionID() {
        return donacionID;
    }

    public String getProductoID() {
        return productoID;
    }

    public Integer getCantidad() {
        return cantidad;
    }
}
