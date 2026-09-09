package com.vitzemtsov.fileconverter.converter.interfaces;

import java.io.InputStream;

public interface FileToPdfStrategy {

    boolean supports(String fileName);

    byte[] convert(InputStream inputStream,String fileName);
}
