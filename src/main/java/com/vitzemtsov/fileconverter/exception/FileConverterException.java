package com.vitzemtsov.fileconverter.exception;

public abstract class FileConverterException extends RuntimeException {

    protected FileConverterException(String message) {
        super(message);
    }

    protected FileConverterException(String message, Throwable cause) {
        super(message, cause);
    }
}