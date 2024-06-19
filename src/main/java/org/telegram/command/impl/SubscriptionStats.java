package org.telegram.command.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.bot.BotService;
import org.telegram.bot.UpdateUtil;
import org.telegram.command.Command;
import org.telegram.command.CommandName;
import org.telegram.service.SubscriptionService;
import org.telegram.telegrambots.meta.api.objects.Update;

@RequiredArgsConstructor
@Component
public class SubscriptionStats implements Command {

    private static final String STATS_MESSAGE = "Статистика абонементов:";
    private static final String ERROR_MESSAGE = "Ошибка при получении статистики абонементов.";

    private final BotService botService;
    private final SubscriptionService subscriptionService;

    @Override
    public boolean execute(Update update, boolean isBeginning) {
        long userId = UpdateUtil.getUserId(update);
        try {
            String stats = subscriptionService.getSubscriptionStats();
            botService.sendText(userId, STATS_MESSAGE + "\n" + stats);
            return true;
        } catch (Exception e) {
            botService.sendText(userId, ERROR_MESSAGE);
            return false;
        }
    }

    @Override
    public CommandName getName() {
        return CommandName.SUBSCRIPTION_STATS;
    }
}
