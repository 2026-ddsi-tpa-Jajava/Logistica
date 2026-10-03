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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Fachada implements FachadaLogistica {

  public Fachada() {
  }

  private static final Logger log = LoggerFactory.getLogger(Fachada.class);

  private Long ultimoIdPaquete = 0L;

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

  private String generarIdPaquete() {

    ultimoIdPaquete++;

    return ultimoIdPaquete.toString();

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
                    a.getNecesidadID(),
                    a.getIdEntidad(),
                    LocalDateTime.now(),
                    EstadoAsginacionEnum.valueOf(a.getEstado().name())
            ))
            .toList();
  }

  public List<PaqueteDTO> obtenerStock(String depositoID) {

    Deposito deposito = depositoRepository.findById(Long.parseLong(depositoID)).orElseThrow(NoSuchElementException::new);

    return obtenerStockDTO(deposito);
  }

  public void vaciarStock(String depositoID) {

    Deposito deposito = depositoRepository.findById(Long.parseLong(depositoID)).orElseThrow(NoSuchElementException::new);

    deposito.getStockActual().clear();

    deposito.setCantidadStock(0);

    depositoRepository.save(deposito);
  }

  public List<AsignacionDTO> obtenerAsignacionesPorEstado(EstadoAsignacion estado) {

    return asignacionRepository.findByEstado(estado)
            .stream()
            .map(a -> new AsignacionDTO(
                    a.getId().toString(),
                    a.getIdPaquete(),
                    a.getNecesidadID(),
                    a.getIdEntidad(),
                    LocalDateTime.now(),
                    EstadoAsginacionEnum.valueOf(a.getEstado().name())
            ))
            .toList();
  }

  public Integer obtenerCantidadStockPorProducto(
          String productoID) {

    return depositoRepository.findAll()
            .stream()
            .flatMap(
                    d -> d.getStockActual().stream()
            )
            .filter(
                    p -> Objects.equals(
                            p.getProducto(),
                            productoID
                    )
            )
            .mapToInt(
                    Paquete::getCantidad
            )
            .sum();
  }

  public void eliminarTodosLosPaquetes() {

    depositoRepository.findAll().forEach(deposito -> {deposito.getStockActual().clear(); deposito.setCantidadStock(0); depositoRepository.save(deposito);});
  }

  public AsignacionDTO crearAsignacionDesdeStock(Map<String,String> body) {

    log.info("BODY RECIBIDO = {}", body);

    String paqueteID = body.get("paqueteID");

    String necesidadID =  body.get("necesidadID");

    Integer cantidadAsignada = Integer.valueOf((body.get("cantidadAsignada")));

    String idEntidad = body.get("entidadID");

    log.info("Consumiendo stock. paquete={} cantidadAsignada={}", paqueteID, cantidadAsignada);

    Long idPaquete = Long.parseLong(paqueteID);

    Deposito deposito = depositoRepository.findAll()
            .stream()
            .filter(d -> d.getStockActual()
                    .stream()
                    .anyMatch(p -> p.getId().equals(idPaquete)))
            .findFirst()
            .orElseThrow();

    Paquete paquete = deposito.getStockActual()
            .stream()
            .filter(p -> p.getId().equals(idPaquete))
            .findFirst()
            .orElseThrow();

    if (cantidadAsignada > paquete.getCantidad()) {

      throw new IllegalArgumentException(
              "No hay stock suficiente en el paquete"
      );
    }

    Integer restante =
            paquete.getCantidad() - cantidadAsignada;

    if (restante == 0) {

      deposito.getStockActual().remove(paquete);

    } else {

      paquete.setCantidad(restante);
    }

    deposito.setCantidadStock(
            deposito.getCantidadStock()
                    - cantidadAsignada
    );

    depositoRepository.save(deposito);


    Asignacion asignacion = new Asignacion(paqueteID, idEntidad, necesidadID, cantidadAsignada, "STOCK");

    // LOG DE ASIGNACION CREADA

    log.info("Asignacion creada necesidad={} cantidad={} origen=STOCK", necesidadID, cantidadAsignada);

    Asignacion guardada = asignacionRepository.save(asignacion);

    return new AsignacionDTO(
            guardada.getId().toString(),
            guardada.getIdPaquete(),
            guardada.getNecesidadID(),
            guardada.getIdEntidad(),
            LocalDateTime.now(),
            EstadoAsginacionEnum.ASIGNADA
    );
  }

  // ---------------------------------------TAREAS DELEGADAS AL WORKER--------------------------------------------------

  public void procesarDonacionWorker(String depositoID, String donacionID, String productoID, Integer cantidad){

    // LOG PARA QUE SE VEA QUE EL WORKER ESTA PROCESANDO DONACION

    log.info("Worker procesando donacion {}", donacionID);

    DepositoDTO deposito = logisticaClient.obtenerDeposito(depositoID);

    List<NecesidadMaterialDTO> necesidades = donadoresYEntidadesClient.obtenerNecesidadesInsatisfechasDe(productoID);

    System.out.println("Necesidades encontradas: " + necesidades.size());

    if (necesidades.isEmpty()) {
      /*
      Map<String,Object> body = Map.of(
              "depositoID", depositoID,
              "donacionID", donacionID,
              "productoID", productoID,
              "cantidad", cantidad
      );
  */

      Map<String,Object> body = new HashMap<>();

      body.put("depositoID", depositoID);
      body.put("donacionID", donacionID);
      body.put("productoID", productoID);
      body.put("cantidad", cantidad);

      log.info(
              "BODY STOCK depositoID={} donacionID={} productoID={} cantidad={}",
              depositoID,
              donacionID,
              productoID,
              cantidad
      );

      Metrics.counter("logistica.worker.post_stock").increment();

      System.out.println("MANDANDO A STOCK");
      System.out.println(body);
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
      /*
      Map<String,Object> body = Map.of(
              "depositoID", depositoID,
              "donacionID", donacionID,
              "productoID", productoID,
              "cantidad", cantidad
      );
    */

      Map<String,Object> body = new HashMap<>();

      body.put("depositoID", depositoID);
      body.put("donacionID", donacionID);
      body.put("productoID", productoID);
      body.put("cantidad", cantidad);

      log.info(
              "BODY STOCK depositoID={} donacionID={} productoID={} cantidad={}",
              depositoID,
              donacionID,
              productoID,
              cantidad
      );
      Metrics.counter("logistica.worker.post_stock").increment();

      logisticaClient.agregarStock(body);

      return;
    }

    String idPaquete = generarIdPaquete();

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
            "idEntidad", necesidadSeleccionada.entidadID(),
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

    // System.out.println("ENTRO A agregarStock");

    String depositoID = (String) body.get("depositoID");

    String donacionID = (String) body.get("donacionID");

    String productoID = (String) body.get("productoID");

    Integer cantidad = (Integer) body.get("cantidad");

    // LOG PARA DECIR QUE SE ESTA AGREGANDO STOCK
    log.info("Agregando stock. deposito={} donacion={} producto={} cantidad={}", depositoID, donacionID, productoID, cantidad);

    Deposito deposito = depositoRepository.findById(Long.parseLong(depositoID)).orElseThrow();

    Paquete paquete = new Paquete(donacionID, productoID, cantidad);

    deposito.agregarPaqueteAlStock(paquete);

    depositoRepository.save(deposito);

    System.out.println("STOCK GUARDADO");
  }

  public AsignacionDTO crearAsignacion(Map<String, Object> body) {


    String depositoID = (String) body.get("depositoID");

    String paqueteID = (String) body.get("paqueteID");

    String necesidadID = (String) body.get("necesidadID");

    String idEntidad = (String) body.get("idEntidad");

    Integer cantidadAsignada = (Integer) body.get("cantidadAsignada");

    Integer sobrante = (Integer) body.get("sobrante");

    String donacionID = (String) body.get("donacionID");

    String productoID = (String) body.get("productoID");

    // LOG PARA DECIR QUE SE ESTA CREANDO UNA ASIGNACION
    log.info("Creando asignacion. necesidad={} entidad={} cantidad={} origen=MATCHMAKING", necesidadID, idEntidad, cantidadAsignada);

    Deposito deposito = depositoRepository.findById(Long.parseLong(depositoID)).orElseThrow();

    Asignacion  asignacion = new Asignacion(
                    paqueteID,
                    idEntidad,
                    necesidadID,
                    cantidadAsignada,
                    "MATCHMAKING"
            );

    if (sobrante > 0) {

      Metrics.counter("logistica.sobrantes.generados").increment();

      Paquete paqueteSobrante = new Paquete(donacionID, productoID, sobrante);

      deposito.agregarPaqueteAlStock(paqueteSobrante);

      depositoRepository.save(deposito);
    }

    Asignacion guardada = asignacionRepository.save(asignacion);

    Metrics.counter("logistica.asignaciones.generadas").increment();

    return new AsignacionDTO(
            guardada.getId().toString(),
            guardada.getIdPaquete(),
            guardada.getNecesidadID(),
            guardada.getIdEntidad(),
            LocalDateTime.now(),
            EstadoAsginacionEnum.ASIGNADA
    );
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

    return new AsignacionDTO(
            asignacion.getId().toString(),
            asignacion.getIdPaquete(),
            asignacion.getNecesidadID(),
            asignacion.getIdEntidad(),
            LocalDateTime.now(),
            EstadoAsginacionEnum.valueOf(asignacion.getEstado().name())
    );
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
    System.out.println("PUBLICANDO " + donacionID);

    // LOG SOBRE GESTION DE LA DONACION

    log.info("Gestionando donacion id={} producto={} cantidad={}", donacionID, productoID, cantidad);

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

    String necesidadID = necesidadSeleccionada.id();

    // Me fijo lo que va a sobrar para despues guardarlo en el stock
    Integer cantidadAsignada = Math.min(paqueteDTO.cantidad(), necesidadSeleccionada.cantidadObjetivo());

    Integer sobrante = paqueteDTO.cantidad() - cantidadAsignada;


    if (necesidadID == null) {
      throw new IllegalStateException("La necesidad seleccionada no tiene ID válido");
    }


    Asignacion asignacion =
            new Asignacion(
                    paqueteDTO.id(),
                    necesidadSeleccionada.entidadID(),
                    necesidadID,
                    cantidadAsignada,
                    "MATCHMAKING"
            );

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

    return new AsignacionDTO(
            guardada.getId().toString(),
            guardada.getIdPaquete(),
            guardada.getNecesidadID(),
            guardada.getIdEntidad(),
            LocalDateTime.now(),
            EstadoAsginacionEnum.valueOf(guardada.getEstado().name())
    );

  }

  @Override
  public void reportarEntrega(PaqueteDTO paqueteDTO) {

    // System.out.println("ENTRO A REPORTAR ENTREGA");

    //LOG PARA DECIR QUE SE ESTA REPORTANDO UNA ENTREGA
    log.info("Reportando entrega donacion={}", paqueteDTO.donacionID());

    if (paqueteDTO == null) {
      throw new RuntimeException();
    }

    Asignacion asignacion = asignacionRepository.findByIdPaquete(paqueteDTO.id()).orElseThrow(NoSuchElementException::new);

    System.out.println("ASIGNACION ENCONTRADA");

    donadoresYEntidadesClient.satisfacerNecesidad(asignacion.getNecesidadID(), asignacion.getCantidadAsignada());

    System.out.println("SATISFACER NECESIDAD OK");

    System.out.println("CAMBIANDO DONACION");

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

