package nbc_final.gathering.common.config.s3;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {

    @org.springframework.beans.factory.annotation.Value("${CLOUD_AWS_S3_ACCESS_KEY}")
    private String accessKey;

    @org.springframework.beans.factory.annotation.Value("${CLOUD_AWS_S3_SECRET_KEY}")
    private String secretKey;

    @Value("${CLOUD_AWS_S3_REGION_STATIC}")
    private String region;


    @Bean
    public AmazonS3 amazonS3() {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
    }
}