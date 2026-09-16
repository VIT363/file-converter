package com.vitzemtsov.fileconverter.exception.retrayable.special;

import com.vitzemtsov.fileconverter.exception.retrayable.TechnicalException;

public class ConversionException extends TechnicalException {

    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}