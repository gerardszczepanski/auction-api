package com.gszczepanski.auctionapi.infrastructure.rabbitmq;

import org.springframework.stereotype.Component;

import com.gszczepanski.auctionapi.domain.auction.Auction.PlaceBetResult;
import com.gszczepanski.auctionapi.domain.auction.AuctionEventPublisher;
import com.gszczepanski.auctionapi.domain.auction.AuctionSnapshot;

@Component
class RabbitMqAuctionEventPublisher implements AuctionEventPublisher {

    @Override
    public void publishAuctionCreated(AuctionSnapshot snapshot) {

    }

    @Override
    public void publishAuctionStarted(AuctionSnapshot snapshot) {

    }

    @Override
    public void publishAuctionFinished(AuctionSnapshot snapshot) {

    }

    @Override
    public void publishBetOperationPerformed(AuctionSnapshot snapshot, PlaceBetResult placeBetResult) {

    }
}
