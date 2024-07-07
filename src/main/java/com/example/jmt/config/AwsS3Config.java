package com.example.jmt.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsS3Config {

    @Bean
    public AmazonS3 s3client() {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials("yourAccessKey", "yourSecretKey");
        return AmazonS3ClientBuilder.standard()
                .withRegion("us-west-2")  // 버킷이 위치한 리전을 여기에 명시
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
    }
}