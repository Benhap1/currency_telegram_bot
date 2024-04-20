package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.model.Subscriber;
import com.skillbox.cryptobot.model.SubscriberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class UnsubscribeCommand implements IBotCommand {

    private final SubscriberRepository subscriberRepository;

    @Override
    public String getCommandIdentifier() {
        return "unsubscribe";
    }

    @Override
    public String getDescription() {
        return "Отменяет подписку пользователя";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        SendMessage answer = new SendMessage();
        answer.setChatId(message.getChatId());

        Long telegramId = message.getChatId();
        Optional<Subscriber> subscriberOptional = subscriberRepository.findByTelegramId(telegramId);

        if (subscriberOptional.isPresent()) {
            subscriberRepository.delete(subscriberOptional.get());
            answer.setText("Подписка отменена.");
        } else {
            answer.setText("Активные подписки отсутствуют.");
        }

        try {
            absSender.execute(answer);
        } catch (Exception e) {
            log.error("Ошибка возникла в команде /unsubscribe", e);
        }
    }
}
