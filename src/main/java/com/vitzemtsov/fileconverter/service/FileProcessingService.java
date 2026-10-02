package com.vitzemtsov.fileconverter.service;

import com.vitzemtsov.fileconverter.converter.ConverterService;
import com.vitzemtsov.fileconverter.converter.util.PdfName;
import com.vitzemtsov.fileconverter.exception.basic.FileConverterException;
import com.vitzemtsov.fileconverter.exception.retryable.TechnicalException;
import com.vitzemtsov.fileconverter.minio.ObjectNameDecoder;
import com.vitzemtsov.fileconverter.minio.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileProcessingService {

    private final ConverterService converterService;
    private final MinioService minioService;

    @Value("${minio.result-bucket:pdf-files}")
    private String resultBucket;

    public ConversionResult processAndConvert(UUID fileId, String bucketName, String objectName) {

        String decodedName = ObjectNameDecoder.decode(objectName);

        try (InputStream sourceData = minioService.downloadFile(bucketName, decodedName)) {

            byte[] pdf = converterService.convert(sourceData, decodedName);

            String pdfObjectName = PdfName.buildPdfObjectName(decodedName);

            minioService.uploadFile(resultBucket, pdfObjectName, pdf);

            log.info("Файл сконвертирован: fileId={}, pdf={}/{}", fileId, resultBucket, pdfObjectName);

            return new ConversionResult(resultBucket, pdfObjectName);

        } catch (FileConverterException e) {
            throw e;
        } catch (Exception e) {
            throw new TechnicalException("Ошибка обработки файла: " + objectName, e);
        }
    }
}