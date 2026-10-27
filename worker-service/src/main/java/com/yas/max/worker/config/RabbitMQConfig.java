package com.yas.max.worker.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RabbitMQConfig {

    public static final String TASK_QUEUE = "task.queue";

    @Bean
    public Queue taskQueue() {
        return new Queue(TASK_QUEUE, true);
    }


    @Bean
    public MessageConverter jsonMessageConverter() {
        JsonMapper jsonMapper = JsonMapper.builder()
                .build();
        return new JacksonJsonMessageConverter(jsonMapper);
    }

}
