package org.example.springs3upload.s3.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller
public class TestCon {

    @PostMapping("/test")
    public void test() {
      log.info("Controller $####################");
    }
}
