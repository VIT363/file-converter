package com.vitzemtsov.fileconverter.converter.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PdfName {

    public String buildPdfObjectName(String objectName) {

        if (objectName.contains(".")) {
            return objectName.substring(0, objectName.lastIndexOf('.')) + ".pdf";
        }
        return objectName + ".pdf";
    }
}
