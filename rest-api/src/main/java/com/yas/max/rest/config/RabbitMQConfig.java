package com.yas.max.rest.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RabbitMQConfig {

    public static final String TASK_QUEUE = "task.queue";
    public static final String TASK_EXCHANGE = "task.exchange";
    public static final String TASK_ROUTING_KEY = "task.routing.key";

    @Bean
    public Queue taskQueue() {
        return new Queue(TASK_QUEUE, true);
    }

    @Bean
    public DirectExchange taskExchange() {
        return new DirectExchange(TASK_EXCHANGE);
    }

    @Bean
    public Binding binding(Queue taskQueue, DirectExchange taskExchange) {
        return BindingBuilder.bind(taskQueue).to(taskExchange).with(TASK_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        JsonMapper jsonMapper = JsonMapper.builder()
                .build();
        return new JacksonJsonMessageConverter(jsonMapper);
    }

}
