package com.vitzemtsov.fileconverter.converter.interfaces;

import com.vitzemtsov.fileconverter.converter.util.FileType;

import java.io.InputStream;
import java.util.Arrays;

public interface FileToPdfStrategy {

    boolean supports(String fileName);

    byte[] convert(InputStream inputStream, String fileName);

    default boolean supportsAny(String fileName, FileType... types) {
        return FileType.fromFileName(fileName)
                .map(type -> Arrays.asList(types).contains(type))
                .orElse(false);
    }
}
