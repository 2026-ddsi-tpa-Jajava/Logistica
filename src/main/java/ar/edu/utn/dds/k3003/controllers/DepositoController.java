package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.DepositoDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.PaqueteDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;


@RestController
@RequestMapping("/depositos")
public class DepositoController {

    private Fachada fachada;

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
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (RuntimeException e) {
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
            return ResponseEntity.notFound().build();

        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body("Error interno al buscar el depósito");
        }
    }

    // POST gestionar donacion
    @PostMapping("/{id}/donacion")
    public ResponseEntity<?> gestionarDonacion(@PathVariable String id, @RequestBody PaqueteDTO paquete) {

        try {
            DepositoDTO depositoActualizado = fachada.gestionarDonacion(id, paquete.donacionID(), paquete.producto(), paquete.cantidad());
            return ResponseEntity.ok(depositoActualizado);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();

        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body("Error interno al gestionar la donación");
        }
    }
}
