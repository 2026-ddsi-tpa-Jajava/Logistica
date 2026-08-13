package ar.edu.utn.dds.k3003.model;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue donacionesQueue() {

        return new Queue("donaciones.queue", true);

    }

}
