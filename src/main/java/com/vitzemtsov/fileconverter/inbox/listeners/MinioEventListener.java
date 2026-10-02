package com.vitzemtsov.fileconverter.inbox.listeners;

import com.vitzemtsov.common.events.FileConversionRequest;
import com.vitzemtsov.fileconverter.service.FileConversionService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MinioEventListener {

    private final FileConversionService fileConversionService;

    @KafkaListener(topics = "#{@kafkaTopicsProperties.getToConvert()}")
    public void consume(FileConversionRequest request) {
        fileConversionService.handle(request);
    }
}