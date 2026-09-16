package com.vitzemtsov.fileconverter.minio;

import lombok.experimental.UtilityClass;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@UtilityClass
public class ObjectNameDecoder {

    public String decode(String objectName) {
        return URLDecoder.decode(objectName, StandardCharsets.UTF_8);
    }
}