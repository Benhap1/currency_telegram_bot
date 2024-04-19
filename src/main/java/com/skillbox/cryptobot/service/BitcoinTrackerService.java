package com.skillbox.cryptobot.service;

import com.skillbox.cryptobot.client.BinanceClient;
import com.skillbox.cryptobot.model.Subscriber;
import com.skillbox.cryptobot.model.SubscriberRepository;
import com.skillbox.cryptobot.utils.TextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class BitcoinTrackerService {

    private final BinanceClient binanceClient;
    private final SubscriberRepository subscriberRepository;
    private final int notifyDelayMinutes;
    private final AbsSender telegramBot;

    public BitcoinTrackerService(BinanceClient binanceClient, SubscriberRepository subscriberRepository,
                                 @Value("${telegram.notify.delay.value}") int notifyDelayValue,
                                 @Value("${telegram.notify.delay.unit}") TimeUnit notifyDelayUnit, AbsSender telegramBot) {
        this.binanceClient = binanceClient;
        this.subscriberRepository = subscriberRepository;
        this.notifyDelayMinutes = (int) Duration.of(notifyDelayValue, notifyDelayUnit.toChronoUnit()).toMinutes();
        this.telegramBot = telegramBot;
        startTracking();
    }

    private void startTracking() {
        try (ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1)) {
            scheduler.scheduleAtFixedRate(this::checkBitcoinPriceAndNotifySubscribers, 0, 2, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("Error occurred while starting tracking", e);
        }
    }


    private void checkBitcoinPriceAndNotifySubscribers() {
        log.info("Checking bitcoin price and notifying subscribers...");
        try {
            BigDecimal currentPrice = binanceClient.getBitcoinPrice();
            LocalDateTime lastNotificationTime = LocalDateTime.now().minusMinutes(notifyDelayMinutes);

            List<Subscriber> subscribers = subscriberRepository.findAllBySubscriptionPriceGreaterThan(currentPrice);
            subscribers.forEach(subscriber -> {
                if (subscriber.getLastNotified() == null || subscriber.getLastNotified().isBefore(lastNotificationTime)) {
                    notifySubscriber(subscriber, currentPrice);
                    subscriber.setLastNotified(LocalDateTime.now());
                    subscriberRepository.save(subscriber);
                }
            });
        } catch (IOException e) {
            log.error("Error while checking bitcoin price and notifying subscribers", e);
        }
    }

    private void notifySubscriber(Subscriber subscriber, BigDecimal currentPrice) {
        log.info("Sending notification to subscriber {}", subscriber.getTelegramId());
        SendMessage answer = new SendMessage();
        answer.setChatId(String.valueOf(subscriber.getTelegramId()));
        String roundedPrice = TextUtil.toString(currentPrice);
        answer.setText("Пора покупать, стоимость биткоина " + roundedPrice + " USD");
        try {
            telegramBot.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Error while sending notification to subscriber", e);
        }
    }
}
