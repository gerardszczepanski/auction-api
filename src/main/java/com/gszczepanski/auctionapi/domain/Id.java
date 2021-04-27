package com.gszczepanski.auctionapi.domain;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@RequiredArgsConstructor(access = PRIVATE)
public class Id {

    private final String value;

    public static Id from(String uuid) {
        validateUuid(uuid);
        return new Id(uuid);
    }

    private static void validateUuid(String uuid) {
        try {
            UUID.fromString(uuid);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Not valid UUID provided as Id");
        }
    }

    public static Id generate() {
        return new Id(UUID.randomUUID().toString());
    }

    public String asString() {
        return value;
    }

}
