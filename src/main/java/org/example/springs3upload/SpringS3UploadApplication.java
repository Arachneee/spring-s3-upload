package org.example.springs3upload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@EnableAsync
public class SpringS3UploadApplication {

    public static void main(String[] args) {
        int maxFD = (int) Runtime.getRuntime().maxMemory();
        System.out.println("########## Current file descriptor limit: " + maxFD);
        SpringApplication.run(SpringS3UploadApplication.class, args);
    }

}
