package com.chromeos.playtool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PlaytoolApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlaytoolApplication.class, args);
    }

}
