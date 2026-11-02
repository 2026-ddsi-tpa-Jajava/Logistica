package ar.edu.utn.dds.k3003.model;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PublisherConsulta {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publicar(MensajeConsulta mensaje) {

        rabbitTemplate.convertAndSend("consultas.queue", mensaje);

    }
}