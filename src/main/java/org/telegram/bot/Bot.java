package org.telegram.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.command.Command;
import org.telegram.command.CommandContainer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class Bot extends TelegramLongPollingBot {

    @Value("${bot.username}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    private final CommandContainer commandContainer;

    public Bot(CommandContainer commandContainer) {
        this.commandContainer = commandContainer;
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
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText().trim();
            String chatId = update.getMessage().getChatId().toString();
            System.out.println("Message received: " + messageText);

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
            String callbackData = update.getCallbackQuery().getData();
            String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
            System.out.println("Callback data received: " + callbackData);

            Command command = commandContainer.getCallbackCommand(callbackData);
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
            e.printStackTrace();
        }
    }
}
