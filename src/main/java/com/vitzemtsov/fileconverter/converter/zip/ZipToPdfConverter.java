package com.vitzemtsov.fileconverter.converter.zip;

import com.vitzemtsov.fileconverter.converter.interfaces.FileToPdfStrategy;
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
        return fileName.toLowerCase().endsWith(".zip");
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
            throw new IllegalStateException("ошибка конвертации ZIP в PDF ", e);
        }
    }

    private byte[] mergePdfs(List<byte[]> pdfs) throws IOException {
        PDFMergerUtility merger = new PDFMergerUtility();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        for (byte[] pdf : pdfs) {
            merger.addSource(new RandomAccessReadBuffer(pdf));
        }

        merger.setDestinationStream(output);
        merger.mergeDocuments(null);

        return output.toByteArray();
    }
}