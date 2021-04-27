package com.gszczepanski.auctionapi.domain.auction

import com.gszczepanski.auctionapi.domain.Money

import java.time.OffsetDateTime
import java.time.ZoneOffset

import static com.gszczepanski.auctionapi.domain.Money.Currency.*

trait WithAuctionFixtures {

    CreateAuctionSpecification givenCreateAuctionSpecification(String code) {
        return CreateAuctionSpecification.Builder.aCreateAuctionSpecification()
                .code(code)
                .startPrice(Money.from("0.00", PLN))
                .minimalPrice(Money.from("10.00", PLN))
                .startDate(givenDateTime(01, 18))
                .endDate(givenDateTime(02, 18))
                .build()
    }

    Auction givenNotStartedAuction() {
        return Auction.createFrom(
                givenCreateAuctionSpecification("ASD-FGH")
        )
    }

    OffsetDateTime givenDateTime(int day, int hour) {
        return OffsetDateTime.of(2021, 06, day, hour, 0, 0, 0, ZoneOffset.UTC)
    }

}
