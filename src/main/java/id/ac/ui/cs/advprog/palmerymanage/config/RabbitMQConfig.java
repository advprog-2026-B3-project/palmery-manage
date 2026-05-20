package id.ac.ui.cs.advprog.palmerymanage.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.harvest:harvest_exchange}")
    private String exchange;

    @Value("${rabbitmq.routingkey.harvest.approved:harvest_approved_routing_key}")
    private String routingKeyApproved;

    @Value("${rabbitmq.routingkey.harvest.submitted:harvest_submitted_routing_key}")
    private String routingKeySubmitted;

    @Value("${rabbitmq.queue.harvest.approved:harvest_approved_queue}")
    private String queueApproved;

    @Value("${rabbitmq.queue.harvest.submitted:harvest_submitted_queue}")
    private String queueSubmitted;

    @Bean
    public Queue harvestApprovedQueue() {
        return new Queue(queueApproved, true);
    }

    @Bean
    public Queue harvestSubmittedQueue() {
        return new Queue(queueSubmitted, true);
    }

    @Bean
    public DirectExchange harvestExchange() {
        return new DirectExchange(exchange);
    }

    @Bean
    public Binding bindingApproved(Queue harvestApprovedQueue, DirectExchange harvestExchange) {
        return BindingBuilder.bind(harvestApprovedQueue).to(harvestExchange).with(routingKeyApproved);
    }

    @Bean
    public Binding bindingSubmitted(Queue harvestSubmittedQueue, DirectExchange harvestExchange) {
        return BindingBuilder.bind(harvestSubmittedQueue).to(harvestExchange).with(routingKeySubmitted);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
