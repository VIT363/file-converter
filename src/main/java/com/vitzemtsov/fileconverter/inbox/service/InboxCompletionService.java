package com.vitzemtsov.fileconverter.inbox.service;

import com.vitzemtsov.fileconverter.inbox.entity.InboxMessage;
import com.vitzemtsov.fileconverter.inbox.enums.InboxStatus;
import com.vitzemtsov.fileconverter.inbox.repository.InboxMessageRepository;
import com.vitzemtsov.fileconverter.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InboxCompletionService {

    private final InboxMessageRepository inboxRepository;
    private final OutboxService outboxService;

    @Transactional
    public void complete(InboxMessage inbox, String bucketName, String objectName, String eventId) {

        inbox.setStatus(InboxStatus.PROCESSED);
        inbox.setProcessedAt(LocalDateTime.now());
        inbox.setFailureType(null);
        inbox.setLastError(null);

        inboxRepository.save(inbox);

        outboxService.createSuccessEvent(eventId, bucketName, objectName);
    }
}