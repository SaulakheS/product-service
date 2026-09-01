package com.zestindia.productservice.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ProductEventListener {

    private static final Logger log = LoggerFactory.getLogger(ProductEventListener.class);

    @Async("taskExecutor")
    @EventListener
    public void handleProductActivityEvent(ProductActivityEvent event) {
        log.info("[ASYNC-EVENT] Product Event Received on thread [{}]: Type={}, ProductId={}, Name='{}', User='{}'",
                Thread.currentThread().getName(),
                event.getActivityType(),
                event.getProductId(),
                event.getProductName(),
                event.getTriggeredBy());

        // In a microservices architecture, this could push to Kafka/RabbitMQ or update an elastic search index
    }
}
