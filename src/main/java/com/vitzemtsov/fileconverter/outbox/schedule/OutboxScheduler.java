package com.vitzemtsov.fileconverter.outbox.schedule;

import com.vitzemtsov.fileconverter.outbox.entity.OutboxMessage;
import com.vitzemtsov.fileconverter.outbox.enums.OutboxStatus;
import com.vitzemtsov.fileconverter.outbox.repository.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxScheduler {

    private final OutboxMessageRepository outboxRepository;
    private final KafkaTemplate<@NonNull String,@NonNull String> kafkaTemplate;

    @Scheduled(fixedDelayString = "${app.outbox.fixed-delay}")
    public void publish() {

        List<OutboxMessage> messages = outboxRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.NEW);

        for (OutboxMessage message : messages) {
            publishMessage(message);
        }
    }

    private void publishMessage(OutboxMessage message) {

        try {
            kafkaTemplate.send(message.getTopic(), message.getEventId(), message.getPayload()).get();

            message.setStatus(OutboxStatus.SENT);
            message.setSentAt(LocalDateTime.now());

            outboxRepository.save(message);

        } catch (Exception e) {

            message.setAttempts(message.getAttempts() + 1);

            message.setLastError(e.getMessage());

            outboxRepository.save(message);

            log.error("Ошибка отправки outbox event: {}", message.getEventId(), e);
        }
    }
}