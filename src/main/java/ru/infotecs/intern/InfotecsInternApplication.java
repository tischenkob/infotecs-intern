package ru.infotecs.intern;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class InfotecsInternApplication {

    public static void main(String[] args) {
        SpringApplication.run(InfotecsInternApplication.class, args);
    }

}
