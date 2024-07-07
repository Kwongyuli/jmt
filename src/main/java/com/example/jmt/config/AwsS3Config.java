package com.example.jmt.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

//@Configuration
//public class AwsS3Config {
//
//    @Bean
//    public AmazonS3 s3client() {
//        return AmazonS3ClientBuilder.standard()
//                .withRegion("ap-northeast-2")  // 버킷이 위치한 리전을 여기에 명시
//                .withCredentials(new DefaultAWSCredentialsProviderChain())
//                .build();
//    }
//}

// package com.example.jmt.config;

// import com.amazonaws.auth.AWSStaticCredentialsProvider;
// import com.amazonaws.auth.BasicAWSCredentials;
// import com.amazonaws.services.s3.AmazonS3;
// import com.amazonaws.services.s3.AmazonS3ClientBuilder;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.beans.factory.annotation.Value;

@Configuration
public class AwsS3Config {

    @Value("${aws.accessKeyId}")
    private String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    @Bean
    public AmazonS3 s3client() {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonS3ClientBuilder.standard()
                .withRegion("ap-northeast-2")
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
    }
}