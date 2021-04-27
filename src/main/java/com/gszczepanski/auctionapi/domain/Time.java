package com.gszczepanski.auctionapi.domain;

import java.time.Clock;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class Time {

    private final Clock clock;

    public OffsetDateTime now() {
        return OffsetDateTime.ofInstant(clock.instant(), clock.getZone());
    }

}
