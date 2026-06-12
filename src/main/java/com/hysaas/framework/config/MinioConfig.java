package com.hysaas.framework.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

    @Bean
    public MinioClient minioClient(MinioProperties props) {
        return MinioClient.builder()
                .endpoint(props.getEndpoint())
                .credentials(props.getAccessKey(), props.getSecretKey())
                .build();
    }

    @Bean
    public ApplicationRunner initMinioBucket(MinioClient client, MinioProperties props) {
        return args -> {
            try {
                String bucket = props.getBucket();
                boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
                if (!exists) {
                    client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                }
            } catch (Exception e) {
                log.warn("MinIO bucket init skipped: {}", e.getMessage());
            }
        };
    }
}
