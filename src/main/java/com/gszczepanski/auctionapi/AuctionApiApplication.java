package com.gszczepanski.auctionapi;

import java.time.Clock;
import java.util.TimeZone;
import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = "com.gszczepanski.auctionapi")
@EntityScan(basePackages = "com.gszczepanski.auctionapi")
@EnableJpaRepositories
@EnableTransactionManagement
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
