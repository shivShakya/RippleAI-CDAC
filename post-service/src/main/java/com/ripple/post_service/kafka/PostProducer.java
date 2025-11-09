package com.ripple.post_service.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PostProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "post-topic";

    public PostProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPost(String message) {
        kafkaTemplate.send(TOPIC, message);
        System.out.println("✅ Sent to Kafka: " + message);
    }
}
