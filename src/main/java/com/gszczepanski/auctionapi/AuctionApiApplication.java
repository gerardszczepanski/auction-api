package com.gszczepanski.auctionapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.gszczepanski.auctionapi")
public class AuctionApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuctionApiApplication.class, args);
    }

}
