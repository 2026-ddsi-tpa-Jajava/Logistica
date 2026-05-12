package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.PaqueteDTO;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaLogistica;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;


@RestController
@RequestMapping("/entregas")
public class EntregaController {

    private final FachadaLogistica fachada = new Fachada();

    // POST reportar entrega
    @PostMapping
    public ResponseEntity<?> reportarEntrega(@RequestBody PaqueteDTO paquete) {

        try {
            fachada.reportarEntrega(paquete);
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();

        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body("Error interno del sistema");
        }
    }
}