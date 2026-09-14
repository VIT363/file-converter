package com.vitzemtsov.fileconverter.inbox.repository;

import com.vitzemtsov.fileconverter.inbox.entity.InboxMessage;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InboxMessageRepository extends JpaRepository<@NonNull InboxMessage,@NonNull Long> {

    Optional<InboxMessage> findByEventId(String eventId);
}
