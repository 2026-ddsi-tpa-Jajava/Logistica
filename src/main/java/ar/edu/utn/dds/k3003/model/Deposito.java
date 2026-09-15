package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.catedra.dtos.logistica.PaqueteDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.TipoAlgoritmoEnum;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Deposito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String direccion;
    private Integer capacidadMaxima;
    private Integer cantidadStock;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Paquete> stockActual;

    @Enumerated(EnumType.STRING)
    private TipoAlgoritmoEnum algoritmoMatchmaking;

    public Deposito() {
    }

    public Deposito(String nombre, String direccion, Integer capacidadMaxima) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.capacidadMaxima = capacidadMaxima;
        this.algoritmoMatchmaking = null;
        this.stockActual = new ArrayList<>();
        this.cantidadStock = 0;
    }


    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public String getNombre() {return nombre;}

    public void setNombre(String nombre) {this.nombre = nombre;}

    public String getDireccion() {return direccion;}

    public void setDireccion(String direccion) {this.direccion = direccion;}

    public Integer getCapacidadMaxima() {return capacidadMaxima;}

    public void setCapacidadMaxima(Integer capacidadMaxima) {this.capacidadMaxima = capacidadMaxima;}

    public List<Paquete> getStockActual() {return stockActual;}

    public void setStockActual(List<Paquete> stockActual) {this.stockActual = stockActual;}

    public TipoAlgoritmoEnum getAlgoritmoMatchmaking() {return algoritmoMatchmaking;}

    public void setAlgoritmoMatchmaking(TipoAlgoritmoEnum algoritmoMatchmaking) {this.algoritmoMatchmaking = algoritmoMatchmaking;}

    public Integer getCantidadStock() {return cantidadStock;}

    public void setCantidadStock(Integer cantidadStock) {this.cantidadStock = cantidadStock;}


    // modificación
    public void modificarStributos(String nombre, String direccion, Integer capacidadMaxima) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.capacidadMaxima = capacidadMaxima;
    }

    // agregar paquete al stock
    public void agregarPaqueteAlStock(Paquete paquete){
        this.stockActual.add(paquete);
        this.cantidadStock += paquete.getCantidad();

    }

    public Integer ocupacionActual() {

        return stockActual.stream().mapToInt(Paquete::getCantidad).sum();}

    public boolean tieneLugar(Integer cantidadEntrante){

        return ocupacionActual() + cantidadEntrante <= capacidadMaxima;

    }

}



