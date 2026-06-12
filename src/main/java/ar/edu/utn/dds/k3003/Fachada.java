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
import ar.edu.utn.dds.k3003.clients.DonacionesClient;
import ar.edu.utn.dds.k3003.clients.DonadoresYEntidadesClient;
import ar.edu.utn.dds.k3003.exceptions.DonadorNoEncontradoException;
import ar.edu.utn.dds.k3003.exceptions.DonadorYaExistenteException;
import ar.edu.utn.dds.k3003.model.Asignacion;
import ar.edu.utn.dds.k3003.model.Deposito;
import ar.edu.utn.dds.k3003.repositories.*;

import java.time.LocalDateTime;
import java.util.*;

import io.micrometer.core.instrument.Metrics;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Fachada implements FachadaLogistica {


    @Autowired
    private DonacionesClient donacionesClient;
    @Autowired
    private DonadoresYEntidadesClient donadoresYEntidadesClient;

  public Fachada() {
  }


  @Autowired
  private DepositoRepository depositoRepository;
  @Autowired
  private AsignacionRepository asignacionRepository;

  private FachadaDonadoresYEntidades fachadaDonadoresYEntidades;
  private FachadaDonaciones fachadaDonaciones;


  private double calcularScore(NecesidadMaterialDTO necesidad) {

    return necesidad.nivelDeUrgencia() / (double) necesidad.cantidadObjetivo();

  }

  public List<DepositoDTO> obtenerDepositos() {
    return depositoRepository.findAll().stream().map(deposito -> new DepositoDTO(
                    deposito.getId().toString(),
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

    if (depositoDTO.id() != null && depositoRepository.findById(Long.parseLong(depositoDTO.id())).isPresent()) {
      throw new RuntimeException();
    }

    Deposito deposito = new Deposito(depositoDTO.nombre(), depositoDTO.direccion(), depositoDTO.capacidadMaxima());

    Deposito guardado = depositoRepository.save(deposito);

    // Metrica de deposito creado
    Metrics.counter("logistica.depositos.creados").increment();

    return new DepositoDTO(guardado.getId().toString(), guardado.getAlgoritmoMatchmaking(), guardado.getNombre(), guardado.getDireccion(), guardado.getCapacidadMaxima(), List.of());

  }

  @Override
  public DepositoDTO buscarDepositoPorID(String depositoID) throws NoSuchElementException {

    Deposito deposito = depositoRepository.findById(Long.parseLong(depositoID)).orElseThrow(NoSuchElementException :: new);

    return new DepositoDTO(deposito.getId().toString(), deposito.getAlgoritmoMatchmaking(), deposito.getNombre(), deposito.getDireccion(), deposito.getCapacidadMaxima(), List.of());
  }

  @Override
  public AsignacionDTO buscarAsignacionPorPaqueteID(String paqueteID) throws NoSuchElementException {

    Asignacion asignacion = asignacionRepository.findByIdPaquete(paqueteID).orElseThrow(NoSuchElementException :: new);

    return new AsignacionDTO(asignacion.getId().toString(), asignacion.getIdPaquete(), asignacion.getIdEntidad(), LocalDateTime.now(), EstadoAsginacionEnum.valueOf(asignacion.getEstado().name()));
  }

  @Override
  public DepositoDTO gestionarDonacion(String depositoID, String donacionID, String productoID, Integer cantidad) throws NoSuchElementException {

    Deposito deposito = depositoRepository.findById(Long.parseLong(depositoID)).orElseThrow(NoSuchElementException::new);

    if(cantidad <= 0){
      throw new IllegalArgumentException("Cantidad de producto invalida");
    }


    List<NecesidadMaterialDTO> necesidades = donadoresYEntidadesClient.obtenerNecesidadesInsatisfechasDe(productoID);

    if(necesidades.isEmpty()){
      return new DepositoDTO(deposito.getId().toString(), deposito.getAlgoritmoMatchmaking(), deposito.getNombre(), deposito.getDireccion(), deposito.getCapacidadMaxima(), List.of());
    }


    for (NecesidadMaterialDTO necesidad : necesidades) {
        if (necesidad.tipo() == TipoNecesidadMaterialEnum.RECURRENTE && cantidad < necesidad.cantidadObjetivo()) {
            throw new IllegalArgumentException(
                "No se permiten donaciones parciales para necesidades recurrentes"
        );
      }
    }

    String idPaquete = UUID.randomUUID().toString();

    PaqueteDTO paqueteDTO = new PaqueteDTO(idPaquete, donacionID, productoID, cantidad);

    AsignacionDTO asignacion = ejecutarMatchmaking(depositoID, paqueteDTO, necesidades);

    // Metrica de donacion procesada
    Metrics.counter("logistica.donaciones.gestionadas").increment();

    return new DepositoDTO(deposito.getId().toString(), deposito.getAlgoritmoMatchmaking(), deposito.getNombre(),deposito.getDireccion(), deposito.getCapacidadMaxima(), List.of());

  }

  @Override
  public void setAlgoritmoMM(String depositoID, TipoAlgoritmoEnum algoritmo) {
    Deposito deposito = depositoRepository.findById(Long.parseLong(depositoID)).orElseThrow(NoSuchElementException::new);

    deposito.setAlgoritmoMatchmaking(algoritmo);

    depositoRepository.save(deposito);
  }

  @Override
  public AsignacionDTO ejecutarMatchmaking(String depositoID, PaqueteDTO paqueteDTO, List<NecesidadMaterialDTO> necesidades) {

    if (paqueteDTO == null) {
      throw new RuntimeException();
    }

    Deposito deposito = depositoRepository.findById(Long.parseLong(depositoID)).orElseThrow(NoSuchElementException::new);

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


    String idNecesidad = necesidadSeleccionada.id();

    if (idNecesidad == null) {
      throw new IllegalStateException("La necesidad seleccionada no tiene ID válido");
    }


    Asignacion asignacion = new Asignacion(paqueteDTO.id(), idNecesidad);

    Asignacion guardada = asignacionRepository.save(asignacion);

    // Metrica de asignacion creada
    Metrics.counter("logistica.asignaciones.generadas").increment();

    return new AsignacionDTO(guardada.getId().toString(), guardada.getIdPaquete(), guardada.getIdEntidad(), LocalDateTime.now(), EstadoAsginacionEnum.valueOf(guardada.getEstado().name()));

  }



  @Override
  public void reportarEntrega(PaqueteDTO paqueteDTO) {

    if (paqueteDTO == null) {
      throw new RuntimeException();
    }

    Asignacion asignacion = asignacionRepository.findByIdPaquete(paqueteDTO.id()).orElseThrow(NoSuchElementException::new);

    donadoresYEntidadesClient.satisfacerNecesidad(asignacion.getIdEntidad(), paqueteDTO.cantidad());

    donacionesClient.cambiarEstadoDeDonacion(paqueteDTO.donacionID(), EstadoDonacionEnum.ACEPTADA);

    asignacion.completarEntrega();

    Metrics.counter("logistica.entregas.reportadas").increment();
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

