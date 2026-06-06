package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DonadoresYEntidadesClient {

    public List<NecesidadMaterialDTO> obtenerNecesidadesInsatisfechasDe(String productoID) {
        try {
            return HttpClientBuilder.get("https://agusb1101-donadores-entidades.onrender.com/necesidades/" + productoID, new TypeReference<List<NecesidadMaterialDTO>>() {});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void satisfacerNecesidad(String necesidadID, Integer cantidad) {
        try {
            HttpClientBuilder.post("https://agusb1101-donadores-entidades.onrender.com/necesidades/" + necesidadID + "/satisfaccion", cantidad, Void.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
