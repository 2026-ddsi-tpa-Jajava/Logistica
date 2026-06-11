package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import org.springframework.stereotype.Component;


@Component
public class DonacionesClient {

    public DonacionDTO cambiarEstadoDeDonacion(String id, EstadoDonacionEnum estado) {
        try {
            return HttpClientBuilder.post("https://donaciones-5u8i.onrender.com/donaciones/" + id, estado, DonacionDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}