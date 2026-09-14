package com.vitzemtsov.fileconverter.inbox.entity;

import com.vitzemtsov.fileconverter.inbox.enums.FailureType;
import com.vitzemtsov.fileconverter.inbox.enums.InboxStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inbox_messages", uniqueConstraints = {
        @UniqueConstraint(name = "uk_inbox_event_id", columnNames = "event_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InboxMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InboxStatus status;

    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Enumerated(EnumType.STRING)
    @Column(name = "failure_type")
    private FailureType failureType;
}