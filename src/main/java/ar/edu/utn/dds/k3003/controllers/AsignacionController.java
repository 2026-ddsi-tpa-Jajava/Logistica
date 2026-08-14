package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.AsignacionDTO;
import ar.edu.utn.dds.k3003.model.EstadoAsignacion;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;


@RestController
@RequestMapping("/asignaciones")
public class AsignacionController {

    private Fachada fachada;

    public AsignacionController(Fachada fachada) {
        this.fachada = fachada;
    }

    // GET todas las asignaciones
    @GetMapping
    public ResponseEntity<?> obtenerAsignaciones() {

        try {

            return ResponseEntity.ok(
                    fachada.obtenerAsignaciones()
            );

        } catch (Exception e) {

            return ResponseEntity.internalServerError()
                    .body(e.getMessage());
        }
    }

    //GET Asignacion por estado
    @GetMapping("/estado/{estado}")
    public ResponseEntity<?> obtenerPorEstado(@PathVariable String estado) {

        return ResponseEntity.ok(fachada.obtenerAsignacionesPorEstado(EstadoAsignacion.valueOf(estado)));
    }

    // GET asignacion de un paquete
    @GetMapping("/{idPaquete}")
    public ResponseEntity<?> buscarAsignacionPorPaquete(@PathVariable String idPaquete) {

        try {
            AsignacionDTO asignacion = fachada.buscarAsignacionPorPaqueteID(idPaquete);
            return ResponseEntity.ok(asignacion);

        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();

        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body("Error interno al buscar la asignación");
        }
    }

    @PostMapping
    public ResponseEntity<?> crearAsignacion(
            @RequestBody Map<String, Object> body) {

        try {
            AsignacionDTO dto = fachada.crearAsignacion(body);
            return ResponseEntity.ok(dto);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(e.getMessage());
        }
    }



    @DeleteMapping
    public ResponseEntity<Void> eliminarTodasLasAsignaciones() {
        fachada.eliminarTodasLasAsignaciones();
        return ResponseEntity.noContent().build();
    }
}