package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.DepositoDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.PaqueteDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/depositos")
public class DepositoController {

    private final Fachada fachada;
    private static final Logger log = LoggerFactory.getLogger(DepositoController.class);

    public DepositoController(Fachada fachada) {
        this.fachada = fachada;
    }

    // POST deposito
    @PostMapping
    public ResponseEntity<?> crearDeposito(@RequestBody DepositoDTO depositoDTO) {
        try {
            DepositoDTO depositoCreado = fachada.agregarDeposito(depositoDTO);
            return ResponseEntity.ok(depositoCreado);

        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al crear depósito: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (RuntimeException e) {
            log.error("Error interno al crear el depósito", e);
            return ResponseEntity.internalServerError().body("Error interno al crear el depósito");
        }
    }

    // GET depositos
    @GetMapping
    public ResponseEntity<?> obtenerDepositos() {
        try {
            List<DepositoDTO> depositos = fachada.obtenerDepositos();
            return ResponseEntity.ok(depositos);

        } catch (RuntimeException e) {
            log.error("Error interno al obtener los depósitos", e);
            return ResponseEntity.internalServerError().body("Error al obtener los depósitos");
        }
    }

    // GET deposito por id
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerDepositoPorId(@PathVariable String id) {
        try {
            DepositoDTO deposito = fachada.buscarDepositoPorID(id);
            return ResponseEntity.ok(deposito);

        } catch (NoSuchElementException e) {
            log.warn("Depósito no encontrado. id={}", id, e);
            return ResponseEntity.notFound().build();

        } catch (RuntimeException e) {
            log.error("Error interno al buscar el depósito. id={}", id, e);
            return ResponseEntity.internalServerError().body("Error interno al buscar el depósito");
        }
    }

    @GetMapping("/{id}/stock")
    public ResponseEntity<?> obtenerStock(@PathVariable String id) {

        try {

            return ResponseEntity.ok(fachada.obtenerStock(id));

        } catch (NoSuchElementException e) {

            return ResponseEntity.notFound().build();

        } catch (Exception e) {

            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // POST gestionar donacion
    @PostMapping("/{id}/donacion")
    public ResponseEntity<?> gestionarDonacion(@PathVariable String id, @RequestBody PaqueteDTO paquete) {
        try {
            DepositoDTO depositoActualizado = fachada.gestionarDonacion(
                    id,
                    paquete.donacionID(),
                    paquete.producto(),
                    paquete.cantidad()
            );
            return ResponseEntity.ok(depositoActualizado);

        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al gestionar donación. depositoId={}, paquete={}", id, paquete, e);
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (NoSuchElementException e) {
            log.warn("Depósito no encontrado al gestionar donación. depositoId={}, paquete={}", id, paquete, e);
            return ResponseEntity.notFound().build();

        } catch (RuntimeException e) {
            log.error("Error interno al gestionar la donación. depositoId={}, paquete={}", id, paquete, e);
            return ResponseEntity.internalServerError().body("Error interno al gestionar la donación");
        }
    }

    // esto lo usa el worker
    @PostMapping("/stock")
    public ResponseEntity<?> agregarStock(@RequestBody Map<String,Object> body) {

        fachada.agregarStock(body);

        return ResponseEntity.ok().build();
    }

    // GET cantidad de stock de determinado producto
    @GetMapping("/stock/{productoID}")
    public ResponseEntity<?> obtenerCantidadStockPorProducto(@PathVariable String productoID) {

        Integer cantidad = fachada.obtenerCantidadStockPorProducto(productoID);

        return ResponseEntity.ok(cantidad);
    }


    @DeleteMapping
    public ResponseEntity<Void> eliminarTodosLosDepositos() {
        fachada.eliminarTodosLosDepositos();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/stock")
    public ResponseEntity<?> vaciarStock(@PathVariable String id) {

        try {

            fachada.vaciarStock(id);

            return ResponseEntity.noContent().build();

        } catch (NoSuchElementException e) {

            return ResponseEntity.notFound().build();

        } catch (Exception e) {

            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }


}