package com.isa.jutjubic.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue jsonQueue() {
        return QueueBuilder.durable("video.json.queue").build();
    }

    @Bean
    public Queue protoQueue() {
        return QueueBuilder.durable("video.proto.queue").build();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        // SimpleMessageConverter prihvata byte[], String, i Serializable
        factory.setMessageConverter(new SimpleMessageConverter());
        return factory;
    }
}
