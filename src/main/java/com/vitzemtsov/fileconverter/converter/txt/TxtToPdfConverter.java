package com.vitzemtsov.fileconverter.converter.txt;

import com.vitzemtsov.fileconverter.config.AppProperties;
import com.vitzemtsov.fileconverter.converter.interfaces.FileToPdfStrategy;
import com.vitzemtsov.fileconverter.converter.util.FileType;
import com.vitzemtsov.fileconverter.exception.no_retray.ConfigurationException;
import com.vitzemtsov.fileconverter.exception.retray.special.ConversionException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class TxtToPdfConverter implements FileToPdfStrategy {

    private static final float MARGIN_LEFT = 50;
    private static final float MARGIN_TOP = 700;
    private static final float LINE_HEIGHT = 15;
    private static final int FONT_SIZE = 12;

    private final AppProperties appProperties;
    private final ResourceLoader resourceLoader;

    public TxtToPdfConverter(AppProperties appProperties, ResourceLoader resourceLoader) {
        this.appProperties = appProperties;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public boolean supports(String fileName) {
        return supportsAny(fileName, FileType.TXT);
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
            throw new ConversionException("Ошибка конвертации TXT в PDF: " + fileName, e);
        }
    }

    private PDType0Font loadFont(PDDocument document) {
        String fontPath = appProperties.getPdf().getFontPath();
        Resource fontResource = resourceLoader.getResource(fontPath);

        if (!fontResource.exists()) {
            throw new ConfigurationException(
                    "Шрифт не найден по пути: " + fontPath
                            + ". Проверьте свойство app.pdf.font-path");
        }
        try (InputStream fontStream = fontResource.getInputStream()) {
            return PDType0Font.load(document, fontStream);
        } catch (IOException e) {
            throw new ConversionException("Ошибка загрузки шрифта: " + fontPath, e);
        }
    }
}