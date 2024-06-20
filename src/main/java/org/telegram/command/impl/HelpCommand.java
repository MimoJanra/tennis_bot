package org.telegram.command.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.bot.BotService;
import org.telegram.bot.UpdateUtil;
import org.telegram.command.Command;
import org.telegram.command.CommandName;
import org.telegram.telegrambots.meta.api.objects.Update;

@RequiredArgsConstructor
@Component
public class HelpCommand implements Command {

    private static final String HELP_MESSAGE = "Список доступных команд:\n" +
            "/start - Начало работы\n" +
            "/help - Список команд\n" +
            "/new_booking - Записаться на тренировку";

    private final BotService botService;

    @Override
    public boolean execute(Update update, boolean isBeginning) {
        long userId = UpdateUtil.getUserId(update);
        botService.sendText(userId, HELP_MESSAGE);
        return true;
    }

    @Override
    public CommandName getName() {
        return CommandName.HELP;
    }
}
