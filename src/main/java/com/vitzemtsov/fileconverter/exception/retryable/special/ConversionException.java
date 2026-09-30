package com.vitzemtsov.fileconverter.exception.retryable.special;

import com.vitzemtsov.fileconverter.exception.retryable.TechnicalException;

public class ConversionException extends TechnicalException {

    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}