package ar.edu.utn.dds.k3003.model;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class WorkerDonacion {

    @RabbitListener(queues = "donaciones.queue")
    public void procesar(Object mensaje){

        System.out.println("Clase recibida: " + mensaje.getClass());

        System.out.println(mensaje);

    }



}
