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
public class AddSubscription implements Command {

    private static final String ADD_MESSAGE = "Введите данные для добавления абонемента:";
    private static final String CONFIRMATION_MESSAGE = "Абонемент успешно добавлен.";
    private static final String ERROR_MESSAGE = "Ошибка при добавлении абонемента.";

    private final BotService botService;
    private final SubscriptionService subscriptionService;

    @Override
    public boolean execute(Update update, boolean isBeginning) {
        long userId = UpdateUtil.getUserId(update);
        if (isBeginning) {
            botService.sendText(userId, ADD_MESSAGE);
            return false;
        } else {
            String input = UpdateUtil.getInput(update);
            try {
                subscriptionService.addSubscription(input);
                botService.sendText(userId, CONFIRMATION_MESSAGE);
                return true;
            } catch (Exception e) {
                botService.sendText(userId, ERROR_MESSAGE);
                return false;
            }
        }
    }

    @Override
    public CommandName getName() {
        return CommandName.ADD_SUBSCRIPTION;
    }
}
