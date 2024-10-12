package org.example.springs3upload.s3;

import static software.amazon.awssdk.regions.Region.AP_NORTHEAST_2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AwsS3Config {

    @Bean
    public S3Client s3Client() {
        ApacheHttpClient apacheHttpClient = (ApacheHttpClient) ApacheHttpClient.builder()
                .maxConnections(50)
                .build();

        return S3Client.builder()
                .httpClient(apacheHttpClient)
                .region(AP_NORTHEAST_2)
                .build();
    }

    @Bean
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(10);
    }

    @Bean
    public S3AsyncClient s3AsyncClient() {
        return S3AsyncClient.builder()
                .region(Region.AP_NORTHEAST_2) // 지역 설정
                .httpClientBuilder(NettyNioAsyncHttpClient.builder()
                        .maxConcurrency(50)  // 최대 동시 요청 설정
                )
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .retryPolicy(RetryPolicy.none())  // 재시도 정책 비활성화
                        .build()
                )
                .build();
    }

    @Bean
    public S3Presigner S3Presigner() {
        return S3Presigner.builder()
                .region(AP_NORTHEAST_2)
                .build();
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> {
            factory.setProtocol("org.apache.coyote.http11.Http11NioProtocol");
        };
    }

}
