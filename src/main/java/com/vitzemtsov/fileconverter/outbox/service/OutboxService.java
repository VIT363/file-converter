package com.vitzemtsov.fileconverter.outbox.service;

import com.vitzemtsov.common.events.ConversionStatus;
import com.vitzemtsov.fileconverter.kafka.KafkaTopicsProperties;
import com.vitzemtsov.common.events.FileConvertedEvent;
import com.vitzemtsov.fileconverter.outbox.entity.OutboxMessage;
import com.vitzemtsov.fileconverter.outbox.enums.OutboxStatus;
import com.vitzemtsov.fileconverter.outbox.repository.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxMessageRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTopicsProperties topicsProperties;

    public void createSuccessEvent(UUID fileId, String bucketName, String objectName) {
        FileConvertedEvent event = new FileConvertedEvent(
                fileId,
                ConversionStatus.SUCCESS,
                bucketName,
                objectName,
                null
        );
        saveEvent(event);
    }

    public void createErrorEvent(UUID fileId, String errorMessage) {
        FileConvertedEvent event = new FileConvertedEvent(
                fileId,
                ConversionStatus.ERROR,
                null,
                null,
                errorMessage
        );
        saveEvent(event);
    }

    private void saveEvent(FileConvertedEvent event) {
        OutboxMessage message = new OutboxMessage();
        message.setEventId(UUID.randomUUID().toString());
        message.setEventType(event.status() == ConversionStatus.SUCCESS
                ? "PDF_CONVERTED" : "PDF_CONVERSION_FAILED");
        message.setTopic(topicsProperties.getConverted());
        message.setPayload(objectMapper.writeValueAsString(event));
        message.setStatus(OutboxStatus.NEW);
        message.setCreatedAt(LocalDateTime.now());
        outboxRepository.save(message);
    }
}