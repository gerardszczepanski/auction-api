package com.gszczepanski.auctionapi.domain.auction;

import com.gszczepanski.auctionapi.domain.Id;
import com.gszczepanski.auctionapi.domain.Money;
import lombok.Value;

@Value
public class PlaceBetSpecification {

    private final String auctionCode;

    private final Id userId;

    private final Money price;

}
