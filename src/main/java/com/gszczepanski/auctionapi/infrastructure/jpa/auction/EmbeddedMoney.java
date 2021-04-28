package com.gszczepanski.auctionapi.infrastructure.jpa.auction;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;

@Embeddable
@Value
@AllArgsConstructor
public class EmbeddedMoney {

    @NotNull
    @Column(name = "money_amount")
    private final BigDecimal amount;

    @NotNull
    @Column(name = "money_currency")
    private final String currency;

    /**
     * For Hibernate
     */
    EmbeddedMoney() {
        this.amount = null;
        this.currency = null;
    }

}
