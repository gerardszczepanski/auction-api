package com.gszczepanski.auctionapi.domain.auction;

import java.time.Duration;
import java.time.OffsetDateTime;

import com.gszczepanski.auctionapi.domain.Money;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.math.BigDecimal.ZERO;
import static java.util.Objects.nonNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@RequiredArgsConstructor(access = PRIVATE)
public class CreateAuctionSpecification {

    private final String code;

    private final Money startPrice;

    private final Money minimalPrice;

    private final OffsetDateTime startDate;

    private final OffsetDateTime endDate;

    public static final class Builder {

        private String code;

        private Money startPrice;

        private Money minimalPrice;

        private OffsetDateTime startDate;

        private OffsetDateTime endDate;

        private Builder() {
        }

        public static Builder aCreateAuctionSpecification() {
            return new Builder();
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder startPrice(Money startPrice) {
            this.startPrice = startPrice;
            return this;
        }

        public Builder minimalPrice(Money minimalPrice) {
            this.minimalPrice = minimalPrice;
            return this;
        }

        public Builder startDate(OffsetDateTime startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(OffsetDateTime endDate) {
            this.endDate = endDate;
            return this;
        }

        public CreateAuctionSpecification build() {
            checkArgument(!isNullOrEmpty(code), "code is empty");

            validatePrices();
            validateDates();

            return new CreateAuctionSpecification(code, startPrice, minimalPrice, startDate, endDate);
        }

        private void validatePrices() {
            checkArgument(nonNull(startPrice), "startPrice is null");
            checkArgument(nonNull(minimalPrice), "minimalPrice is null");

            checkArgument(startPrice.getAmount().compareTo(ZERO) != -1, "startPrice can not be negative");
            checkArgument(minimalPrice.getAmount().compareTo(ZERO) != -1, "minimalPrice can not be negative");
            checkArgument(startPrice.getAmount().compareTo(minimalPrice.getAmount()) != 1, "startPrice can not be greater than minimalPrice");
        }

        private void validateDates() {
            checkArgument(nonNull(startDate), "startDate is null");
            checkArgument(nonNull(endDate), "endDate is null");
            checkArgument(startDate.compareTo(endDate) == -1, "startDate must be before endDate");

            Duration duration = Duration.between(startDate, endDate);
            long hours = duration.toHours();

            checkArgument(hours >= 24 && hours <= 120, "Auction valid period is from 24 hours to 120 hours");
        }
    }
}
