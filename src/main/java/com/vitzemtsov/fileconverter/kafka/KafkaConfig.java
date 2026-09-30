package com.vitzemtsov.fileconverter.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private final KafkaTopicsProperties topicsProperties;

    @Bean
    public NewTopic toConvertTopic() {
        return TopicBuilder.name(topicsProperties.getToConvert())
                .partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic convertedTopic() {
        return TopicBuilder.name(topicsProperties.getConverted())
                .partitions(3).replicas(1).build();
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        ConsumerRecordRecoverer recoverer = (record, exception) ->
                log.error("Сообщение потеряно после всех ретраев: topic={}, partition={}, offset={}, err={}",
                        record.topic(), record.partition(), record.offset(), exception.getMessage());

        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3L));
    }
}