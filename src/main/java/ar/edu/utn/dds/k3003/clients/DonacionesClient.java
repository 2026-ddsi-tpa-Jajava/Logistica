package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
public class DonacionesClient {



    public DonacionDTO cambiarEstadoDeDonacion(String id, EstadoDonacionEnum estado) {

        try {

            Map<String, String> body = Map.of("estado", estado.name());

            return HttpClientBuilder.patch("https://donaciones-5u8i.onrender.com/donaciones/" + id + "/estado", body, DonacionDTO.class);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}