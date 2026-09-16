package com.vitzemtsov.fileconverter.exception.retrayable;

import com.vitzemtsov.fileconverter.exception.basic.FileConverterException;

public class TechnicalException extends FileConverterException {

    public TechnicalException(String message, Throwable cause) {
        super(message, cause);
    }
}
