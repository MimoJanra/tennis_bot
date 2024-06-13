package org.telegram.bot;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.command.Command;
import org.telegram.command.CommandContainer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;

@Component
public class Bot extends TelegramLongPollingBot {

    @Value("${bot.username}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    private final CommandContainer commandContainer;
    private final Map<Long, Command> currentUserCommands = new HashMap<>();

    private static Bot instance;

    public Bot(CommandContainer commandContainer) {
        if (instance != null) {
            throw new IllegalStateException("Bot instance already exists!");
        }
        this.commandContainer = commandContainer;
        instance = this;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Long userId = UpdateUtil.getUserId(update);
        String input = UpdateUtil.getInput(update);

        System.out.println("Received update from user: " + userId + " with input: " + input);

        boolean isFinished = false;
        if (commandContainer.hasCommand(input)) {   // user starts new command
            Command command = commandContainer.getCommand(input);
            currentUserCommands.put(userId, command);
            isFinished = command.execute(update, true);
        } else if (currentUserCommands.containsKey(userId)) {  // user continues some command
            isFinished = currentUserCommands.get(userId).execute(update, false);
        }

        if (isFinished) {
            currentUserCommands.remove(userId);
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
