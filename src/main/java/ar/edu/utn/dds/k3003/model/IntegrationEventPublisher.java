package ar.edu.utn.dds.k3003.model;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class IntegrationEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public IntegrationEventPublisher(RabbitTemplate rabbitTemplate) {

        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarEvento(String tipo, Map<String,Object> payload) {

        Map<String,Object> evento = Map.of("id", UUID.randomUUID().toString(), "tipo", tipo, "version", 1L, "ocurridoEn", Instant.now().toString(), "payload", payload);

        rabbitTemplate.convertAndSend("cqrs.events.queue", evento);

    }
}