package com.gszczepanski.auctionapi.infrastructure.jpa.auction;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.gszczepanski.auctionapi.domain.auction.AuctionRepository;
import com.gszczepanski.auctionapi.domain.auction.AuctionSnapshot;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
class AuctionJpaRepository implements AuctionRepository {

    private final AuctionJpaMapper auctionJpaMapper;

    private final AuctionEntityJpaRepository auctionEntityJpaRepository;

    @Override
    public AuctionSnapshot save(AuctionSnapshot auctionSnapshot) {
        checkArgument(nonNull(auctionSnapshot), "auctionSnapshot is null");

        AuctionEntity entityToSave = auctionJpaMapper.toEntity(auctionSnapshot);
        AuctionEntity savedEntity = auctionEntityJpaRepository.save(entityToSave);
        return auctionJpaMapper.fromEntity(savedEntity);
    }

    @Override
    public Optional<AuctionSnapshot> findOne(AuctionQuery query) {
        checkArgument(nonNull(query), "query is null");

        BooleanExpression predicate = AuctionEntity.createPredicateFrom(query);
        Optional<AuctionEntity> maybeAuction = auctionEntityJpaRepository.findOne(predicate);
        return maybeAuction.map(auctionJpaMapper::fromEntity);
    }

    @Override
    public List<AuctionSnapshot> findAll(AuctionQuery query) {
        checkArgument(nonNull(query), "query is null");

        BooleanExpression predicate = AuctionEntity.createPredicateFrom(query);
        return auctionEntityJpaRepository.findAll(predicate).stream()
                .map(auctionJpaMapper::fromEntity)
                .collect(toList());
    }
}
