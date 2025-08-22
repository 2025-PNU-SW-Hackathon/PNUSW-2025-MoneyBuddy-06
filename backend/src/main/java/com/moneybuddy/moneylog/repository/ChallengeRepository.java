package com.moneybuddy.moneylog.repository;

import com.moneybuddy.moneylog.domain.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    List<Challenge> findByIsSharedTrue();
    List<Challenge> findByIsSystemGeneratedTrueAndMobtiTypeIn(List<String> mobtiTypes);

    List<Challenge> findByTypeAndCategoryAndIsSharedTrue(String type, String category);

    List<Challenge> findByTypeAndIsSharedTrue(String type);

    List<Challenge> findByTypeAndCategoryAndIsAccountLinkedAndIsSharedTrue(String type, String category, boolean isAccountLinked);

}