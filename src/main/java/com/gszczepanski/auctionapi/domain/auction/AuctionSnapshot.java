package com.gszczepanski.auctionapi.domain.auction;


import com.gszczepanski.auctionapi.domain.Id;
import com.gszczepanski.auctionapi.domain.Money;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.List;

@Value
@Builder
public class AuctionSnapshot {

    private final Id id;

    private final String code;

    private final Money minimalPrice;

    private final Money currentAuctionedPrice;

    private final OffsetDateTime startDate;

    private final OffsetDateTime endDate;

    private final List<BetSnapshot> bets;

    private final Auction.AuctionStatus status;

    private final int version;

    private final OffsetDateTime creationTime;

    @Value
    @Builder
    public static class BetSnapshot {

        private final Id id;

        private final Id userId;

        private final OffsetDateTime creationTime;

        private final Money price;

    }

}


