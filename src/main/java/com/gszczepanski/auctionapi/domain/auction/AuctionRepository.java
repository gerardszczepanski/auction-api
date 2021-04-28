package com.gszczepanski.auctionapi.domain.auction;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.gszczepanski.auctionapi.domain.auction.Auction.AuctionStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static lombok.AccessLevel.NONE;

public interface AuctionRepository {

    AuctionSnapshot save(AuctionSnapshot auction);

    Optional<AuctionSnapshot> findOne(AuctionQuery query);

    List<AuctionSnapshot> findAll(AuctionQuery query);

    @Value
    @Builder
    class AuctionQuery {

        @Getter(NONE)
        private final String code;

        @Builder.Default
        private final List<AuctionStatus> statuses = Arrays.asList(AuctionStatus.values());

        public static AuctionQuery queryForCode(String code) {
            checkArgument(!isNullOrEmpty(code), "Code is empty");
            return AuctionQuery.builder().code(code).build();
        }

        public Optional<String> getCode() {
            return Optional.ofNullable(code);
        }

    }
}
