package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.AsignacionDTO;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaLogistica;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;


@RestController
@RequestMapping("/asignaciones")
public class AsignacionController {

    private final FachadaLogistica fachada = new Fachada();

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
}