package com.gszczepanski.auctionapi;

import java.time.Clock;
import java.util.TimeZone;
import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "com.gszczepanski.auctionapi")
public class AuctionApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuctionApiApplication.class, args);
    }

    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Bean
    public Clock clock() {
        return Clock.system(TimeZone.getTimeZone("UTC").toZoneId());
    }

}
