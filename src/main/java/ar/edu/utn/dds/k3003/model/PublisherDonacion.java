package ar.edu.utn.dds.k3003.model;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PublisherDonacion {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publicar(MensajeDonacion mensaje){

        rabbitTemplate.convertAndSend("donaciones.queue", mensaje);

    }
}
