package com.gszczepanski.auctionapi.domain.auction

import com.gszczepanski.auctionapi.domain.Id
import com.gszczepanski.auctionapi.domain.Money
import com.gszczepanski.auctionapi.domain.Time
import spock.lang.Specification

import static com.gszczepanski.auctionapi.domain.Money.Currency.PLN
import static com.gszczepanski.auctionapi.domain.auction.Auction.AuctionStatus.FINISHED_NOT_SOLD
import static com.gszczepanski.auctionapi.domain.auction.Auction.AuctionStatus.FINISHED_SOLD
import static com.gszczepanski.auctionapi.domain.auction.Auction.AuctionStatus.NOT_STARTED
import static com.gszczepanski.auctionapi.domain.auction.Auction.AuctionStatus.STARTED
import static com.gszczepanski.auctionapi.domain.auction.Auction.PlaceBetResult
import static com.gszczepanski.auctionapi.domain.auction.Auction.PlaceBetResultStatus.*
import static com.gszczepanski.auctionapi.domain.auction.Auction.createFrom

class AuctionSpec extends Specification implements WithAuctionFixtures {

    def "should create Auction from CreateAuctionSpecification"() {
        given:
        CreateAuctionSpecification specification = CreateAuctionSpecification.Builder.aCreateAuctionSpecification()
                .code("HHH-XXX")
                .minimalPrice(Money.from("10.00", PLN))
                .startDate(givenDateTime(01, 18))
                .endDate(givenDateTime(02, 18))
                .build()
        when:
        Auction auction = createFrom(specification)
        then:
        auction.id
        auction.code == specification.code
        auction.startDate == specification.startDate
        auction.endDate == specification.endDate
        auction.minimalPrice == specification.minimalPrice
        auction.status == NOT_STARTED
    }

    def "should throw exception on create Auction from CreateAuctionSpecification when specification is null"() {
        when:
        createFrom(null)
        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'specification is null'
    }

    def "should start Auction that is eligible for starting"() {
        given:
        Auction auction = givenNotStartedAuction()
        Time time = Stub()
        time.now() >> auction.getStartDate().plusMinutes(1)

        when:
        auction.startAuction(time)

        then:
        auction.status == STARTED
    }

    def "should throw exception on start Auction when provided Time is null"() {
        given:
        Auction auction = givenNotStartedAuction()

        when:
        auction.startAuction(null)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'time is null'
    }

    def "should throw exception on start Auction when Auction is already started"() {
        given:
        Auction auction = givenStartedAuctionWithNoBets()
        Time time = Stub()
        time.now() >> auction.getStartDate().plusMinutes(1)

        when:
        auction.startAuction(time)

        then:
        IllegalStateException ex = thrown()
        ex.message == 'Auction is not eligible for starting'
    }

    def "should throw exception on start Auction when Auction start time is future time"() {
        given:
        Auction auction = givenNotStartedAuction()
        Time time = Stub()
        time.now() >> auction.getStartDate().minusMinutes(1)

        when:
        auction.startAuction(time)

        then:
        IllegalStateException ex = thrown()
        ex.message == 'Auction is not eligible for starting'
    }

    def "should add first Bet to started Auction when minimal price is met"() {
        given:
        Auction auction = givenStartedAuctionWithNoBets()

        PlaceBetSpecification specification = PlaceBetSpecification.builder()
                .auctionCode(auction.getCode())
                .userId(Id.generate())
                .price(auction.getMinimalPrice().add(Money.from('10', PLN)))
                .build()

        Time time = Stub()
        time.now() >> auction.getStartDate().plusMinutes(10)

        when:
        PlaceBetResult result = auction.placeBet(specification, time)

        then:
        result.status == SUCCESS
        result.specification == specification
        auction.currentAuctionedPrice() == specification.getPrice()
    }

    def "should not add first Bet to started Auction when minimal price is not met"() {
        given:
        Auction auction = givenStartedAuctionWithNoBets()

        PlaceBetSpecification specification = PlaceBetSpecification.builder()
                .auctionCode(auction.getCode())
                .userId(Id.generate())
                .price(auction.getMinimalPrice().subtract(Money.from('1', PLN)))
                .build()

        Time time = Stub()
        time.now() >> auction.getStartDate().plusMinutes(10)

        when:
        PlaceBetResult result = auction.placeBet(specification, time)

        then:
        result.status == FAILURE_PRICE_LOWER_THAN_MINIMAL_PRICE
        result.specification == specification
        auction.currentAuctionedPrice() == Money.from('0', PLN)
    }

    def "should not add first Bet when Auction is not started"() {
        given:
        Auction auction = givenNotStartedAuction()

        PlaceBetSpecification specification = PlaceBetSpecification.builder()
                .auctionCode(auction.getCode())
                .userId(Id.generate())
                .price(auction.getMinimalPrice().add(Money.from('10', PLN)))
                .build()

        Time time = Stub()
        time.now() >> auction.getStartDate().plusMinutes(10)

        when:
        PlaceBetResult result = auction.placeBet(specification, time)

        then:
        result.status == FAILURE_AUCTION_NOT_STARTED
        result.specification == specification
        auction.currentAuctionedPrice() == Money.from('0', PLN)
    }

    def "should not add first Bet when Auction is finished"() {
        given:
        Auction auction = givenFinishedAuctionWithNoBets()

        PlaceBetSpecification specification = PlaceBetSpecification.builder()
                .auctionCode(auction.getCode())
                .userId(Id.generate())
                .price(auction.getMinimalPrice().add(Money.from('10', PLN)))
                .build()

        Time time = Stub()
        time.now() >> auction.getEndDate().plusMinutes(10)

        when:
        PlaceBetResult result = auction.placeBet(specification, time)

        then:
        result.status == FAILURE_AUCTION_FINISHED
        result.specification == specification
        auction.currentAuctionedPrice() == Money.from('0', PLN)
    }

    def "should not add Bet when Auction top Bet price is higher than bet specification price"() {
        given:
        Auction auction = givenStartedAuctionWithBets()

        PlaceBetSpecification specification = PlaceBetSpecification.builder()
                .auctionCode(auction.getCode())
                .userId(Id.generate())
                .price(auction.currentAuctionedPrice().subtract(Money.from('5', PLN)))
                .build()

        Time time = Stub()
        time.now() >> auction.getLastBet().get().getCreationTime().plusMinutes(10)

        when:
        PlaceBetResult result = auction.placeBet(specification, time)

        then:
        result.status == FAILURE_PRICE_TOO_LOW
        result.specification == specification
        auction.currentAuctionedPrice() == auction.getLastBet().get().getPrice()
    }

    def "should add new top Bet when Auction top Bet price is lower than bet specification price"() {
        given:
        Auction auction = givenStartedAuctionWithBets()

        PlaceBetSpecification specification = PlaceBetSpecification.builder()
                .auctionCode(auction.getCode())
                .userId(Id.generate())
                .price(auction.currentAuctionedPrice().add(Money.from('5', PLN)))
                .build()

        Time time = Stub()
        time.now() >> auction.getLastBet().get().getCreationTime().plusMinutes(10)

        when:
        PlaceBetResult result = auction.placeBet(specification, time)

        then:
        result.status == SUCCESS
        result.specification == specification
        auction.currentAuctionedPrice() == specification.getPrice()
    }

    def "should finish Auction that is eligible for finishing with no bets placed"() {
        given:
        Auction auction = givenStartedAuctionWithNoBets()
        Time time = Stub()
        time.now() >> auction.getEndDate().plusMinutes(1)

        when:
        auction.finishAuction(time)

        then:
        auction.status == FINISHED_NOT_SOLD
    }

    def "should finish Auction that is eligible for finishing with bets placed"() {
        given:
        Auction auction = givenStartedAuctionWithBets()
        Time time = Stub()
        time.now() >> auction.getEndDate().plusMinutes(1)

        when:
        auction.finishAuction(time)

        then:
        auction.status == FINISHED_SOLD
    }

    def "should throw exception on finish Auction when provided Time is null"() {
        given:
        Auction auction = givenStartedAuctionWithBets()

        when:
        auction.finishAuction(null)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'time is null'
    }

    def "should throw exception on finish Auction when Auction is not eligible for finishing"() {
        given:
        Auction auction = givenStartedAuctionWithBets()
        Time time = Stub()
        time.now() >> auction.getEndDate().minusMinutes(1)

        when:
        auction.finishAuction(time)

        then:
        IllegalStateException ex = thrown()
        ex.message == 'Auction is not eligible for finishing'
    }
}
