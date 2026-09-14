package com.vitzemtsov.fileconverter.exception.no_retray;

import com.vitzemtsov.fileconverter.exception.basic.FileConverterException;

public class UnsupportedFormatException extends FileConverterException {

    public UnsupportedFormatException(String message) {
        super(message);
    }
}
