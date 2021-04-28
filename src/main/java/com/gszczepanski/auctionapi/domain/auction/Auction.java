package com.gszczepanski.auctionapi.domain.auction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;

import com.gszczepanski.auctionapi.domain.Id;
import com.gszczepanski.auctionapi.domain.Money;
import com.gszczepanski.auctionapi.domain.Time;
import com.gszczepanski.auctionapi.domain.auction.AuctionSnapshot.BetSnapshot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.gszczepanski.auctionapi.domain.auction.Auction.AuctionStatus.FINISHED_NOT_SOLD;
import static com.gszczepanski.auctionapi.domain.auction.Auction.AuctionStatus.FINISHED_SOLD;
import static com.gszczepanski.auctionapi.domain.auction.Auction.AuctionStatus.NOT_STARTED;
import static com.gszczepanski.auctionapi.domain.auction.Auction.AuctionStatus.STARTED;
import static com.gszczepanski.auctionapi.domain.auction.Auction.PlaceBetResultStatus.FAILURE_AUCTION_FINISHED;
import static com.gszczepanski.auctionapi.domain.auction.Auction.PlaceBetResultStatus.FAILURE_AUCTION_NOT_STARTED;
import static com.gszczepanski.auctionapi.domain.auction.Auction.PlaceBetResultStatus.FAILURE_PRICE_LOWER_THAN_MINIMAL_PRICE;
import static com.gszczepanski.auctionapi.domain.auction.Auction.PlaceBetResultStatus.FAILURE_PRICE_TOO_LOW;
import static com.gszczepanski.auctionapi.domain.auction.Auction.PlaceBetResultStatus.SUCCESS;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.NONE;
import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class Auction {

    private final Id id;

    private final String code;

    private final Money minimalPrice;

    private final OffsetDateTime startDate;

    private final OffsetDateTime endDate;

    @Getter(NONE)
    private final Deque<Bet> bets;

    private final OffsetDateTime creationTime;

    private final int version;

    private AuctionStatus status;

    static Auction createFrom(CreateAuctionSpecification specification, Time time) {
        checkArgument(nonNull(specification), "specification is null");
        checkArgument(nonNull(time), "time is null");

        return new Auction(
                Id.generate(),
                specification.getCode(),
                specification.getMinimalPrice(),
                specification.getStartDate(),
                specification.getEndDate(),
                new LinkedList<>(),
                time.now(),
                0,
                NOT_STARTED
        );
    }

    static Auction restoreFrom(AuctionSnapshot snapshot) {
        checkArgument(nonNull(snapshot), "snapshot is null");

        // TODO validate snapshot data

        Deque<Bet> bets = snapshot.getBets().stream()
                .map(Bet::fromSnapshot)
                .collect(toCollection(LinkedList::new));

        return new Auction(
                snapshot.getId(),
                snapshot.getCode(),
                snapshot.getMinimalPrice(),
                snapshot.getStartDate(),
                snapshot.getEndDate(),
                bets,
                snapshot.getCreationTime(),
                snapshot.getVersion(),
                snapshot.getStatus()
        );
    }

    void startAuction(Time time) {
        checkArgument(nonNull(time), "time is null");
        checkState(isEligibleForStarting(time), "Auction is not eligible for starting");

        status = STARTED;
    }

    boolean isEligibleForStarting(Time time) {
        checkArgument(nonNull(time), "time is null");
        return status == NOT_STARTED && startDate.compareTo(time.now()) <= 0;
    }

    void finishAuction(Time time) {
        checkArgument(nonNull(time), "time is null");
        checkState(isEligibleForFinishing(time), "Auction is not eligible for finishing");

        status = getLastBet()
                .map(betFound -> FINISHED_SOLD)
                .orElse(FINISHED_NOT_SOLD);
    }

    boolean isEligibleForFinishing(Time time) {
        checkArgument(nonNull(time), "time is null");
        return status == STARTED && time.now().compareTo(endDate) == 1;
    }

    PlaceBetResult placeBet(PlaceBetSpecification specification, Time time) {
        checkArgument(nonNull(specification), "specification is null");
        checkArgument(nonNull(time), "time is null");
        checkArgument(specification.getAuctionCode().equals(code), "bet specification is not for this auction");

        if (status == NOT_STARTED) {
            return PlaceBetResult.failureResult(FAILURE_AUCTION_NOT_STARTED, specification);
        }
        if (status == FINISHED_SOLD || status == FINISHED_NOT_SOLD) {
            return PlaceBetResult.failureResult(FAILURE_AUCTION_FINISHED, specification);
        }

        Optional<Bet> maybeLastBet = getLastBet();
        if (maybeLastBet.isEmpty()) {
            if (isBetCandidatePriceHigherOrEqualMinimalPrice(specification)) {
                return addNewTopBet(specification, time);
            }
            return PlaceBetResult.failureResult(FAILURE_PRICE_LOWER_THAN_MINIMAL_PRICE, specification);
        }

        Bet lastBet = maybeLastBet.get();
        if (isBetCandidatePriceHigherThanLastBetPrice(specification, lastBet)) {
            return addNewTopBet(specification, time);
        }
        return PlaceBetResult.failureResult(FAILURE_PRICE_TOO_LOW, specification);
    }

    Money currentAuctionedPrice() {
        return getLastBet()
                .map(Bet::getPrice)
                .orElseGet(() -> Money.from(BigDecimal.ZERO, minimalPrice.getCurrency()));
    }

    public AuctionSnapshot asSnapshot() {
        return AuctionSnapshot.builder()
                .id(id)
                .code(code)
                .startDate(startDate)
                .endDate(endDate)
                .minimalPrice(minimalPrice)
                .currentAuctionedPrice(currentAuctionedPrice())
                .version(version)
                .status(status)
                .bets(
                        bets.stream()
                                .map(Bet::asSnapshot)
                                .collect(toList())
                )
                .build();
    }

    private PlaceBetResult addNewTopBet(PlaceBetSpecification specification, Time time) {
        final Bet bet = assembleBetFrom(specification, time);
        bets.add(bet);
        return PlaceBetResult.successResult(specification, bet.asSnapshot());
    }

    private boolean isBetCandidatePriceHigherThanLastBetPrice(PlaceBetSpecification specification, Bet lastBet) {
        return specification.getPrice().getAmount().compareTo(lastBet.getPrice().getAmount()) == 1;
    }

    private boolean isBetCandidatePriceHigherOrEqualMinimalPrice(PlaceBetSpecification specification) {
        return specification.getPrice().getAmount().compareTo(minimalPrice.getAmount()) > -1;
    }

    private Bet assembleBetFrom(PlaceBetSpecification specification, Time time) {
        return new Bet(
                Id.generate(),
                specification.getUserId(),
                time.now(),
                specification.getPrice()
        );
    }

    Optional<Bet> getLastBet() {
        return bets.isEmpty() ? Optional.empty() : Optional.of(bets.getLast());
    }

    public enum AuctionStatus {

        NOT_STARTED,
        STARTED,
        FINISHED_SOLD,
        FINISHED_NOT_SOLD

    }

    enum PlaceBetResultStatus {
        SUCCESS,
        FAILURE_AUCTION_NOT_STARTED,
        FAILURE_AUCTION_FINISHED,
        FAILURE_PRICE_TOO_LOW,
        FAILURE_PRICE_LOWER_THAN_MINIMAL_PRICE
    }

    @Value
    @AllArgsConstructor(access = PRIVATE)
    public static class PlaceBetResult {

        private final PlaceBetResultStatus status;

        private final PlaceBetSpecification specification;

        @Getter(NONE)
        private final BetSnapshot betSnapshot;

        private static PlaceBetResult successResult(PlaceBetSpecification specification, BetSnapshot betSnapshot) {
            return new PlaceBetResult(SUCCESS, specification, betSnapshot);
        }

        private static PlaceBetResult failureResult(PlaceBetResultStatus status, PlaceBetSpecification specification) {
            return new PlaceBetResult(status, specification, null);
        }

        public Optional<BetSnapshot> getBetSnapshot() {
            return Optional.ofNullable(betSnapshot);
        }

    }

    @Value
    static class Bet {

        private final Id id;

        private final Id userId;

        private final OffsetDateTime creationTime;

        private final Money price;

        private static Bet fromSnapshot(BetSnapshot snapshot) {
            return new Bet(
                    snapshot.getId(),
                    snapshot.getUserId(),
                    snapshot.getCreationTime(),
                    snapshot.getPrice()
            );
        }

        private BetSnapshot asSnapshot() {
            return BetSnapshot.builder()
                    .id(id)
                    .userId(userId)
                    .price(price)
                    .creationTime(creationTime)
                    .build();
        }

    }

}
