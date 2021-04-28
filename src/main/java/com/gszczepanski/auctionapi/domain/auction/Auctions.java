package com.gszczepanski.auctionapi.domain.auction;

import com.gszczepanski.auctionapi.domain.Id;
import com.gszczepanski.auctionapi.domain.Time;
import com.gszczepanski.auctionapi.domain.auction.Auction.PlaceBetResult;
import com.gszczepanski.auctionapi.domain.auction.AuctionRepository.AuctionQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.gszczepanski.auctionapi.domain.auction.Auction.AuctionStatus.NOT_STARTED;
import static com.gszczepanski.auctionapi.domain.auction.Auction.AuctionStatus.STARTED;
import static com.gszczepanski.auctionapi.domain.auction.Auction.PlaceBetResultStatus.SUCCESS;
import static com.gszczepanski.auctionapi.domain.auction.AuctionRepository.AuctionQuery.queryForCode;
import static java.lang.String.format;
import static java.util.Objects.nonNull;

@Service
@Transactional
@RequiredArgsConstructor
public class Auctions {

    private final AuctionRepository auctionRepository;
    private final AuctionEventPublisher auctionEventPublisher;
    private final Time time;

    public Id createAuction(CreateAuctionSpecification specification) {
        checkArgument(nonNull(specification), "specification is null");

        Auction auction = Auction.createFrom(specification);
        auctionRepository.save(auction);
        auctionEventPublisher.publishAuctionCreated(auction.asSnapshot());
        return auction.getId();
    }

    public void startEligibleAuctions() {
        AuctionQuery query = AuctionQuery.builder().statuses(List.of(NOT_STARTED)).build();
        List<Auction> auctions = auctionRepository.findAll(query);

        auctions.stream()
                .filter(auction -> auction.isEligibleForStarting(time))
                .forEach(auction -> {
                    auction.startAuction(time);
                    auctionRepository.save(auction);
                    auctionEventPublisher.publishAuctionStarted(auction.asSnapshot());
                });
    }

    public void finishEligibleAuctions() {
        AuctionQuery query = AuctionQuery.builder().statuses(List.of(STARTED)).build();
        List<Auction> auctions = auctionRepository.findAll(query);

        auctions.stream()
                .filter(auction -> auction.isEligibleForFinishing(time))
                .forEach(auction -> {
                    auction.finishAuction(time);
                    auctionRepository.save(auction);
                    auctionEventPublisher.publishAuctionFinished(auction.asSnapshot());
                });
    }

    public PlaceBetResult placeBet(PlaceBetSpecification specification) {
        checkArgument(nonNull(specification), "specification is null");
        Auction auction = findAuctionByCode(specification);

        PlaceBetResult result = auction.placeBet(specification, time);
        if (result.getStatus() == SUCCESS) {
            auctionRepository.save(auction);
        }
        auctionEventPublisher.publishBetOperationPerformed(auction.asSnapshot(), result);
        return result;
    }

    private Auction findAuctionByCode(PlaceBetSpecification specification) {
        return auctionRepository.findOne(
                queryForCode(specification.getAuctionCode())
        ).orElseThrow(() -> new IllegalArgumentException(format("Auction not found for code %s", specification.getAuctionCode())));
    }

}
