package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.TipoNecesidadMaterialEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.*;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonaciones;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaLogistica;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LogisticaTest {

    FachadaLogistica fachadaLogistica;

    @Mock
    FachadaDonadoresYEntidades fachadaDonadoresYEntidades;

    @Mock
    FachadaDonaciones fachadaDonaciones;

    @Test
    void agregarDepositoTest(){

        fachadaLogistica = new Fachada();

        DepositoDTO depositoSinID = new DepositoDTO(null, null, "Deposito UTN", "Av. Medrano 951", 50, null);

        DepositoDTO resultado = fachadaLogistica.agregarDeposito(depositoSinID);

        assertNotNull(resultado);
        assertNotNull(resultado.id());
        assertEquals("Deposito UTN", resultado.nombre());
        assertEquals("Av. Medrano 951", resultado.direccion());
        assertEquals(50, resultado.capacidadMaxima());
    }

    @Test
    void buscarDepositoPorIDTest(){

        fachadaLogistica = new Fachada();

        DepositoDTO deposito = fachadaLogistica.agregarDeposito(new DepositoDTO("10", null, "Deposito UBA", "Viamonte 430", 10, null));

        DepositoDTO resultado = fachadaLogistica.buscarDepositoPorID(deposito.id());

        assertNotNull(resultado);
        assertEquals(deposito.id(), resultado.id());
        assertEquals("Deposito UBA", resultado.nombre());
        assertEquals("Viamonte 430", resultado.direccion());
        assertEquals(10, resultado.capacidadMaxima());

    }


    @Test
    void ejecutarMatchmakingSubAtendidosTest() {

        fachadaLogistica = new Fachada();

        DepositoDTO deposito =
                fachadaLogistica.agregarDeposito(
                        new DepositoDTO(null, null, "Deposito Test", "Direccion", 100, null)
                );

        fachadaLogistica.setAlgoritmoMM(deposito.id(), TipoAlgoritmoEnum.SUB_ATENDIDOS);

        PaqueteDTO paquete = new PaqueteDTO("1", "donacion1", "fideos", 5);

        NecesidadMaterialDTO necesidad1 =
                new NecesidadMaterialDTO("n1", "e1", 3, "descripcion", 10, "fideos", TipoNecesidadMaterialEnum.EXTRAORDINARIA);

        NecesidadMaterialDTO necesidad2 =
                new NecesidadMaterialDTO("n2", "e2", 5, "descripcion", 20, "fideos", TipoNecesidadMaterialEnum.EXTRAORDINARIA);

        List<NecesidadMaterialDTO> necesidades = List.of(necesidad1, necesidad2);

        AsignacionDTO asignacion =
                fachadaLogistica.ejecutarMatchmaking(deposito.id(), paquete, necesidades);

        assertNotNull(asignacion);
        assertEquals("n2", asignacion.necesidadID());
    }

    @Test
    void ejecutarMatchmakingPorScoreTest() {

        fachadaLogistica = new Fachada();

        DepositoDTO deposito =
                fachadaLogistica.agregarDeposito(
                        new DepositoDTO(null, null, "Deposito Test", "Direccion", 100, null)
                );

        fachadaLogistica.setAlgoritmoMM(deposito.id(), TipoAlgoritmoEnum.PRIORIDAD_POR_SCORE);

        PaqueteDTO paquete = new PaqueteDTO("1", "donacion1", "arroz", 10);

        NecesidadMaterialDTO necesidad1 =
                new NecesidadMaterialDTO("n1", "e1", 10, "descripcion", 100, "arroz", TipoNecesidadMaterialEnum.EXTRAORDINARIA);

        NecesidadMaterialDTO necesidad2 =
                new NecesidadMaterialDTO("n2", "e2", 5, "descripcion", 20, "arroz", TipoNecesidadMaterialEnum.EXTRAORDINARIA);

        List<NecesidadMaterialDTO> necesidades = List.of(necesidad1, necesidad2);

        AsignacionDTO asignacion =
                fachadaLogistica.ejecutarMatchmaking(deposito.id(), paquete, necesidades);

        assertNotNull(asignacion);
        assertEquals("n2", asignacion.necesidadID());
    }

    @Test
    void gestionarDonacionRecurrenteParcialTest() {

        fachadaLogistica = new Fachada();
        fachadaLogistica.setFachadaDonadoresYEntidades(fachadaDonadoresYEntidades);

        DepositoDTO deposito =
                fachadaLogistica.agregarDeposito(
                        new DepositoDTO(null, null, "Deposito", "Direccion", 100, null)
                );

        when(fachadaDonadoresYEntidades.obtenerNecesidadesInsatisfechasDe("leche"))
                .thenReturn(
                        List.of(
                                new NecesidadMaterialDTO(
                                        "n1",
                                        "e1",
                                        5,
                                        "descripcion",
                                        10,
                                        "leche",
                                        TipoNecesidadMaterialEnum.RECURRENTE
                                )
                        )
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> fachadaLogistica.gestionarDonacion(deposito.id(), "don1", "leche", 5)
        );
    }


}

