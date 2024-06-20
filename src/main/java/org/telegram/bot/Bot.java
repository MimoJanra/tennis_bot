package org.telegram.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.command.Command;
import org.telegram.command.CommandContainer;
import org.telegram.service.UserService;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class Bot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(Bot.class);
    private final CommandContainer commandContainer;
    private final UserService userService;
    @Value("${bot.username}")
    private String botUsername;
    @Value("${bot.token}")
    private String botToken;

    public Bot(CommandContainer commandContainer, UserService userService) {
        this.commandContainer = commandContainer;
        this.userService = userService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        String chatId = null;
        String userId = null;
        String userName = null;

        if (update.hasMessage() && update.getMessage().hasText()) {
            chatId = update.getMessage().getChatId().toString();
            userId = String.valueOf(update.getMessage().getFrom().getId());
            userName = update.getMessage().getFrom().getUserName();
            logger.info("Message received: {} from chatId: {}, userId: {}, userName: {}", update.getMessage().getText(), chatId, userId, userName);

            userService.createOrUpdate(update);

            String messageText = update.getMessage().getText().trim();

            Command activeCommand = commandContainer.getActiveCommand(chatId);
            if (activeCommand != null) {
                activeCommand.execute(update, false);
            } else if (commandContainer.hasCommand(messageText)) {
                Command command = commandContainer.getCommand(messageText);
                commandContainer.setActiveCommand(chatId, command);
                command.execute(update, true);
            } else {
                sendMessage(chatId, "Неизвестная команда");
            }
        } else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId().toString();
            userId = String.valueOf(update.getCallbackQuery().getFrom().getId());
            userName = update.getCallbackQuery().getFrom().getUserName();
            logger.info("Callback data received: {} from chatId: {}, userId: {}, userName: {}", update.getCallbackQuery().getData(), chatId, userId, userName);

            userService.createOrUpdate(update);

            String callbackData = update.getCallbackQuery().getData();

            Command command = commandContainer.getCallbackCommand(callbackData, chatId);
            if (command != null) {
                command.execute(update, false);
            } else {
                sendMessage(chatId, "Неизвестная команда");
            }
        }
    }

    private void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Failed to send message to chatId {}: {}", chatId, e.getMessage());
        }
    }
}
