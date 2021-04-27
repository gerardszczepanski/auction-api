package com.gszczepanski.auctionapi.domain.auction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.gszczepanski.auctionapi.domain.auction.Auction.AuctionStatus;
import lombok.Builder;
import lombok.Value;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

public interface AuctionRepository {

    Auction save(Auction auction);

    Optional<Auction> findOne(AuctionQuery query);

    List<Auction> findAll(AuctionQuery query);

    @Value
    @Builder
    class AuctionQuery {

        private final String code;

        @Builder.Default
        private final List<AuctionStatus> statuses = new ArrayList<>();

        public static AuctionQuery queryForCode(String code) {
            checkArgument(!isNullOrEmpty(code), "Code is empty");
            return AuctionQuery.builder().code(code).build();
        }

    }
}
