package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.model.Subscriber;
import com.skillbox.cryptobot.model.SubscriberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Обработка команды начала работы с ботом
 */
@Service
@AllArgsConstructor
@Slf4j
public class StartCommand implements IBotCommand {
    @Autowired
    private final SubscriberRepository subscriberRepository;

    @Override
    public String getCommandIdentifier() {
        return "start";
    }

    @Override
    public String getDescription() {
        return "Запускает бота";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        SendMessage answer = new SendMessage();
        answer.setChatId(message.getChatId());

        answer.setText("""
                Привет! Данный бот помогает отслеживать стоимость биткоина.
                Поддерживаемые команды:
                /subscribe [число] - подписаться на стоимость биткоина в USD
                /get_price - получить стоимость биткоина
                /get_subscription - получить текущую подписку
                /unsubscribe - отменить подписку на стоимость
                """);

        try {
            absSender.execute(answer);
            // Добавление новой записи о пользователе в таблицу Subscribers
            Long telegramId = message.getChatId();
            if (!subscriberRepository.existsByTelegramId(telegramId)) {
                // Запись о пользователе не существует, создаем новую запись
                Subscriber newSubscriber = new Subscriber();
                newSubscriber.setTelegramId(telegramId);
                newSubscriber.setSubscriptionPrice(null); // Установка цены подписки в null
                subscriberRepository.save(newSubscriber);
            } else {
                log.info("Запись о пользователе уже существует в базе данных");
            }
        } catch (TelegramApiException e) {
            log.error("Error occurred in /start command", e);
        }
    }
}