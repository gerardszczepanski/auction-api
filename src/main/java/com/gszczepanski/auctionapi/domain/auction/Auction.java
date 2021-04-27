package com.gszczepanski.auctionapi.domain.auction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;

import com.gszczepanski.auctionapi.domain.Id;
import com.gszczepanski.auctionapi.domain.Money;
import com.gszczepanski.auctionapi.domain.Time;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.gszczepanski.auctionapi.domain.auction.Auction.AuctionStatus.STARTED;
import static com.gszczepanski.auctionapi.domain.auction.Auction.AuctionStatus.FINISHED_NOT_SOLD;
import static com.gszczepanski.auctionapi.domain.auction.Auction.AuctionStatus.FINISHED_SOLD;
import static com.gszczepanski.auctionapi.domain.auction.Auction.AuctionStatus.NOT_STARTED;
import static java.util.Objects.nonNull;
import static lombok.AccessLevel.NONE;
import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class Auction {

    private final Id id;

    private final String code;

    private final Money startPrice;

    private final Money minimalPrice;

    private final OffsetDateTime startDate;

    private final OffsetDateTime endDate;

    @Getter(NONE)
    private final Deque<Bet> bets;

    private AuctionStatus status;

    private int version;

    static Auction createFrom(CreateAuctionSpecification specification) {
        checkArgument(nonNull(specification), "specification is null");

        return new Auction(
                Id.generate(),
                specification.getCode(),
                specification.getStartPrice(),
                specification.getMinimalPrice(),
                specification.getStartDate(),
                specification.getEndDate(),
                new LinkedList<>(),
                NOT_STARTED,
                0
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
                .map(bet -> calculateFinishStatusBasedOnBet(bet))
                .orElse(FINISHED_NOT_SOLD);
    }

    boolean isEligibleForFinishing(Time time) {
        checkArgument(nonNull(time), "time is null");
        return status == STARTED && time.now().compareTo(endDate) == 1;
    }

    private AuctionStatus calculateFinishStatusBasedOnBet(Bet bet) {
        if (bet.getPrice().getAmount().compareTo(minimalPrice.getAmount()) > -1) {
            return FINISHED_SOLD;
        }
        return FINISHED_NOT_SOLD;
    }

    PlaceBetResult placeBet(PlaceBetSpecification specification, Time time) {
        checkArgument(nonNull(specification), "specification is null");
        checkArgument(nonNull(time), "time is null");

        if (status == NOT_STARTED) {
            return PlaceBetResult.FAILURE_AUCTION_NOT_STARTED;
        }
        if (status == FINISHED_SOLD || status == FINISHED_NOT_SOLD) {
            return PlaceBetResult.FAILURE_AUCTION_FINISHED;
        }

        Optional<Bet> maybeLastBet = getLastBet();
        if (maybeLastBet.isEmpty()) {
            if (isBetCandidatePriceHigherOrEqualMinimalPrice(specification)) {
                return addNewTopBet(specification, time);
            }
            return PlaceBetResult.FAILURE_PRICE_LOWER_THAN_MINIMAL_PRICE;
        }

        Bet lastBet = maybeLastBet.get();
        if (isBetCandidatePriceHigherThanLastBetPrice(specification, lastBet)) {
            return addNewTopBet(specification, time);
        }
        return PlaceBetResult.FAILURE_PRICE_TOO_LOW;
    }

    Money currentAuctionedPrice() {
        return getLastBet()
                .map(bet -> bet.getPrice())
                .orElseGet(() -> Money.from(BigDecimal.ZERO, startPrice.getCurrency()));
    }

    private PlaceBetResult addNewTopBet(PlaceBetSpecification specification, Time time) {
        bets.add(assembleBetFrom(specification, time));
        return PlaceBetResult.SUCCESS;
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

    private Optional<Bet> getLastBet() {
        return bets.isEmpty() ? Optional.empty() : Optional.of(bets.getLast());
    }

    enum AuctionStatus {

        NOT_STARTED,
        STARTED,
        FINISHED_SOLD,
        FINISHED_NOT_SOLD;

    }

    enum PlaceBetResult {
        SUCCESS,
        FAILURE_AUCTION_NOT_STARTED,
        FAILURE_AUCTION_FINISHED,
        FAILURE_PRICE_TOO_LOW,
        FAILURE_PRICE_LOWER_THAN_MINIMAL_PRICE
    }

    @Value
    class Bet {

        private final Id betId;

        private final Id userId;

        private final OffsetDateTime creationTime;

        private final Money price;

    }

}
