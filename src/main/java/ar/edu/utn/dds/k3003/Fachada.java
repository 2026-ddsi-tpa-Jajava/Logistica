package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.*;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.AsignacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.DepositoDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.PaqueteDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.EstadoAsginacionEnum;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonaciones;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.TipoAlgoritmoEnum;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaIncentivos;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaLogistica;
import ar.edu.utn.dds.k3003.exceptions.DonadorNoEncontradoException;
import ar.edu.utn.dds.k3003.exceptions.DonadorYaExistenteException;
import ar.edu.utn.dds.k3003.model.Asignacion;
import ar.edu.utn.dds.k3003.model.Deposito;
import ar.edu.utn.dds.k3003.repositories.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import lombok.val;
import org.springframework.stereotype.Component;

@Component
public class Fachada implements FachadaLogistica {

  public Fachada() {


    this.depositoRepository = new DepositoRepositoryMemoria();
    this.asignacionRepository = new AsignacionRepositoryMemoria();


  }

  private DepositoRepository depositoRepository;
  private AsignacionRepository asignacionRepository;
  private Integer contadorIdDeposito = 0;
  private Integer contadorIdAsignacion = 0;
  private Integer contadorIdPaquete = 0;
  private FachadaDonadoresYEntidades fachadaDonadoresYEntidades;
  private FachadaDonaciones fachadaDonaciones;

  private String generarIdDeposito(){

    contadorIdDeposito++;

    return String.valueOf(contadorIdDeposito);
  }

  private String generarIdAsignacion(){

    contadorIdAsignacion++;

    return String.valueOf(contadorIdAsignacion);
  }

  private String generarIdPaquete(){

    contadorIdPaquete++;

    return String.valueOf(contadorIdPaquete);
  }



  private double calcularScore(NecesidadMaterialDTO necesidad) {

    return necesidad.nivelDeUrgencia() / (double) necesidad.cantidadObjetivo();

  }


  public List<DepositoDTO> obtenerDepositos() {
    return depositoRepository.findAll().stream().map(deposito -> new DepositoDTO(
                    deposito.getId(),
                    deposito.getAlgoritmoMatchmaking(),
                    deposito.getNombre(),
                    deposito.getDireccion(),
                    deposito.getCapacidadMaxima(),
                    List.of()
            )).toList();
  }


  @Override
  public DepositoDTO agregarDeposito(DepositoDTO depositoDTO) {

    if (depositoDTO == null) {
      throw new RuntimeException();
    }

    if (depositoDTO.id() != null && depositoRepository.findById(depositoDTO.id()).isPresent()) {
      throw new RuntimeException();
    }


    String id = generarIdDeposito();

    Deposito deposito = new Deposito(id, depositoDTO.nombre(), depositoDTO.direccion(), depositoDTO.capacidadMaxima());

    depositoRepository.save(deposito);

    return new DepositoDTO(deposito.getId(), deposito.getAlgoritmoMatchmaking(), deposito.getNombre(), deposito.getDireccion(), deposito.getCapacidadMaxima(), List.of());

  }

  @Override
  public DepositoDTO buscarDepositoPorID(String depositoID) throws NoSuchElementException {

    Deposito deposito = depositoRepository.findById(depositoID).orElseThrow(NoSuchElementException :: new);

    return new DepositoDTO(deposito.getId(), deposito.getAlgoritmoMatchmaking(), deposito.getNombre(), deposito.getDireccion(), deposito.getCapacidadMaxima(), List.of());
  }

  @Override
  public AsignacionDTO buscarAsignacionPorPaqueteID(String paqueteID) throws NoSuchElementException {

    Asignacion asignacion = asignacionRepository.findByPaqueteId(paqueteID).orElseThrow(NoSuchElementException :: new);

    return new AsignacionDTO(asignacion.getId(), asignacion.getIdPaquete(), asignacion.getIdEntidad(), LocalDateTime.now(), EstadoAsginacionEnum.valueOf(asignacion.getEstado().name()));
  }

  @Override
  public DepositoDTO gestionarDonacion(String depositoID, String donacionID, String productoID, Integer cantidad) throws NoSuchElementException {

    Deposito deposito = depositoRepository.findById(depositoID).orElseThrow(NoSuchElementException::new);

    if(cantidad <= 0){
      throw new IllegalArgumentException("Cantidad de producto invalida");
    }


    List<NecesidadMaterialDTO> necesidades = fachadaDonadoresYEntidades.obtenerNecesidadesInsatisfechasDe(productoID);

    if(necesidades.isEmpty()){
      return new DepositoDTO(deposito.getId(), deposito.getAlgoritmoMatchmaking(), deposito.getNombre(), deposito.getDireccion(), deposito.getCapacidadMaxima(), List.of());
    }


    for (NecesidadMaterialDTO necesidad : necesidades) {
        if (necesidad.tipo() == TipoNecesidadMaterialEnum.RECURRENTE && cantidad < necesidad.cantidadObjetivo()) {
            throw new IllegalArgumentException(
                "No se permiten donaciones parciales para necesidades recurrentes"
        );
      }
    }

    String idPaquete = generarIdPaquete();

    PaqueteDTO paqueteDTO = new PaqueteDTO(idPaquete, donacionID, productoID, cantidad);

    AsignacionDTO asignacion = ejecutarMatchmaking(depositoID, paqueteDTO, necesidades);

    return new DepositoDTO(deposito.getId(), deposito.getAlgoritmoMatchmaking(), deposito.getNombre(),deposito.getDireccion(), deposito.getCapacidadMaxima(), List.of());

  }

  @Override
  public void setAlgoritmoMM(String depositoID, TipoAlgoritmoEnum algoritmo) {
    Deposito deposito = depositoRepository.findById(depositoID)
            .orElseThrow(NoSuchElementException::new);

    deposito.setAlgoritmoMatchmaking(algoritmo);
  }

  @Override
  public AsignacionDTO ejecutarMatchmaking(String depositoID, PaqueteDTO paqueteDTO, List<NecesidadMaterialDTO> necesidades) {

    if (paqueteDTO == null) {
      throw new RuntimeException();
    }

    Deposito deposito = depositoRepository.findById(depositoID).orElseThrow(NoSuchElementException::new);

    TipoAlgoritmoEnum algoritmo = deposito.getAlgoritmoMatchmaking();

    // La lista no está vacía (ya validado en gestionarDonacion)
    NecesidadMaterialDTO necesidadSeleccionada =  necesidades.stream().max(Comparator.comparing(NecesidadMaterialDTO::cantidadObjetivo)).orElseThrow();


    if (algoritmo == null || algoritmo == TipoAlgoritmoEnum.SUB_ATENDIDOS) {

      necesidadSeleccionada = necesidades.stream().max(Comparator.comparing(NecesidadMaterialDTO::cantidadObjetivo)).orElseThrow();



    } else if (algoritmo == TipoAlgoritmoEnum.PRIORIDAD_POR_SCORE) {

      necesidadSeleccionada = necesidades.stream().max(Comparator.comparing(this::calcularScore)).orElseThrow();
    }

    else {

      throw new IllegalStateException("Algoritmo de matchmaking no soportado");

    }



    String idAsignacion = generarIdAsignacion();


    String idNecesidad = necesidadSeleccionada.id();

    if (idNecesidad == null) {
      idNecesidad = "necesidad1";
    }

    Asignacion asignacion = new Asignacion(idAsignacion, paqueteDTO.id(), idNecesidad);

    //Asignacion asignacion = new Asignacion(idAsignacion, paqueteDTO.id(), necesidadSeleccionada.id());

    asignacionRepository.save(asignacion);

    return new AsignacionDTO(asignacion.getId(), asignacion.getIdPaquete(), asignacion.getIdEntidad(), LocalDateTime.now(), EstadoAsginacionEnum.valueOf(asignacion.getEstado().name()));

  }



  @Override
  public void reportarEntrega(PaqueteDTO paqueteDTO) {

    if (paqueteDTO == null) {
      throw new RuntimeException();
    }

    Asignacion asignacion = asignacionRepository.findByPaqueteId(paqueteDTO.id()).orElseThrow(NoSuchElementException::new);

    fachadaDonadoresYEntidades.satisfacerNecesidad(asignacion.getIdEntidad(), paqueteDTO.cantidad());

    fachadaDonaciones.cambiarEstadoDeDonacion(paqueteDTO.donacionID(), EstadoDonacionEnum.ACEPTADA);

    asignacion.completarEntrega();
  }



  @Override
  public void setFachadaDonadoresYEntidades(FachadaDonadoresYEntidades fachadaDonadoresYEntidades) {

    this.fachadaDonadoresYEntidades = fachadaDonadoresYEntidades;

  }

  @Override
  public void setFachadaDonaciones(FachadaDonaciones fachadaDonaciones) {

    this.fachadaDonaciones = fachadaDonaciones;

  }



}

