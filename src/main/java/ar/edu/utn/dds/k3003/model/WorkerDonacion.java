package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.Fachada;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WorkerDonacion {

    @Autowired
    private Fachada fachada;

    @RabbitListener(queues = "donaciones.queue")
    public void procesar(MensajeDonacion mensaje){

        fachada.procesarDonacionWorker(mensaje.getDepositoID(), mensaje.getDonacionID(), mensaje.getProductoID(), mensaje.getCantidad());

    }

}
