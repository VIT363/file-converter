package com.vitzemtsov.fileconverter.kafka;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.kafka.topics")
@Data
public class KafkaTopicsProperties {
    private String inputEvents;
    private String outputEvents;
}
