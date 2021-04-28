package com.gszczepanski.auctionapi.domain.auction;

import com.gszczepanski.auctionapi.domain.auction.Auction.PlaceBetResult;

public interface AuctionEventPublisher {

    void publishAuctionCreated(AuctionSnapshot snapshot);

    void publishAuctionStarted(AuctionSnapshot snapshot);

    void publishAuctionFinished(AuctionSnapshot snapshot);

    void publishBetOperationPerformed(AuctionSnapshot snapshot, PlaceBetResult placeBetResult);

}
