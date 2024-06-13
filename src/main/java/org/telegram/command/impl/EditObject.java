package org.telegram.command.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.bot.BotService;
import org.telegram.bot.Button;
import org.telegram.bot.UpdateUtil;
import org.telegram.command.Command;
import org.telegram.command.CommandName;
import org.telegram.models.BookingObject;
import org.telegram.service.BookingObjectService;

import java.util.*;

@RequiredArgsConstructor
@Component
@Scope("prototype")
public class EditObject implements Command {

    private enum Step {
        BEGIN,
        SELECT_OBJECT,
        SELECT_ACTION,
        CHANGE_NAME,
        CHANGE_DESCRIPTION,
        DELETE
    }

    private static final String SELECT_OBJECT = "Выберите тренировку для изменения";
    private static final String CHANGE_NAME = "Изменить название";
    private static final String CHANGE_DESCRIPTION = "Изменить описание";
    private static final String DELETE = "Удалить";
    private static final String CANCEL = "Отмена";
    private static final String SELECT_ACTION = "Выберите действие";
    private static final String ENTER_NEW_NAME = "Введите новое название";
    private static final String ENTER_NEW_DESCRIPTION = "Введите новое описание";
    private static final String CONFIRM_DELETING = "Вы уверены что хотите удалить %s?";
    private static final String DONE = "Готово";

    private final BotService botService;
    private final BookingObjectService bookingObjectService;

    private final Map<Long, Step> usersSteps = new HashMap<>();
    private boolean isFinished;
    private BookingObject bookingObject;

    @Override
    public boolean execute(Update update, boolean isBeginning) {
        long userId = UpdateUtil.getUserId(update);
        String input = UpdateUtil.getInput(update);

        if (!usersSteps.containsKey(userId) || isBeginning) {
            usersSteps.put(userId, Step.BEGIN);
        }

        switch (usersSteps.get(userId)) {
            case BEGIN -> begin(userId);
            case SELECT_OBJECT -> selectObject(userId, input);
            case SELECT_ACTION -> selectAction(userId, input);
            case CHANGE_NAME -> changeName(userId, input);
            case CHANGE_DESCRIPTION -> changeDescription(userId, input);
            case DELETE -> delete(userId);
        }

        return isFinished;
    }

    @Override
    public CommandName getName() {
        return CommandName.EDIT_OBJECT;
    }

    private void begin(long userId) {
        botService.sendWithKeyboard(userId, SELECT_OBJECT, getObjectsButtons());
        usersSteps.put(userId, Step.SELECT_OBJECT);
    }

    private void selectObject(long userId, String input) {
        Optional<BookingObject> objectOpt = Optional.ofNullable(bookingObjectService.findById(input));
        if (objectOpt.isPresent()) {
            bookingObject = objectOpt.get();
            botService.sendWithKeyboard(userId, SELECT_ACTION, getActionsButtons());
            usersSteps.put(userId, Step.SELECT_ACTION);
        }
    }

    private void selectAction(long userId, String input) {
        try {
            Step step = Step.valueOf(input.toUpperCase());
            switch (step) {
                case CHANGE_NAME -> botService.sendText(userId, ENTER_NEW_NAME);
                case CHANGE_DESCRIPTION -> botService.sendText(userId, ENTER_NEW_DESCRIPTION);
                case DELETE -> botService.sendWithKeyboard(userId,
                        String.format(CONFIRM_DELETING, bookingObject.getName()), getConfirmButtons());
            }

            usersSteps.put(userId, step);
        } catch (IllegalArgumentException ignored) {}
    }

    private void changeName(long userId, String input) {
        bookingObject.setName(input);
        confirmChanges(userId);
    }

    private void changeDescription(long userId, String input) {
        bookingObject.setDescription(input);
        confirmChanges(userId);
    }

    private void delete(long userId) {
        bookingObjectService.delete(bookingObject);
        botService.sendText(userId, DONE);
        usersSteps.put(userId, Step.BEGIN);
        isFinished = true;
    }

    private void confirmChanges(long userId) {
        bookingObjectService.save(bookingObject);
        botService.sendText(userId, DONE);
        usersSteps.put(userId, Step.BEGIN);
        isFinished = true;
    }

    private List<Button> getObjectsButtons() {
        List<Button> objectsButtons = new ArrayList<>();
        List<BookingObject> objectsList = bookingObjectService.findAll();
        objectsList.forEach(object -> objectsButtons.add(new Button(String.valueOf(object.getId()), object.getName())));
        return objectsButtons;
    }

    private List<Button> getActionsButtons() {
        return List.of(
                new Button(Step.CHANGE_NAME.toString(), CHANGE_NAME),
                new Button(Step.CHANGE_DESCRIPTION.toString(), CHANGE_DESCRIPTION),
                new Button(Step.DELETE.toString(), DELETE)
        );
    }

    private List<Button> getConfirmButtons() {
        return List.of(
                new Button(String.valueOf(bookingObject.getId()), DELETE),
                new Button(CANCEL, CANCEL)
        );
    }

}
