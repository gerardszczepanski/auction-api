package com.gszczepanski.auctionapi.domain.auction

import com.gszczepanski.auctionapi.domain.Id
import com.gszczepanski.auctionapi.domain.Money

import java.time.OffsetDateTime
import java.time.ZoneOffset

import static com.gszczepanski.auctionapi.domain.Money.Currency.*
import static com.gszczepanski.auctionapi.domain.auction.AuctionSnapshot.*

trait WithAuctionFixtures {

    CreateAuctionSpecification givenCreateAuctionSpecification(String code) {
        return CreateAuctionSpecification.Builder.aCreateAuctionSpecification()
                .code('ASD-FGH')
                .minimalPrice(Money.from("10.00", PLN))
                .startDate(givenDateTime(01, 18))
                .endDate(givenDateTime(02, 18))
                .build()
    }

    Auction givenNotStartedAuction() {
        return Auction.restoreFrom(
                builder()
                        .id(Id.from('f42f975d-f3f3-4a02-ab60-f5116da097d2'))
                        .code('ASD-FGH')
                        .minimalPrice(Money.from("10.00", PLN))
                        .startDate(givenDateTime(01, 18))
                        .endDate(givenDateTime(02, 18))
                        .bets([])
                        .version(1)
                        .status(Auction.AuctionStatus.NOT_STARTED)
                        .creationTime(givenDateTime(01, 17))
                        .build()
        )
    }

    Auction givenStartedAuctionWithNoBets() {
        return Auction.restoreFrom(
                builder()
                        .id(Id.from('f42f975d-f3f3-4a02-ab60-f5116da097d2'))
                        .code('DDD-HXG')
                        .minimalPrice(Money.from("10.00", PLN))
                        .startDate(givenDateTime(01, 18))
                        .endDate(givenDateTime(02, 18))
                        .bets([])
                        .version(1)
                        .status(Auction.AuctionStatus.STARTED)
                        .creationTime(givenDateTime(01, 17))
                        .build()
        )
    }

    Auction givenStartedAuctionWithBets() {
        return Auction.restoreFrom(
                builder()
                        .id(Id.from('f55e954b-ba18-4749-9d20-be7ec7f716c9'))
                        .code('XDD-HXG')
                        .minimalPrice(Money.from("10.00", PLN))
                        .startDate(givenDateTime(01, 18))
                        .endDate(givenDateTime(02, 18))
                        .bets([
                                givenBetSnapshot(Id.generate(), '10', givenDateTime(01, 19, 10)),
                                givenBetSnapshot(Id.generate(), '11', givenDateTime(01, 19, 50)),
                                givenBetSnapshot(Id.generate(), '15', givenDateTime(01, 22, 30))
                        ])
                        .version(1)
                        .status(Auction.AuctionStatus.STARTED)
                        .creationTime(givenDateTime(01, 17))
                        .build()
        )
    }

    BetSnapshot givenBetSnapshot(Id userId, String price, OffsetDateTime creationTime) {
        return BetSnapshot.builder()
                .price(Money.from(price, PLN))
                .id(Id.generate())
                .creationTime(creationTime)
                .userId(userId)
                .build()
    }

    Auction givenFinishedAuctionWithNoBets() {
        return Auction.restoreFrom(
                builder()
                        .id(Id.from('45e13ada-c50d-40da-9d8a-9ffa999e221a'))
                        .code('ASG-HXG')
                        .minimalPrice(Money.from("10.00", PLN))
                        .startDate(givenDateTime(01, 18))
                        .endDate(givenDateTime(02, 18))
                        .bets([])
                        .version(2)
                        .status(Auction.AuctionStatus.FINISHED_NOT_SOLD)
                        .creationTime(givenDateTime(01, 17))
                        .build()
        )
    }

    OffsetDateTime givenDateTime(int day, int hour) {
        return OffsetDateTime.of(2021, 06, day, hour, 0, 0, 0, ZoneOffset.UTC)
    }

    OffsetDateTime givenDateTime(int day, int hour, int minutes) {
        return OffsetDateTime.of(2021, 06, day, hour, minutes, 0, 0, ZoneOffset.UTC)
    }

}
