package com.vitzemtsov.fileconverter.service;

public record ConversionResult(
        String bucketName,
        String objectName
) {
}