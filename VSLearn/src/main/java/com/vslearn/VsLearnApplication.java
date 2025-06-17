package com.vslearn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.vslearn")
public class VsLearnApplication {

    public static void main(String[] args) {
        SpringApplication.run(VsLearnApplication.class, args);
    }

}
