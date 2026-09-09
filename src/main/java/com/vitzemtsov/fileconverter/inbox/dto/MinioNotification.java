package com.vitzemtsov.fileconverter.inbox.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class MinioNotification {
    private List<Record> records;

    @Data
    public static class Record {
        private S3 s3;
        private String eventName;
    }

    @Data
    public static class S3 {
        private Bucket bucket;
        private ObjectData object;
    }

    @Data
    public static class Bucket {
        private String name;
    }

    @Data
    public static class ObjectData {
        private String key;
        private long size;
        private String sequencer;
    }
}
