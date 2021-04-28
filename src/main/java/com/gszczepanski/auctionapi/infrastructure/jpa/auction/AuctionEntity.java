package com.gszczepanski.auctionapi.infrastructure.jpa.auction;

import java.time.OffsetDateTime;
import java.util.List;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import com.gszczepanski.auctionapi.domain.auction.AuctionRepository;
import com.gszczepanski.auctionapi.domain.auction.AuctionRepository.AuctionQuery;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;

@Entity
@Table(name = "auctions")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class AuctionEntity {

    @Id
    private String id;

    private String code;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount",
                    column = @Column(name = "minimal_price_amount")),
            @AttributeOverride(name = "currency",
                    column = @Column(name = "minimal_price_currency"))
    })
    private EmbeddedMoney minimalPrice;

    private OffsetDateTime startDate;

    private OffsetDateTime endDate;

    @OneToMany
    @JoinColumn(name = "auction_id")
    private List<BetEntity> bets;

    private String status;

    @Version
    private int version;

    private OffsetDateTime creationTime;

    static BooleanExpression createPredicateFrom(AuctionQuery query) {
        checkArgument(nonNull(query), "query is null");
        BooleanExpression result = QAuctionEntity.auctionEntity.status.in(
                query.getStatuses().stream().map(status -> status.name()).toArray(String[]::new)
        );

        return query.getCode()
                .map(code -> result.and(QAuctionEntity.auctionEntity.code.eq(code)))
                .orElse(result);
    }

}
