package com.skillbox.cryptobot.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    boolean existsByTelegramId(Long telegramId);
    Optional<Subscriber> findByTelegramId(Long telegramId);
    List<Subscriber> findAllBySubscriptionPriceGreaterThan(BigDecimal currentPrice);


}

