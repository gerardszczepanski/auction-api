package com.gszczepanski.auctionapi.domain.auction

import com.gszczepanski.auctionapi.IntegrationBaseSpec
import org.springframework.beans.factory.annotation.Autowired

class AuctionsIntSpec extends IntegrationBaseSpec {

    @Autowired
    Auctions auctions

    def "should find Auction by auction code"() {
        given:
            String auctionCode = 'AAA-QWER'
        when:
            Optional<AuctionSnapshot> foundAuctionSnapshot = auctions.findOne(auctionCode)
        then:
            foundAuctionSnapshot.isPresent()
            AuctionSnapshot auctionSnapshot = foundAuctionSnapshot.get()
            auctionSnapshot.code == auctionCode
            auctionSnapshot.bets.size() == 1
            auctionSnapshot.getCurrentAuctionedPrice() == auctionSnapshot.bets[0].getPrice()
        cleanup:
            cleanUpDatabase()
    }
}
