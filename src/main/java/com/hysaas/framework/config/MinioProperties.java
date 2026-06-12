package com.hysaas.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "hysaas.minio")
public class MinioProperties {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
}
