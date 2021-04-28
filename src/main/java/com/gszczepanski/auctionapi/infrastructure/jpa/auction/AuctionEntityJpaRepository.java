package com.gszczepanski.auctionapi.infrastructure.jpa.auction;

import java.util.List;
import java.util.Optional;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.querydsl.core.types.Predicate;
import lombok.Generated;

@Generated
public interface AuctionEntityJpaRepository extends PagingAndSortingRepository<AuctionEntity, String>, QuerydslPredicateExecutor {

    List<AuctionEntity> findAll(Predicate predicate);

    Optional<AuctionEntity> findOne(Predicate predicate);

}
