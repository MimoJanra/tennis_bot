package org.telegram.command.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.bot.BotService;
import org.telegram.bot.Button;
import org.telegram.bot.UpdateUtil;
import org.telegram.command.Command;
import org.telegram.command.CommandName;
import org.telegram.models.TrainingObject;
import org.telegram.service.TrainingObjectService;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;

@RequiredArgsConstructor
@Component
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
    private final TrainingObjectService trainingObjectService;

    private final Map<Long, Step> usersSteps = new HashMap<>();
    private boolean isFinished;
    private TrainingObject trainingObject;

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
        try {
            Long objectId = Long.parseLong(input);
            Optional<TrainingObject> objectOpt = trainingObjectService.findById(objectId);
            if (objectOpt.isPresent()) {
                trainingObject = objectOpt.get();
                botService.sendWithKeyboard(userId, SELECT_ACTION, getActionsButtons());
                usersSteps.put(userId, Step.SELECT_ACTION);
            } else {
                botService.sendText(userId, "Объект не найден.");
            }
        } catch (NumberFormatException e) {
            botService.sendText(userId, "Неверный формат ID объекта.");
        }
    }

    private void selectAction(long userId, String input) {
        try {
            Step step = Step.valueOf(input.toUpperCase());
            switch (step) {
                case CHANGE_NAME -> botService.sendText(userId, ENTER_NEW_NAME);
                case CHANGE_DESCRIPTION -> botService.sendText(userId, ENTER_NEW_DESCRIPTION);
                case DELETE -> botService.sendWithKeyboard(userId,
                        String.format(CONFIRM_DELETING, trainingObject.getName()), getConfirmButtons());
            }
            usersSteps.put(userId, step);
        } catch (IllegalArgumentException ignored) {}
    }

    private void changeName(long userId, String input) {
        trainingObject.setName(input);
        confirmChanges(userId);
    }

    private void changeDescription(long userId, String input) {
        trainingObject.setDescription(input);
        confirmChanges(userId);
    }

    private void delete(long userId) {
        trainingObjectService.delete(trainingObject);
        botService.sendText(userId, DONE);
        usersSteps.put(userId, Step.BEGIN);
        isFinished = true;
    }

    private void confirmChanges(long userId) {
        trainingObjectService.save(trainingObject);
        botService.sendText(userId, DONE);
        usersSteps.put(userId, Step.BEGIN);
        isFinished = true;
    }

    private List<Button> getObjectsButtons() {
        List<Button> objectsButtons = new ArrayList<>();
        List<TrainingObject> objectsList = trainingObjectService.findAll();
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
                new Button("confirm_delete", DELETE),
                new Button(CANCEL, CANCEL)
        );
    }
}
