package org.example.springs3upload.s3.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TestRest {

    @PostMapping("/test1")
    public void test() {
        log.info("Rest-Controller $####################");
    }
}
