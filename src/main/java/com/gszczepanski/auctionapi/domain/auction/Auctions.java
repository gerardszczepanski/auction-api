package com.gszczepanski.auctionapi.domain.auction;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gszczepanski.auctionapi.domain.Id;
import com.gszczepanski.auctionapi.domain.Time;
import com.gszczepanski.auctionapi.domain.auction.Auction.PlaceBetResult;
import com.gszczepanski.auctionapi.domain.auction.AuctionRepository.AuctionQuery;
import lombok.RequiredArgsConstructor;

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

        Auction auction = Auction.createFrom(specification, time);
        AuctionSnapshot auctionSnapshot = auction.asSnapshot();
        auctionRepository.save(auctionSnapshot);
        auctionEventPublisher.publishAuctionCreated(auctionSnapshot);

        return auction.getId();
    }

    public void startEligibleAuctions() {
        AuctionQuery query = AuctionQuery.builder().statuses(List.of(NOT_STARTED)).build();
        List<AuctionSnapshot> auctions = auctionRepository.findAll(query);

        auctions.stream()
                .map(Auction::restoreFrom)
                .filter(auction -> auction.isEligibleForStarting(time))
                .forEach(auction -> {
                    auction.startAuction(time);
                    AuctionSnapshot auctionSnapshot = auction.asSnapshot();
                    auctionRepository.save(auctionSnapshot);
                    auctionEventPublisher.publishAuctionStarted(auctionSnapshot);
                });
    }

    public void finishEligibleAuctions() {
        AuctionQuery query = AuctionQuery.builder().statuses(List.of(STARTED)).build();
        List<AuctionSnapshot> auctions = auctionRepository.findAll(query);

        auctions.stream()
                .map(Auction::restoreFrom)
                .filter(auction -> auction.isEligibleForFinishing(time))
                .forEach(auction -> {
                    auction.finishAuction(time);
                    AuctionSnapshot auctionSnapshot = auction.asSnapshot();
                    auctionRepository.save(auctionSnapshot);
                    auctionEventPublisher.publishAuctionFinished(auctionSnapshot);
                });
    }

    public PlaceBetResult placeBet(PlaceBetSpecification specification) {
        checkArgument(nonNull(specification), "specification is null");
        Auction auction = findAuctionByCode(specification);

        PlaceBetResult result = auction.placeBet(specification, time);
        if (result.getStatus() == SUCCESS) {
            auctionRepository.save(auction.asSnapshot());
        }
        auctionEventPublisher.publishBetOperationPerformed(auction.asSnapshot(), result);
        return result;
    }

    public Optional<AuctionSnapshot> findOne(String auctionCode) {
        checkArgument(nonNull(auctionCode), "auctionCode is null");
        return auctionRepository.findOne(queryForCode(auctionCode))
                .map(Auction::restoreFrom)
                .map(Auction::asSnapshot);
    }

    private Auction findAuctionByCode(PlaceBetSpecification specification) {
        return auctionRepository.findOne(
                queryForCode(specification.getAuctionCode())
        )
                .map(Auction::restoreFrom)
                .orElseThrow(() -> new IllegalArgumentException(format("Auction not found for code %s", specification.getAuctionCode())));
    }

}
