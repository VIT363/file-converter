package com.vitzemtsov.fileconverter.exception.nonretryable;

import com.vitzemtsov.fileconverter.exception.basic.FileConverterException;

public class ConfigurationException extends FileConverterException {

    public ConfigurationException(String message) {
        super(message);
    }
}