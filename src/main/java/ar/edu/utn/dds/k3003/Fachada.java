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
import ar.edu.utn.dds.k3003.clients.HttpClientBuilder;
import ar.edu.utn.dds.k3003.clients.LogisticaClient;
import ar.edu.utn.dds.k3003.exceptions.DonadorNoEncontradoException;
import ar.edu.utn.dds.k3003.exceptions.DonadorYaExistenteException;
import ar.edu.utn.dds.k3003.model.*;
import ar.edu.utn.dds.k3003.repositories.*;

import java.time.LocalDateTime;
import java.util.*;

import io.micrometer.core.instrument.Metrics;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Fachada implements FachadaLogistica {

  public Fachada() {
  }

  // ------------------------------------------INYECCION DE CLIENTS-----------------------------------------------------

  @Autowired
  private DonacionesClient donacionesClient;
  @Autowired
  private DonadoresYEntidadesClient donadoresYEntidadesClient;
  @Autowired
  private LogisticaClient logisticaClient;

  @Autowired
  private PublisherDonacion publisherDonacion;

  @Autowired
  private DepositoRepository depositoRepository;
  @Autowired
  private AsignacionRepository asignacionRepository;

  private FachadaDonadoresYEntidades fachadaDonadoresYEntidades;

  private FachadaDonaciones fachadaDonaciones;

  // ----------------------------FUNCIONES UTILIZADAS EN LOS METODOS DE CONTRATO---------------------------------------

  private double calcularScore(NecesidadMaterialDTO necesidad) {

    return necesidad.nivelDeUrgencia() / (double) necesidad.cantidadObjetivo();

  }

  private List<PaqueteDTO> obtenerStockDTO(Deposito deposito) {

    List<PaqueteDTO> paquetesDTO = new ArrayList<>();

    for (Paquete paquete : deposito.getStockActual()) {

      paquetesDTO.add(new PaqueteDTO(paquete.getId().toString(), paquete.getDonacionID(), paquete.getProducto(), paquete.getCantidad()));

    }

    return paquetesDTO;

  }

  // ---------------------------------------------ENDPOINTS ADICIONALES-------------------------------------------------

  public List<DepositoDTO> obtenerDepositos() {
    return depositoRepository.findAll().stream().map(deposito -> new DepositoDTO(
            deposito.getId().toString(),
            deposito.getAlgoritmoMatchmaking(),
            deposito.getNombre(),
            deposito.getDireccion(),
            deposito.getCapacidadMaxima(),
            obtenerStockDTO(deposito)
    )).toList();
  }

  public List<AsignacionDTO> obtenerAsignaciones() {

    return asignacionRepository.findAll()
            .stream()
            .map(a -> new AsignacionDTO(
                    a.getId().toString(),
                    a.getIdPaquete(),
                    a.getIdEntidad(),
                    LocalDateTime.now(),
                    EstadoAsginacionEnum.valueOf(
                            a.getEstado().name())
            )).toList();
  }

  public List<PaqueteDTO> obtenerStock(String depositoID) {

    Deposito deposito = depositoRepository.findById(Long.parseLong(depositoID)).orElseThrow(NoSuchElementException::new);

    return obtenerStockDTO(deposito);
  }

  public void vaciarStock(String depositoID) {

    Deposito deposito = depositoRepository.findById(Long.parseLong(depositoID)).orElseThrow(NoSuchElementException::new);

    deposito.getStockActual().clear();

    depositoRepository.save(deposito);
  }

  public List<AsignacionDTO> obtenerAsignacionesPorEstado(EstadoAsignacion estado) {

    return asignacionRepository.findByEstado(estado).stream().map(a -> new AsignacionDTO(
                    a.getId().toString(),
                    a.getIdPaquete(),
                    a.getIdEntidad(),
                    LocalDateTime.now(),
                    EstadoAsginacionEnum.valueOf(
                            a.getEstado().name())
            )).toList();
  }

  // ---------------------------------------TAREAS DELEGADAS AL WORKER--------------------------------------------------

  public void procesarDonacionWorker(String depositoID, String donacionID, String productoID, Integer cantidad){

    DepositoDTO deposito = logisticaClient.obtenerDeposito(depositoID);

    List<NecesidadMaterialDTO> necesidades = donadoresYEntidadesClient.obtenerNecesidadesInsatisfechasDe(productoID);

    System.out.println("Necesidades encontradas: " + necesidades.size());

    if (necesidades.isEmpty()) {

      Map<String,Object> body = Map.of(
              "depositoID", depositoID,
              "donacionID", donacionID,
              "productoID", productoID,
              "cantidad", cantidad
      );

      Metrics.counter("logistica.worker.post_stock").increment();

      logisticaClient.agregarStock(body);

      return;
    }

    List<NecesidadMaterialDTO> necesidadesValidas = new ArrayList<>();

    for (NecesidadMaterialDTO necesidad : necesidades) {

      if (necesidad.tipo() == TipoNecesidadMaterialEnum.EXTRAORDINARIA) {

        necesidadesValidas.add(necesidad);

      }

      else if (necesidad.tipo() == TipoNecesidadMaterialEnum.RECURRENTE && cantidad >= necesidad.cantidadObjetivo()) {

        necesidadesValidas.add(necesidad);

      }
    }

    if (necesidadesValidas.isEmpty()) {

      Map<String,Object> body = Map.of(
              "depositoID", depositoID,
              "donacionID", donacionID,
              "productoID", productoID,
              "cantidad", cantidad
      );

      Metrics.counter("logistica.worker.post_stock").increment();

      logisticaClient.agregarStock(body);

      return;
    }

    String idPaquete = UUID.randomUUID().toString();

    TipoAlgoritmoEnum algoritmo = deposito.algoritmo();

    NecesidadMaterialDTO necesidadSeleccionada;

    if (algoritmo == null || algoritmo == TipoAlgoritmoEnum.SUB_ATENDIDOS) {

      necesidadSeleccionada = necesidadesValidas.stream().max(Comparator.comparing(NecesidadMaterialDTO::cantidadObjetivo)).orElseThrow();

    } else if (algoritmo == TipoAlgoritmoEnum.PRIORIDAD_POR_SCORE) {

      necesidadSeleccionada = necesidadesValidas.stream().max(Comparator.comparing(this::calcularScore)).orElseThrow();

    } else {

      throw new IllegalStateException("Algoritmo no soportado");
    }

    Integer cantidadAsignada = Math.min(cantidad, necesidadSeleccionada.cantidadObjetivo());

    Integer sobrante = cantidad - cantidadAsignada;

    Map<String, Object> body = Map.of(
            "depositoID", depositoID,
            "paqueteID", idPaquete,
            "necesidadID", necesidadSeleccionada.id(),
            "cantidadAsignada", cantidadAsignada,
            "sobrante", sobrante,
            "donacionID", donacionID,
            "productoID", productoID
    );

    Metrics.counter("logistica.worker.post_asignaciones").increment();

    logisticaClient.crearAsignacion(body);

    Metrics.counter("logistica.worker.donaciones.procesadas").increment();

  }

  // ----------------------------REQUESTS QUE HACE EL WORKER------------------------------------------------------------

  public void agregarStock(Map<String,Object> body) {

    String depositoID = (String) body.get("depositoID");

    String donacionID = (String) body.get("donacionID");

    String productoID = (String) body.get("productoID");

    Integer cantidad = (Integer) body.get("cantidad");

    Deposito deposito = depositoRepository.findById(Long.parseLong(depositoID)).orElseThrow();

    Paquete paquete = new Paquete(donacionID, productoID, cantidad);

    deposito.agregarPaqueteAlStock(paquete);

    depositoRepository.save(deposito);
  }

  public AsignacionDTO crearAsignacion(Map<String, Object> body) {

    String depositoID = (String) body.get("depositoID");

    String paqueteID = (String) body.get("paqueteID");

    String necesidadID = (String) body.get("necesidadID");

    Integer cantidadAsignada = (Integer) body.get("cantidadAsignada");

    Integer sobrante = (Integer) body.get("sobrante");

    String donacionID = (String) body.get("donacionID");

    String productoID = (String) body.get("productoID");

    Deposito deposito = depositoRepository.findById(Long.parseLong(depositoID)).orElseThrow();

    Asignacion asignacion = new Asignacion(paqueteID, necesidadID, cantidadAsignada);

    if (sobrante > 0) {

      Metrics.counter("logistica.sobrantes.generados").increment();

      Paquete paqueteSobrante = new Paquete(donacionID, productoID, sobrante);

      deposito.agregarPaqueteAlStock(paqueteSobrante);

      depositoRepository.save(deposito);
    }

    Asignacion guardada = asignacionRepository.save(asignacion);

    Metrics.counter("logistica.asignaciones.generadas").increment();

    return new AsignacionDTO(guardada.getId().toString(), guardada.getIdPaquete(), guardada.getIdEntidad(), LocalDateTime.now(), EstadoAsginacionEnum.ASIGNADA);
  }

  //---------------------------------METODOS DEL CONTRATO DE FACHADALOGISTICA-------------------------------------------

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

    return new DepositoDTO(guardado.getId().toString(), guardado.getAlgoritmoMatchmaking(), guardado.getNombre(), guardado.getDireccion(), guardado.getCapacidadMaxima(), obtenerStockDTO(deposito));

  }

  @Override
  public DepositoDTO buscarDepositoPorID(String depositoID) throws NoSuchElementException {

    Deposito deposito = depositoRepository.findById(Long.parseLong(depositoID)).orElseThrow(NoSuchElementException :: new);

    return new DepositoDTO(deposito.getId().toString(), deposito.getAlgoritmoMatchmaking(), deposito.getNombre(), deposito.getDireccion(), deposito.getCapacidadMaxima(), obtenerStockDTO(deposito));
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

    if(!deposito.tieneLugar(cantidad)) {

      throw new IllegalArgumentException("No hay espacio suficiente en el depósito");

    }

    publisherDonacion.publicar(new MensajeDonacion(depositoID, donacionID, productoID, cantidad));

    // Metrica de donacion procesada
    Metrics.counter("logistica.donaciones.gestionadas").increment();

    return new DepositoDTO(deposito.getId().toString(), deposito.getAlgoritmoMatchmaking(), deposito.getNombre(),deposito.getDireccion(), deposito.getCapacidadMaxima(), obtenerStockDTO(deposito));

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

      Metrics.counter("logistica.matchmaking.sub_atendidos").increment();

      necesidadSeleccionada = necesidades.stream().max(Comparator.comparing(NecesidadMaterialDTO::cantidadObjetivo)).orElseThrow();


    } else if (algoritmo == TipoAlgoritmoEnum.PRIORIDAD_POR_SCORE) {

      Metrics.counter("logistica.matchmaking.prioridad_score").increment();

      necesidadSeleccionada = necesidades.stream().max(Comparator.comparing(this::calcularScore)).orElseThrow();
    }

    else {

      throw new IllegalStateException("Algoritmo de matchmaking no soportado");

    }

    String idNecesidad = necesidadSeleccionada.id();

    // Me fijo lo que va a sobrar para despues guardarlo en el stock
    Integer cantidadAsignada = Math.min(paqueteDTO.cantidad(), necesidadSeleccionada.cantidadObjetivo());

    Integer sobrante = paqueteDTO.cantidad() - cantidadAsignada;


    if (idNecesidad == null) {
      throw new IllegalStateException("La necesidad seleccionada no tiene ID válido");
    }


    Asignacion asignacion = new Asignacion(paqueteDTO.id(), idNecesidad, cantidadAsignada);

    if (sobrante > 0) {

      Paquete paqueteSobrante = new Paquete(paqueteDTO.donacionID(), paqueteDTO.producto(), sobrante);

      deposito.agregarPaqueteAlStock(paqueteSobrante);

      depositoRepository.save(deposito);
    }

    System.out.println("Antes del save");
    Asignacion guardada = asignacionRepository.save(asignacion);
    System.out.println("Asignacion guardada: " + guardada.getId());

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

    donadoresYEntidadesClient.satisfacerNecesidad(asignacion.getIdEntidad(), asignacion.getCantidadAsignada());

    donacionesClient.cambiarEstadoDeDonacion(paqueteDTO.donacionID(), EstadoDonacionEnum.ACEPTADA);

    asignacion.completarEntrega();

    asignacionRepository.save(asignacion);

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

  public void eliminarTodosLosDepositos() {
    depositoRepository.deleteAll();
  }

  public void eliminarTodasLasAsignaciones() {
    asignacionRepository.deleteAll();
  }

}

