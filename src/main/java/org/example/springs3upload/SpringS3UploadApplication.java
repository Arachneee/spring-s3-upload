package org.example.springs3upload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
//@EnableAsync
public class SpringS3UploadApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringS3UploadApplication.class, args);
    }

}
