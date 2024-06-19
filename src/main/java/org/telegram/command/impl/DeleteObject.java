package org.telegram.command.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.bot.BotService;
import org.telegram.bot.UpdateUtil;
import org.telegram.command.Command;
import org.telegram.command.CommandName;
import org.telegram.models.TrainingObject;
import org.telegram.service.TrainingObjectService;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class DeleteObject implements Command {

    private static final String DELETE_PROMPT = "Выберите тренировку для удаления:";
    private static final String CONFIRMATION_MESSAGE = "Тренировка успешно удалена.";
    private static final String ERROR_MESSAGE = "Ошибка при удалении тренировки.";

    private final BotService botService;
    private final TrainingObjectService trainingObjectService;

    @Override
    public boolean execute(Update update, boolean isBeginning) {
        long userId = UpdateUtil.getUserId(update);
        if (isBeginning) {
            List<TrainingObject> trainings = trainingObjectService.findAll();
            sendTrainings(userId, trainings);
            return false;
        } else {
            String input = UpdateUtil.getInput(update);
            if (input.startsWith("confirm_delete_")) {
                try {
                    long trainingId = Long.parseLong(input.replace("confirm_delete_", ""));
                    trainingObjectService.deleteById(trainingId);
                    botService.sendText(userId, CONFIRMATION_MESSAGE);
                    return true;
                } catch (Exception e) {
                    botService.sendText(userId, ERROR_MESSAGE);
                    return false;
                }
            } else {
                long trainingId = Long.parseLong(input);
                sendDeleteConfirmation(userId, trainingId);
                return false;
            }
        }
    }

    @Override
    public CommandName getName() {
        return CommandName.DELETE_OBJECT;
    }

    private void sendTrainings(long userId, List<TrainingObject> trainings) {
        for (int i = 0; i < trainings.size(); i += 3) {
            List<TrainingObject> sublist = trainings.subList(i, Math.min(i + 3, trainings.size()));
            String message = sublist.stream()
                    .map(training -> training.getId() + ": " + training.getName())
                    .collect(Collectors.joining("\n"));
            botService.sendWithInlineKeyboard(userId, DELETE_PROMPT + "\n" + message, createInlineKeyboard(sublist));
        }
    }

    private InlineKeyboardMarkup createInlineKeyboard(List<TrainingObject> trainings) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (TrainingObject training : trainings) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("Удалить " + training.getName());
            button.setCallbackData(String.valueOf(training.getId()));
            row.add(button);
            keyboard.add(row);
        }
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    private void sendDeleteConfirmation(long userId, long trainingId) {
        Optional<TrainingObject> trainingOpt = trainingObjectService.findById(trainingId);
        if (trainingOpt.isPresent()) {
            TrainingObject training = trainingOpt.get();
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton confirmButton = new InlineKeyboardButton();
            confirmButton.setText("Подтвердить удаление");
            confirmButton.setCallbackData("confirm_delete_" + trainingId);

            InlineKeyboardButton cancelButton = new InlineKeyboardButton();
            cancelButton.setText("Отмена");
            cancelButton.setCallbackData("cancel_delete");

            row.add(confirmButton);
            row.add(cancelButton);
            keyboard.add(row);

            inlineKeyboardMarkup.setKeyboard(keyboard);

            botService.sendWithInlineKeyboard(userId, "Вы уверены, что хотите удалить " + training.getName() + "?", inlineKeyboardMarkup);
        } else {
            botService.sendText(userId, ERROR_MESSAGE);
        }
    }
}
