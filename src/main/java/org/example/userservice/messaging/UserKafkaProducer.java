package org.example.userservice.messaging;

import org.example.userservice.events.UserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserKafkaProducer {
    private final Logger logger = LoggerFactory.getLogger(UserKafkaProducer.class);
    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    public UserKafkaProducer(KafkaTemplate<String, UserEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendUserToKafka(UserEvent userEvent) {
        kafkaTemplate.send("users", userEvent);
        logger.info("User sent to kafka: id={}", userEvent.getEmail());
    }
}
