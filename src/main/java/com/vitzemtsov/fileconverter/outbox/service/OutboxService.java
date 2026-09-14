package com.vitzemtsov.fileconverter.outbox.service;

import tools.jackson.databind.ObjectMapper;
import com.vitzemtsov.fileconverter.outbox.dto.PdfConvertedEvent;
import com.vitzemtsov.fileconverter.outbox.enums.OutboxStatus;
import com.vitzemtsov.fileconverter.outbox.entity.OutboxMessage;
import com.vitzemtsov.fileconverter.outbox.repository.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxMessageRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.output-events:output-events}")
    private String outputTopic;

    public void createSuccessEvent(String eventId, String bucketName, String objectName) {

        PdfConvertedEvent event = new PdfConvertedEvent(bucketName, objectName, eventId);

        OutboxMessage message = new OutboxMessage();

        message.setEventId(eventId);
        message.setEventType("PDF_CONVERTED");
        message.setTopic(outputTopic);
        message.setPayload(objectMapper.writeValueAsString(event));
        message.setStatus(OutboxStatus.NEW);
        message.setCreatedAt(LocalDateTime.now());

        outboxRepository.save(message);
    }
}