package com.tianqizhang.webapp.Config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {

    @Value("${cloud.aws.region}")
    private String region;

    @Bean
    AmazonS3 generateS3Client(){
        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .build();
    }
}
