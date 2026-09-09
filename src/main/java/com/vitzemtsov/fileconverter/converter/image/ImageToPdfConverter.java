package com.vitzemtsov.fileconverter.converter.image;

import com.vitzemtsov.fileconverter.converter.interfaces.FileToPdfStrategy;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

@Component
public class ImageToPdfConverter implements FileToPdfStrategy {

    @Override
    public boolean supports(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg");
    }

    @Override
    public byte[] convert(InputStream inputStream, String fileName) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            PDImageXObject image = PDImageXObject.createFromByteArray(document, inputStream.readAllBytes(), "image");

            PDPage page = new PDPage();
            document.addPage(page);

            PDRectangle mediaBox = page.getMediaBox();
            float pageWidth = mediaBox.getWidth();
            float pageHeight = mediaBox.getHeight();

            float imgWidth = image.getWidth();
            float imgHeight = image.getHeight();

            float scaleX = pageWidth / imgWidth;
            float scaleY = pageHeight / imgHeight;

            float scale = Math.min(scaleX, scaleY);

            float scaledWidth = imgWidth * scale;
            float scaledHeight = imgHeight * scale;

            float x = (pageWidth - scaledWidth) / 2;
            float y = (pageHeight - scaledHeight) / 2;

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.drawImage(image, x, y, scaledWidth, scaledHeight);
            }

            document.save(output);
            return output.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Ошибка конвертации IMAGE в PDF", e);
        }
    }
}