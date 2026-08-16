package com.medcore.hms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MedCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedCoreApplication.class, args);
    }
}