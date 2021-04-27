package com.gszczepanski.auctionapi.domain;

import java.math.BigDecimal;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Objects.nonNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@RequiredArgsConstructor(access = PRIVATE)
public class Money {

    private final BigDecimal amount;

    private final Currency currency;

    public static Money from(String amount, Currency currency) {
        checkArgument(!isNullOrEmpty(amount), "Amount is empty");
        checkArgument(nonNull(currency), "Currency is null");
        return new Money(new BigDecimal(amount), currency);
    }

    public static Money from(BigDecimal amount, Currency currency) {
        checkArgument(nonNull(amount), "Amount is null");
        checkArgument(nonNull(currency), "Currency is null");
        return new Money(amount, currency);
    }

    public enum Currency {

        PLN;

    }

}
