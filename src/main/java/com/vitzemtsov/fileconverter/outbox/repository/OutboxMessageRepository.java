package com.vitzemtsov.fileconverter.outbox.repository;

import com.vitzemtsov.fileconverter.outbox.enums.OutboxStatus;
import com.vitzemtsov.fileconverter.outbox.entity.OutboxMessage;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxMessageRepository extends JpaRepository<@NonNull OutboxMessage,@NonNull Long> {

    List<OutboxMessage> findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}