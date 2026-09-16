package com.vitzemtsov.fileconverter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {

    private Outbox outbox = new Outbox();
    private Pdf pdf = new Pdf();

    @Data
    public static class Outbox {
        private long fixedDelay;
    }

    @Data
    public static class Pdf {
        private String fontPath;
    }
}