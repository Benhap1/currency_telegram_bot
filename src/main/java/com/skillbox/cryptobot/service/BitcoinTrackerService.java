package com.skillbox.cryptobot.service;

import com.skillbox.cryptobot.client.BinanceClient;
import com.skillbox.cryptobot.model.Subscriber;
import com.skillbox.cryptobot.model.SubscriberRepository;
import com.skillbox.cryptobot.utils.TextUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private BinanceClient binanceClient;

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Value("${telegram.notify.delay.value}")
    private int notifyDelayValue;

    @Value("${telegram.notify.delay.unit}")
    private TimeUnit notifyDelayUnit;

    @Value("${telegram.parsing.interval.value}")
    private int parsingIntervalValue;

    @Value("${telegram.parsing.interval.unit}")
    private TimeUnit parsingIntervalUnit;

    @Autowired
    private AbsSender telegramBot;

    private int notifyDelayMinutes;
    private int parsingIntervalMinutes;

    @PostConstruct
    private void init() {
        notifyDelayMinutes = (int) Duration.of(notifyDelayValue, notifyDelayUnit.toChronoUnit()).toMinutes();
        parsingIntervalMinutes = (int) Duration.of(parsingIntervalValue, parsingIntervalUnit.toChronoUnit()).toMinutes();
        startTracking();
    }

    private void startTracking() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::checkBitcoinPriceAndNotifySubscribers, 0, parsingIntervalMinutes, TimeUnit.MINUTES);
    }

    private void checkBitcoinPriceAndNotifySubscribers() {
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
            log.error("Ошибка при парсинге курса биткоина и уведомления", e);
        }
    }

    private void notifySubscriber(Subscriber subscriber, BigDecimal currentPrice) {
        SendMessage answer = new SendMessage();
        answer.setChatId(String.valueOf(subscriber.getTelegramId()));
        String roundedPrice = TextUtil.toString(currentPrice);
        answer.setText("Пора покупать, стоимость биткоина " + roundedPrice + " USD");
        try {
            telegramBot.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Ошибка возникла при отсылке уведомления", e);
        }
    }
}