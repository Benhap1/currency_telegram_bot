package com.skillbox.cryptobot.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class CryptoBot extends TelegramLongPollingCommandBot {

    private final String botUsername;

    public CryptoBot(@Value("${telegram.bot.token}") String botToken,
                     @Value("${telegram.bot.username}") String botUsername,
                     List<IBotCommand> commandList) {
        super(botToken);
        this.botUsername = botUsername;
        commandList.forEach(this::register);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    private List<String> supportedCommands() {
        return Arrays.asList(
                "/subscribe",
                "/get_price",
                "/get_subscription",
                "/unsubscribe"
        );
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            Long chatId = message.getChatId();

            if (message.hasText()) {
                processTextMessage(message);
            } else {
                sendUnsupportedMessageTypeMessage(chatId);
            }
        }
    }

    private void processTextMessage(Message message) {
        String text = message.getText();
        Long chatId = message.getChatId();

        if (!text.startsWith("/")) {
            sendUnknownCommandMessage(chatId);
        } else {
            String command = text.split(" ")[0];
            if (!supportedCommands().contains(command)) {
                sendUnknownCommandMessage(chatId);
            }
        }
    }

    private void sendUnknownCommandMessage(Long chatId) {
        String unknownCommandMessage = """
                Такой команды нет! Вот список поддерживаемых команд:
                /subscribe [число] - подписаться на стоимость биткоина в USD
                /get_price - получить стоимость биткоина
                /get_subscription - получить текущую подписку
                /unsubscribe - отменить подписку на стоимость""";
        sendMessage(chatId, unknownCommandMessage);
    }

    private void sendUnsupportedMessageTypeMessage(Long chatId) {
        String unsupportedMessageTypeMessage = "Бот не поддерживает отправку изображений, " +
                "видео или файлов.";
        sendMessage(chatId, unsupportedMessageTypeMessage);
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            execute(message);
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения", e);
        }
    }
}
