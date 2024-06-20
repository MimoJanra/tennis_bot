package org.telegram.command.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.bot.BotService;
import org.telegram.bot.UpdateUtil;
import org.telegram.command.Command;
import org.telegram.command.CommandContainer;
import org.telegram.command.CommandName;
import org.telegram.telegrambots.meta.api.objects.Update;

@RequiredArgsConstructor
@Component
public class CancelCommand implements Command {

    private final BotService botService;
    private final CommandContainer commandContainer;

    @Override
    public boolean execute(Update update, boolean isBeginning) {
        Long userId = UpdateUtil.getUserId(update);
        commandContainer.clearActiveCommand(userId.toString());
        botService.sendText(userId, "Вы вышли из текущей команды.");
        return true;
    }

    @Override
    public CommandName getName() {
        return CommandName.CANCEL;
    }
}
