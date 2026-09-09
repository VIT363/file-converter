package com.vitzemtsov.fileconverter.converter;

import com.vitzemtsov.fileconverter.converter.interfaces.FileToPdfStrategy;
import com.vitzemtsov.fileconverter.exception.UnsupportedFormatException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConverterService {

    private final List<FileToPdfStrategy> strategies;

    public byte[] convert(InputStream inputStream, String fileName) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(fileName))
                .findFirst()
                .orElseThrow(() -> new UnsupportedFormatException("Неподдерживаемый формат файла: " + fileName))
                .convert(inputStream, fileName);
    }
}