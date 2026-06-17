package com.supersys.ai.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue requestQueue() {
        return new Queue("schedule-analysis-request-queue", true);
    }

    @Bean
    public Queue responseQueue() {
        return new Queue("schedule-analysis-response-queue", true);
    }
}
