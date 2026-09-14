package com.vitzemtsov.fileconverter.converter.zip;

import com.vitzemtsov.fileconverter.converter.interfaces.FileToPdfStrategy;
import com.vitzemtsov.fileconverter.converter.util.FileType;
import com.vitzemtsov.fileconverter.exception.retray.special.ConversionException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
@RequiredArgsConstructor
public class ZipToPdfConverter implements FileToPdfStrategy {

    private final List<FileToPdfStrategy> strategies;

    @Override
    public boolean supports(String fileName) {
        return supportsAny(fileName, FileType.ZIP);
    }

    @Override
    public byte[] convert(InputStream inputStream, String fileName) {
        try (ZipInputStream zip = new ZipInputStream(inputStream)) {
            List<byte[]> pdfs = new ArrayList<>();
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    byte[] content = zip.readAllBytes();
                    String entryName = entry.getName();
                    FileToPdfStrategy strategy = strategies.stream()
                            .filter(s -> s.supports(entryName))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Unsupported file in ZIP: " + entryName
                            ));
                    pdfs.add(strategy.convert(new ByteArrayInputStream(content), entryName));
                }
                zip.closeEntry();
            }
            return mergePdfs(pdfs);
        } catch (IOException e) {
            throw new ConversionException("ошибка конвертации ZIP в PDF: " + fileName, e);
        }
    }

    private byte[] mergePdfs(List<byte[]> pdfs) {
        PDFMergerUtility merger = new PDFMergerUtility();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            for (byte[] pdf : pdfs) {
                merger.addSource(new RandomAccessReadBuffer(pdf));
            }
            merger.setDestinationStream(output);
            merger.mergeDocuments(null);
            return output.toByteArray();

        } catch (IOException e) {
            throw new ConversionException("Ошибка склейки PDF файлов", e);
        }
    }
}