package com.vitzemtsov.fileconverter.inbox.consumer;

import com.vitzemtsov.common.events.FileConversionRequest;
import com.vitzemtsov.fileconverter.exception.basic.FileConverterException;
import com.vitzemtsov.fileconverter.exception.nonretryable.ConfigurationException;
import com.vitzemtsov.fileconverter.exception.nonretryable.UnsupportedFormatException;
import com.vitzemtsov.fileconverter.exception.retryable.TechnicalException;
import com.vitzemtsov.fileconverter.inbox.entity.InboxMessage;
import com.vitzemtsov.fileconverter.inbox.enums.FailureType;
import com.vitzemtsov.fileconverter.inbox.enums.InboxStatus;
import com.vitzemtsov.fileconverter.inbox.repository.InboxMessageRepository;
import com.vitzemtsov.fileconverter.inbox.service.InboxCompletionService;
import com.vitzemtsov.fileconverter.outbox.service.OutboxService;
import com.vitzemtsov.fileconverter.service.ConversionResult;
import com.vitzemtsov.fileconverter.service.FileProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioKafkaConsumer {

    private final FileProcessingService fileProcessingService;
    private final InboxMessageRepository inboxRepository;
    private final InboxCompletionService inboxCompletionService;
    private final OutboxService outboxService;

    @KafkaListener(topics = "#{@kafkaTopicsProperties.getToConvert()}")
    public void consume(FileConversionRequest request) {

        if (request == null || request.fileId() == null) {
            log.warn("Пустое Kafka-сообщение, игнорируем");
            return;
        }

        String eventId = request.fileId().toString();

        Optional<InboxMessage> existing = inboxRepository.findByEventId(eventId);
        if (existing.isPresent() && existing.get().getStatus() == InboxStatus.PROCESSED) {
            log.info("Событие уже обработано: fileId={}", request.fileId());
            return;
        }

        InboxMessage inbox = existing.orElseGet(() -> createInboxMessage(eventId));

        try {
            ConversionResult result = fileProcessingService.processAndConvert(
                    request.fileId(), request.bucketName(), request.objectName());

            inboxCompletionService.complete(inbox, result, request.fileId());

            log.info("Файл успешно обработан: fileId={}, bucket={}, object={}",
                    request.fileId(), request.bucketName(), request.objectName());

        } catch (UnsupportedFormatException | ConfigurationException e) {
            log.warn("Non-retryable ошибка: fileId={}, msg={}", request.fileId(), e.getMessage());
            markFailed(inbox, e);
            outboxService.createErrorEvent(request.fileId(), e.getMessage());

        } catch (TechnicalException e) {
            log.error("Техническая ошибка, будет ретрай: fileId={}", request.fileId(), e);
            markFailed(inbox, e);
            throw e; // Kafka повторит, outbox НЕ пишем
        }
    }

    private InboxMessage createInboxMessage(String eventId) {
        InboxMessage message = new InboxMessage();
        message.setEventId(eventId);
        message.setEventType("FILE_CONVERSION_REQUESTED");
        message.setStatus(InboxStatus.PROCESSING);
        message.setProcessingStartedAt(LocalDateTime.now());
        return inboxRepository.save(message);
    }

    private void markFailed(InboxMessage inbox, FileConverterException exception) {
        inbox.setStatus(InboxStatus.FAILED);
        inbox.setLastError(exception.getMessage());

        FailureType type = (exception instanceof UnsupportedFormatException
                || exception instanceof ConfigurationException)
                ? FailureType.BUSINESS
                : FailureType.TECHNICAL;

        inbox.setFailureType(type);
        inboxRepository.save(inbox);
    }
}