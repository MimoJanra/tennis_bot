package org.telegram.command.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.bot.BotService;
import org.telegram.bot.UpdateUtil;
import org.telegram.command.Command;
import org.telegram.command.CommandContainer;
import org.telegram.command.CommandName;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class StartCommand implements Command {

    private static final String START_MESSAGE = "Добро пожаловать! Используйте /help для получения списка доступных команд.";

    private final BotService botService;
    private final CommandContainer commandContainer;

    @Value("${bot.admin-usernames}")
    private List<String> adminUsernames;

    @Override
    public boolean execute(Update update, boolean isBeginning) {
        long userId = UpdateUtil.getUserId(update);
        String username = update.getMessage().getFrom().getUserName();
        boolean isAdmin = adminUsernames.contains(username);

        String chatId = update.getMessage().getChatId().toString();
        commandContainer.clearActiveCommand(chatId);

        botService.sendMarkup(userId, START_MESSAGE, getStartKeyboard(isAdmin));
        return true;
    }

    @Override
    public CommandName getName() {
        return CommandName.START;
    }

    private ReplyKeyboardMarkup getStartKeyboard(boolean isAdmin) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        List<String> userCommands = new ArrayList<>();
        List<String> adminCommands = new ArrayList<>();

        for (CommandName commandName : CommandName.values()) {
            if (!commandName.equals(CommandName.START) && !commandName.equals(CommandName.HELP)) {
                if (commandName.isAdmin()) {
                    adminCommands.add(commandName.getText());
                } else {
                    userCommands.add(commandName.getText());
                }
            }
        }

        addCommandsToKeyboard(keyboard, userCommands);

        if (isAdmin) {
            addCommandsToKeyboard(keyboard, adminCommands);
        }

        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        return replyKeyboardMarkup;
    }

    private void addCommandsToKeyboard(List<KeyboardRow> keyboard, List<String> commands) {
        for (int i = 0; i < commands.size(); i += 2) {
            KeyboardRow row = new KeyboardRow();
            row.add(new KeyboardButton(commands.get(i)));
            if (i + 1 < commands.size()) {
                row.add(new KeyboardButton(commands.get(i + 1)));
            }
            keyboard.add(row);
        }
    }
}
