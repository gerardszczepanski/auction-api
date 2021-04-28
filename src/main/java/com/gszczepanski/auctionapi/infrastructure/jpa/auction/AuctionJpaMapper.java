package com.gszczepanski.auctionapi.infrastructure.jpa.auction;

import org.springframework.stereotype.Component;

import com.gszczepanski.auctionapi.domain.Id;
import com.gszczepanski.auctionapi.domain.Money;
import com.gszczepanski.auctionapi.domain.auction.AuctionSnapshot;
import com.gszczepanski.auctionapi.domain.auction.AuctionSnapshot.BetSnapshot;

import static com.google.common.base.Preconditions.checkArgument;
import static com.gszczepanski.auctionapi.domain.Money.Currency;
import static com.gszczepanski.auctionapi.domain.auction.Auction.AuctionStatus;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

@Component
class AuctionJpaMapper {

    AuctionSnapshot fromEntity(AuctionEntity entity) {
        checkArgument(nonNull(entity), "entity is null");
        return AuctionSnapshot.builder()
                .id(Id.from(entity.getId()))
                .code(entity.getCode())
                .status(AuctionStatus.valueOf(entity.getStatus()))
                .minimalPrice(Money.from(
                        entity.getMinimalPrice().getAmount(),
                        Currency.valueOf(entity.getMinimalPrice().getCurrency())
                ))
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .bets(
                        entity.getBets()
                                .stream()
                                .map(betEntity -> toBetSnapshot(betEntity))
                                .collect(toList())
                )
                .creationTime(entity.getCreationTime())
                .version(entity.getVersion())
                .build();
    }

    private BetSnapshot toBetSnapshot(BetEntity betEntity) {
        return BetSnapshot.builder()
                .id(Id.from(betEntity.getId()))
                .userId(Id.from(betEntity.getUserId()))
                .price(Money.from(
                        betEntity.getPrice().getAmount(),
                        Currency.valueOf(betEntity.getPrice().getCurrency())
                ))
                .creationTime(betEntity.getCreationTime())
                .build();
    }

    AuctionEntity toEntity(AuctionSnapshot auctionSnapshot) {
        checkArgument(nonNull(auctionSnapshot), "auctionSnapshot is null");

        return AuctionEntity.builder()
                .id(auctionSnapshot.getId().asString())
                .code(auctionSnapshot.getCode())
                .startDate(auctionSnapshot.getStartDate())
                .endDate(auctionSnapshot.getEndDate())
                .creationTime(auctionSnapshot.getCreationTime())
                .version(auctionSnapshot.getVersion())
                .status(auctionSnapshot.getStatus().name())
                .minimalPrice(
                        new EmbeddedMoney(
                                auctionSnapshot.getMinimalPrice().getAmount(),
                                auctionSnapshot.getMinimalPrice().getCurrency().name()
                        )
                )
                .bets(
                        auctionSnapshot.getBets()
                                .stream()
                                .map(betSnapshot -> toBetEntity(betSnapshot, auctionSnapshot.getId()))
                                .collect(toList())
                )
                .build();
    }

    BetEntity toBetEntity(BetSnapshot betSnapshot, Id auctionId) {
        return BetEntity.builder()
                .id(betSnapshot.getId().asString())
                .userId(betSnapshot.getUserId().asString())
                .auction(
                        AuctionEntity.builder().id(auctionId.asString()).build()
                )
                .creationTime(betSnapshot.getCreationTime())
                .price(
                        new EmbeddedMoney(
                                betSnapshot.getPrice().getAmount(),
                                betSnapshot.getPrice().getCurrency().name()
                        )
                )
                .build();
    }

}
