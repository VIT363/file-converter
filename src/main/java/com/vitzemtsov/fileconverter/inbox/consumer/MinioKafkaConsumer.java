package com.vitzemtsov.fileconverter.inbox.consumer;

import com.vitzemtsov.fileconverter.exception.nonretryable.ConfigurationException;
import com.vitzemtsov.fileconverter.inbox.dto.MinioNotification;
import com.vitzemtsov.fileconverter.inbox.enums.FailureType;
import com.vitzemtsov.fileconverter.exception.basic.FileConverterException;
import com.vitzemtsov.fileconverter.exception.retrayable.TechnicalException;
import com.vitzemtsov.fileconverter.exception.nonretryable.UnsupportedFormatException;
import com.vitzemtsov.fileconverter.inbox.enums.InboxStatus;
import com.vitzemtsov.fileconverter.inbox.entity.InboxMessage;
import com.vitzemtsov.fileconverter.inbox.repository.InboxMessageRepository;
import com.vitzemtsov.fileconverter.inbox.service.InboxCompletionService;
import com.vitzemtsov.fileconverter.outbox.dto.PdfConvertedEvent;
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

    @KafkaListener(topics = "input-events")
    public void consume(MinioNotification notification) {

        if (notification == null || notification.getRecords() == null || notification.getRecords().isEmpty()) {

            log.warn("Получено пустое уведомление, игнорируем");
            return;
        }

        notification.getRecords().forEach(record -> {

            String bucketName = record.getS3().getBucket().getName();

            String objectName = record.getS3().getObject().getKey();

            String eventId = record.getS3().getObject().getSequencer();

            processEvent(record, bucketName, objectName, eventId);
        });
    }

    private void processEvent(MinioNotification.Record record, String bucketName, String objectName, String eventId) {

        Optional<InboxMessage> existing = inboxRepository.findByEventId(eventId);

        if (existing.isPresent() && existing.get().getStatus() == InboxStatus.PROCESSED) {

            log.info("Событие уже обработано: eventId={}", eventId);

            return;
        }

        InboxMessage inbox = existing.orElseGet(() -> createInboxMessage(record, eventId));

        try {
            PdfConvertedEvent result = fileProcessingService.processAndConvert(bucketName, objectName, eventId);

            inboxCompletionService.complete(inbox, result.bucketName(), result.objectName(), result.eventId());

            log.info("Файл успешно обработан: bucket={}, object={}, eventId={}", bucketName, objectName, eventId);

        } catch (UnsupportedFormatException e) {

            log.warn("Неподдерживаемый формат файла: {}", objectName);

            markFailed(inbox, e);

        } catch (TechnicalException e) {

            log.error("Техническая ошибка обработки файла: {}/{}", bucketName, objectName, e);

            markFailed(inbox, e);
            throw e;
        } catch (
                ConfigurationException e) {
            log.error("Ошибка конфигурации: {}", e.getMessage());

            markFailed(inbox, e);
        }
    }

    private InboxMessage createInboxMessage(MinioNotification.Record record, String eventId) {

        InboxMessage message = new InboxMessage();

        message.setEventId(eventId);
        message.setEventType(record.getEventName());
        message.setStatus(InboxStatus.PROCESSING);
        message.setProcessingStartedAt(LocalDateTime.now());

        return inboxRepository.save(message);
    }

    private void markFailed(InboxMessage inbox, FileConverterException exception) {

        inbox.setStatus(InboxStatus.FAILED);
        inbox.setLastError(exception.getMessage());

        FailureType type;
        if (exception instanceof UnsupportedFormatException || exception instanceof ConfigurationException) {
            type = FailureType.BUSINESS;
        } else {
            type = FailureType.TECHNICAL;
        }

        inbox.setFailureType(type);
        inboxRepository.save(inbox);
    }
}

