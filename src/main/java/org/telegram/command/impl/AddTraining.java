package org.telegram.command.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.bot.BotService;
import org.telegram.bot.UpdateUtil;
import org.telegram.command.Command;
import org.telegram.command.CommandName;
import org.telegram.models.BookingObject;
import org.telegram.service.BookingObjectService;
import org.telegram.service.UserService;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
@Scope("prototype")
public class AddTraining implements Command {

    private enum Step {
        BEGIN,
        ENTER_NAME,
        ENTER_DESCRIPTION,
        CONFIRMATION
    }

    private static final String ENTER_NAME = "Введите название тренировки:";
    private static final String ENTER_DESCRIPTION = "Введите описание тренировки:";
    private static final String CONFIRMATION_TEXT = "Тренировка добавлена:\nНазвание: %s\nОписание: %s";

    private final BotService botService;
    private final BookingObjectService bookingObjectService;
    private final UserService userService;

    private final Map<Long, Step> usersSteps = new HashMap<>();
    private BookingObject training;

    @Override
    public boolean execute(Update update, boolean isBeginning) {
        Long userId = UpdateUtil.getUserId(update);
        String input = UpdateUtil.getInput(update);

        if (!usersSteps.containsKey(userId) || isBeginning) {
            usersSteps.put(userId, Step.BEGIN);
        }

        switch (usersSteps.get(userId)) {
            case BEGIN -> begin(update);
            case ENTER_NAME -> enterName(userId, input);
            case ENTER_DESCRIPTION -> enterDescription(userId, input);
            case CONFIRMATION -> confirmation(userId);
        }

        return false;
    }

    @Override
    public CommandName getName() {
        return CommandName.ADD_TRAINING;
    }

    private void begin(Update update) {
        long userId = UpdateUtil.getUserId(update);
        training = new BookingObject();

        userService.findById(userId).ifPresent(training::setUser);
        botService.sendText(userId, ENTER_NAME);
        usersSteps.put(userId, Step.ENTER_NAME);
    }

    private void enterName(Long userId, String input) {
        training.setName(input);
        botService.sendText(userId, ENTER_DESCRIPTION);
        usersSteps.put(userId, Step.ENTER_DESCRIPTION);
    }

    private void enterDescription(Long userId, String input) {
        training.setDescription(input);
        bookingObjectService.save(training);
        usersSteps.put(userId, Step.CONFIRMATION);
        confirmation(userId);
    }

    private void confirmation(Long userId) {
        String confirmationText = String.format(CONFIRMATION_TEXT, training.getName(), training.getDescription());
        botService.sendText(userId, confirmationText);
        usersSteps.remove(userId);
    }
}
