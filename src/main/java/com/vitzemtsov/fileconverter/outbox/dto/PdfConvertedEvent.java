package com.vitzemtsov.fileconverter.outbox.dto;

public record PdfConvertedEvent(String bucketName, String objectName, String eventId) {
}