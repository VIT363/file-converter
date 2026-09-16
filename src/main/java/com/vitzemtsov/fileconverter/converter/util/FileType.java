package com.vitzemtsov.fileconverter.converter.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum FileType {

    TXT("txt"),
    PDF("pdf"),
    ZIP("zip"),
    PNG("png"),
    JPG("jpg"),
    JPEG("jpeg");

    private final String extension;

    public static Optional<FileType> fromFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return Optional.empty();
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(type -> lower.endsWith("." + type.extension))
                .findFirst();
    }
}