package com.gszczepanski.auctionapi.infrastructure.jpa.auction;

import java.time.OffsetDateTime;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bets")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class BetEntity {

    @Id
    private String id;

    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id")
    private AuctionEntity auction;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount",
                    column = @Column(name = "price_amount")),
            @AttributeOverride(name = "currency",
                    column = @Column(name = "price_currency"))
    })
    private EmbeddedMoney price;

    private OffsetDateTime creationTime;

}
