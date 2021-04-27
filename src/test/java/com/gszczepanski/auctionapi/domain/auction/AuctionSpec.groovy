package com.gszczepanski.auctionapi.domain.auction

import com.gszczepanski.auctionapi.domain.Money
import com.gszczepanski.auctionapi.domain.Time
import spock.lang.Specification

import static com.gszczepanski.auctionapi.domain.Money.Currency.PLN
import static com.gszczepanski.auctionapi.domain.auction.Auction.AuctionStatus.STARTED
import static com.gszczepanski.auctionapi.domain.auction.Auction.AuctionStatus.NOT_STARTED

class AuctionSpec extends Specification implements WithAuctionFixtures {

    def "should create Auction from CreateAuctionSpecification"() {
        given:
            CreateAuctionSpecification specification = CreateAuctionSpecification.Builder.aCreateAuctionSpecification()
                    .code("HHH-XXX")
                    .startPrice(Money.from("0.00", PLN))
                    .minimalPrice(Money.from("10.00", PLN))
                    .startDate(givenDateTime(01, 18))
                    .endDate(givenDateTime(02, 18))
                    .build()
        when:
            Auction auction = Auction.createFrom(specification)
        then:
            auction.id
            auction.code == specification.code
            auction.startDate == specification.startDate
            auction.endDate == specification.endDate
            auction.startPrice == specification.startPrice
            auction.minimalPrice == specification.minimalPrice
            auction.status == NOT_STARTED
    }

    def "should throw exception on create Auction from CreateAuctionSpecification when specification is null"() {
        when:
            Auction.createFrom(null)
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
}
