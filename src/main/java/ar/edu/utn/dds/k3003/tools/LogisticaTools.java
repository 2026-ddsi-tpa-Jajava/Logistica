package ar.edu.utn.dds.k3003.tools;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.AsignacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.DepositoDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.PaqueteDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.TipoAlgoritmoEnum;
import ar.edu.utn.dds.k3003.model.EstadoAsignacion;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LogisticaTools {

    private final Fachada fachada;

    public LogisticaTools(Fachada fachada) {
        this.fachada = fachada;
    }

    @PostConstruct
    public void init() {
        System.out.println("LOGISTICA TOOLS CARGADAS");
    }

    @Tool(name = "crear_deposito", description = "Crea un nuevo depósito") public DepositoDTO crearDeposito(@ToolParam(description = "Nombre del depósito", required = true) String nombre,
                                                                                                            @ToolParam(description = "Dirección del depósito", required = true) String direccion,
                                                                                                            @ToolParam(description = "Capacidad máxima", required = true) Integer capacidadMaxima) {

        return fachada.agregarDeposito(new DepositoDTO(null, null, nombre, direccion, capacidadMaxima, List.of()));

    }

    @Tool(name = "consultar_depositos", description = "Obtiene todos los depósitos registrados")

    public List<DepositoDTO> consultarDepositos() {

        System.out.println("MCP TOOL consultar_depositos EJECUTADA");

        return fachada.obtenerDepositos();

    }

    @Tool(name = "consultar_deposito", description = "Obtiene un depósito por ID")

    public DepositoDTO consultarDeposito(@ToolParam(description = "ID del depósito", required = true) String depositoID) {

        System.out.println("MCP TOOL consultar_deposito EJECUTADA");

        return fachada.buscarDepositoPorID(depositoID);

    }

    @Tool(name = "consultar_stock", description = "Obtiene el stock de un depósito")

    public List<PaqueteDTO> consultarStock(@ToolParam(description = "ID del depósito", required = true) String depositoID) {

        return fachada.obtenerStock(depositoID);

    }

    @Tool(name = "consultar_asignaciones", description = "Obtiene todas las asignaciones")

    public List<AsignacionDTO> consultarAsignaciones() {

        return fachada.obtenerAsignaciones();

    }

    @Tool(name = "consultar_asignaciones_estado", description = "Obtiene las asignaciones por estado")

    public List<AsignacionDTO> consultarAsignacionesEstado(@ToolParam(description = "ASIGNADA o COMPLETADA", required = true) String estado) {

        return fachada.obtenerAsignacionesPorEstado(EstadoAsignacion.valueOf(estado.toUpperCase()));

    }

    @Tool(name = "reportar_entrega", description = "Reporta la entrega de un paquete")

    public String reportarEntrega(@ToolParam(description = "ID del paquete", required = true) String paqueteID,
                                  @ToolParam(description = "ID de la donación", required = true) String donacionID,
                                  @ToolParam(description = "Producto", required = true) String productoID,
                                  @ToolParam(description = "Cantidad", required = true) Integer cantidad) {

        fachada.reportarEntrega(new PaqueteDTO(paqueteID, donacionID, productoID, cantidad));

        return "Entrega reportada correctamente";

    }

    @Tool(name = "consultar_stock_producto", description = "Obtiene la cantidad total disponible de un producto en todos los depósitos") public Integer consultarStockProducto(
            @ToolParam(description = "ID del producto", required = true) String productoID) {

        return fachada.obtenerCantidadStockPorProducto(productoID);

    }

    @Tool(name = "consultar_asignacion_paquete", description = "Obtiene una asignación a partir del ID del paquete")

    public AsignacionDTO consultarAsignacionPorPaquete(@ToolParam(description = "ID del paquete", required = true) String paqueteID) {

        return fachada.buscarAsignacionPorPaqueteID(paqueteID);

    }

    @Tool(
            name = "vaciar_stock",
            description = "Elimina todos los paquetes almacenados en un depósito"
    )
    public String vaciarStock(@ToolParam(description = "ID del depósito", required = true) String depositoID) {

        fachada.vaciarStock(depositoID);

        return "Stock eliminado correctamente";

    }

    @Tool(name = "configurar_algoritmo", description = "Configura el algoritmo de matchmaking de un depósito") public String configurarAlgoritmo(@ToolParam(description = "ID del depósito", required = true) String depositoID,
                                                                                                                                                 @ToolParam(description = "SUB_ATENDIDOS o PRIORIDAD_POR_SCORE", required = true) String algoritmo) {
        fachada.setAlgoritmoMM(depositoID, TipoAlgoritmoEnum.valueOf(algoritmo.toUpperCase()));

        return "Algoritmo configurado correctamente";

    }

    @Tool(name = "eliminar_depositos", description = "Elimina todos los depósitos") public String eliminarDepositos() {

        fachada.eliminarTodosLosDepositos();

        return "Depósitos eliminados correctamente";

    }

    @Tool(name = "eliminar_asignaciones", description = "Elimina todas las asignaciones") public String eliminarAsignaciones() {

        fachada.eliminarTodasLasAsignaciones();

        return "Asignaciones eliminadas correctamente";

    }

    @Tool(name = "eliminar_paquetes", description = "Elimina todos los paquetes almacenados en todos los depósitos")

    public String eliminarPaquetes() {

        fachada.eliminarTodosLosPaquetes();

        return "Paquetes eliminados correctamente";
    }

    @Tool(name = "consultar_cantidad_stock", description = "Obtiene la cantidad total de stock disponible para un producto")

    public Integer consultarCantidadStock(@ToolParam(description = "ID del producto", required = true) String productoID) {

        return fachada.obtenerCantidadStockPorProducto(productoID);

    }

    @Tool(name = "consultar_stock_total_deposito", description = "Obtiene la cantidad total de productos almacenados en un depósito")

    public Integer consultarStockTotalDeposito(@ToolParam(description = "ID del depósito", required = true) String depositoID) {

        DepositoDTO deposito = fachada.buscarDepositoPorID(depositoID);

        return deposito.stockActual().stream().mapToInt(PaqueteDTO::cantidad).sum();

    }

    @Tool(name = "prueba_logistica", description = "Tool de prueba")

    public String prueba(){

        System.out.println("TOOL PRUEBA EJECUTADA");

        return "Hola desde Logistica";
    }

}