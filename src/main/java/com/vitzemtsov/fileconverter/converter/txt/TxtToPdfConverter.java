package com.vitzemtsov.fileconverter.converter.txt;

import com.vitzemtsov.fileconverter.converter.interfaces.FileToPdfStrategy;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Component
public class TxtToPdfConverter implements FileToPdfStrategy {

    private static final float MARGIN_LEFT = 50;
    private static final float MARGIN_TOP = 700;
    private static final float LINE_HEIGHT = 15;
    private static final int FONT_SIZE = 12;

    @Override
    public boolean supports(String fileName) {
        return fileName.toLowerCase().endsWith(".txt");
    }

    @Override
    public byte[] convert(InputStream inputStream, String fileName) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            PDType0Font font = loadFont(document);

            PDPage currentPage = new PDPage();
            document.addPage(currentPage);
            PDPageContentStream content = new PDPageContentStream(document, currentPage);
            content.beginText();
            content.setFont(font, FONT_SIZE);
            content.newLineAtOffset(MARGIN_LEFT, MARGIN_TOP);

            float y = MARGIN_TOP;
            String line;

            while ((line = reader.readLine()) != null) {
                if (y != MARGIN_TOP) {
                    content.newLineAtOffset(0, -LINE_HEIGHT);
                }
                content.showText(line);
                y -= LINE_HEIGHT;

                if (y < 50) {
                    content.endText();
                    content.close();

                    currentPage = new PDPage();
                    document.addPage(currentPage);
                    content = new PDPageContentStream(document, currentPage);
                    content.beginText();
                    content.setFont(font, FONT_SIZE);
                    content.newLineAtOffset(MARGIN_LEFT, MARGIN_TOP);
                    y = MARGIN_TOP;
                }
            }

            content.endText();
            content.close();

            document.save(output);
            return output.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Ошибка конвертации TXT в PDF", e);
        }
    }

    private PDType0Font loadFont(PDDocument document) throws IOException {
        try (InputStream fontStream = getClass().getResourceAsStream("/fonts/arialmt.ttf")) {
            if (fontStream == null) {
                throw new IOException("Font file /fonts/arialmt.ttf not found in classpath");
            }
            return PDType0Font.load(document, fontStream);
        }
    }
}