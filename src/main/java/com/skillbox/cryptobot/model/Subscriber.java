package com.skillbox.cryptobot.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscribers")
@Data
@NoArgsConstructor
public class Subscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_uuid", columnDefinition = "uuid", updatable = false)
    private UUID id;

    @Column(name = "telegram_id")
    private Long telegramId;

    @Column(name = "subscription_price")
    private BigDecimal subscriptionPrice;

    @Column(name = "last_notified")
    private LocalDateTime lastNotified;
}

