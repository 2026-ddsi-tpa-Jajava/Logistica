package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.catedra.dtos.logistica.AsignacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.DepositoDTO;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LogisticaClient {


    public DepositoDTO obtenerDeposito(String depositoID) {

        try {
            return HttpClientBuilder.get("https://logistica-hjaw.onrender.com" + "/depositos/" + depositoID, DepositoDTO.class);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public AsignacionDTO crearAsignacion(Map<String, Object> body) {

        try {

            return HttpClientBuilder.post("https://logistica-hjaw.onrender.com" + "/asignaciones", body, AsignacionDTO.class);

        } catch (Exception e) {

            throw new RuntimeException(e);
            }
    }

    public void agregarStock(Map<String,Object> body) {

        try {

            HttpClientBuilder.post("https://logistica-hjaw.onrender.com" + "/stock", body, Void.class);

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }
}

