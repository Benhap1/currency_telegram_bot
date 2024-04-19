package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.model.Subscriber;
import com.skillbox.cryptobot.model.SubscriberRepository;
import com.skillbox.cryptobot.service.CryptoCurrencyService;
import com.skillbox.cryptobot.utils.TextUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.math.BigDecimal;

@Service
@Slf4j
@AllArgsConstructor
public class SubscribeCommand implements IBotCommand {

    private final CryptoCurrencyService cryptoCurrencyService;
    private final SubscriberRepository subscriberRepository;

    @Override
    public String getCommandIdentifier() {
        return "subscribe";
    }

    @Override
    public String getDescription() {
        return "Подписывает пользователя на стоимость биткоина";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        SendMessage answer = new SendMessage();
        answer.setChatId(message.getChatId());

        if (arguments == null || arguments.length != 1) {
            answer.setText("Некорректное использование команды. Используйте /subscribe <желаемая_цена>");
        } else {
            try {

                BigDecimal desiredPrice = new BigDecimal(arguments[0]);

                // Получаем текущую цену биткоина
                BigDecimal currentPrice = cryptoCurrencyService.getBitcoinPrice();

                // Сохраняем подписку пользователя
                Long telegramId = message.getChatId();
                Subscriber subscriber = subscriberRepository.findByTelegramId(telegramId)
                        .orElse(new Subscriber());
                subscriber.setTelegramId(telegramId);
                subscriber.setSubscriptionPrice(desiredPrice);
                subscriberRepository.save(subscriber);

                // Формируем ответное сообщение
                answer.setText("Текущая цена биткоина " + TextUtil.toString(currentPrice) + " USD\n" +
                        "Новая подписка создана на стоимость " + TextUtil.toString(desiredPrice));
            } catch (NumberFormatException e) {
                answer.setText("Некорректное значение желаемой цены");
            } catch (Exception e) {
                log.error("Ошибка при обработке команды /subscribe", e);
                answer.setText("Произошла ошибка. Пожалуйста, попробуйте еще раз позже.");
            }
        }

        try {
            absSender.execute(answer);
        } catch (Exception e) {
            log.error("Ошибка при отправке ответа на команду /subscribe", e);
        }
    }
}
