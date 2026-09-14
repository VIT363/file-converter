package com.vitzemtsov.fileconverter.exception.retray.special;

import com.vitzemtsov.fileconverter.exception.retray.TechnicalException;

public class ConversionException extends TechnicalException {

    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}