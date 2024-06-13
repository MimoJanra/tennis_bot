package org.telegram.command.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.bot.BotService;
import org.telegram.bot.UpdateUtil;
import org.telegram.command.Command;
import org.telegram.command.CommandName;
import org.telegram.telegrambots.meta.api.objects.Update;

@RequiredArgsConstructor
@Component
@Scope("prototype")
public class StartCommand implements Command {

    private static final String START_MESSAGE = "Добро пожаловать! Используйте /help для получения списка доступных команд.";

    private final BotService botService;

    @Override
    public boolean execute(Update update, boolean isBeginning) {
        long userId = UpdateUtil.getUserId(update);
        botService.sendText(userId, START_MESSAGE);
        return true;
    }

    @Override
    public CommandName getName() {
        return CommandName.START;
    }
}