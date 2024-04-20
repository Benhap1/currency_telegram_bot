package com.skillbox.cryptobot;

import com.skillbox.cryptobot.bot.command.SubscribeCommand;
import com.skillbox.cryptobot.model.Subscriber;
import com.skillbox.cryptobot.model.SubscriberRepository;
import com.skillbox.cryptobot.service.CryptoCurrencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SubscribeCommandTest {

    @Mock
    private CryptoCurrencyService cryptoCurrencyService;

    @Mock
    private SubscriberRepository subscriberRepository;

    @Mock
    private AbsSender absSender;

    @InjectMocks
    private SubscribeCommand subscribeCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessMessage_WithValidArguments() throws IOException, TelegramApiException {
        // Arrange
        Message message = mock(Message.class); // Mocking Message object
        when(message.getChatId()).thenReturn(123456L); // Stubbing getChatId() method
        String[] arguments = {"10000"};
        BigDecimal currentPrice = new BigDecimal("50000");

        when(cryptoCurrencyService.getBitcoinPrice()).thenReturn(currentPrice);
        when(subscriberRepository.findByTelegramId(any())).thenReturn(Optional.empty());

        // Act
        subscribeCommand.processMessage(absSender, message, arguments);

        // Assert
        verify(absSender).execute(any(SendMessage.class));
        verify(subscriberRepository).save(any(Subscriber.class));
    }

    @Test
    void testProcessMessage_WithInvalidArguments() throws TelegramApiException {
        // Arrange
        Message message = mock(Message.class); // Mocking Message object
        when(message.getChatId()).thenReturn(123456L); // Stubbing getChatId() method
        String[] arguments = {"abc"};

        // Act
        subscribeCommand.processMessage(absSender, message, arguments);

        // Assert
        verify(absSender).execute(any(SendMessage.class));
        verify(subscriberRepository, never()).save(any(Subscriber.class));
    }
}