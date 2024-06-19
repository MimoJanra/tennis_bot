package org.telegram.command.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.bot.BotService;
import org.telegram.bot.UpdateUtil;
import org.telegram.command.Command;
import org.telegram.command.CommandContainer;
import org.telegram.command.CommandName;
import org.telegram.models.TrainingObject;
import org.telegram.service.TrainingObjectService;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class AddTraining implements Command {
    // Промпты для ввода
    private static final String SELECT_DATE_PROMPT = "Выберите дату для тренировки:";
    private static final String SELECT_HOUR_PROMPT = "Выберите час для тренировки:";
    private static final String SELECT_MINUTE_PROMPT = "Выберите минуты для тренировки:";
    private static final String SELECT_DURATION_PROMPT = "Выберите продолжительность тренировки:";
    private static final String SELECT_LOCATION_PROMPT = "Введите место для тренировки:";
    private static final String ENTER_NAME_PROMPT = "Введите название тренировки:";
    private static final String ENTER_DESCRIPTION_PROMPT = "Введите описание тренировки:";
    private static final String SELECT_PARTICIPANTS_PROMPT = "Выберите количество участников (от 2 до 6):";
    private static final String ENTER_COST_PROMPT = "Введите стоимость тренировки:";
    private static final String RECURRING_PROMPT = "Тренировка будет повторяться раз в неделю?";
    private static final String CONFIRMATION_MESSAGE = "Тренировка успешно добавлена!";
    private static final String ERROR_MESSAGE = "Ошибка при добавлении тренировки.";
    private static final String CANCEL_MESSAGE = "Операция отменена.";

    private final BotService botService;
    private final TrainingObjectService trainingObjectService;
    private final CommandContainer commandContainer;

    private enum Step {
        SELECT_DATE,
        SELECT_HOUR,
        SELECT_MINUTE,
        SELECT_DURATION,
        SELECT_LOCATION,
        ENTER_NAME,
        ENTER_DESCRIPTION,
        SELECT_PARTICIPANTS,
        ENTER_COST,
        SELECT_RECURRING,
        CONFIRM
    }

    private Step currentStep = Step.SELECT_DATE;
    private LocalDate selectedDate;
    private String selectedHour;
    private String selectedMinute;
    private int selectedDuration;
    private String selectedLocation;
    private String name;
    private String description;
    private int participants;
    private double cost;
    private boolean recurring;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public boolean execute(Update update, boolean isBeginning) {
        long userId = UpdateUtil.getUserId(update);
        String input = UpdateUtil.getInput(update);

        if (input.equalsIgnoreCase("Отмена") || input.equalsIgnoreCase("CANCEL")) {
            cancelOperation(userId);
            return true;
        }

        try {
            if (update.hasCallbackQuery()) {
                input = update.getCallbackQuery().getData();
            }

            switch (currentStep) {
                case SELECT_DATE:
                    if (isBeginning) {
                        sendDateSelectionPrompt(userId);
                    } else {
                        selectedDate = LocalDate.parse(input, dateFormatter);
                        currentStep = Step.SELECT_HOUR;
                        sendHourSelectionPrompt(userId);
                    }
                    break;
                case SELECT_HOUR:
                    selectedHour = input;
                    currentStep = Step.SELECT_MINUTE;
                    sendMinuteSelectionPrompt(userId);
                    break;
                case SELECT_MINUTE:
                    selectedMinute = input;
                    currentStep = Step.SELECT_DURATION;
                    sendDurationSelectionPrompt(userId);
                    break;
                case SELECT_DURATION:
                    selectedDuration = Integer.parseInt(input.trim());
                    currentStep = Step.SELECT_LOCATION;
                    sendLocationSelectionPrompt(userId);
                    break;
                case SELECT_LOCATION:
                    selectedLocation = input;
                    currentStep = Step.ENTER_NAME;
                    sendNamePrompt(userId);
                    break;
                case ENTER_NAME:
                    name = input;
                    currentStep = Step.ENTER_DESCRIPTION;
                    sendDescriptionPrompt(userId);
                    break;
                case ENTER_DESCRIPTION:
                    description = input;
                    currentStep = Step.SELECT_PARTICIPANTS;
                    sendParticipantsPrompt(userId);
                    break;
                case SELECT_PARTICIPANTS:
                    participants = Integer.parseInt(input.trim());
                    currentStep = Step.ENTER_COST;
                    sendCostPrompt(userId);
                    break;
                case ENTER_COST:
                    cost = Double.parseDouble(input.trim());
                    currentStep = Step.SELECT_RECURRING;
                    sendRecurringPrompt(userId);
                    break;
                case SELECT_RECURRING:
                    recurring = input.equalsIgnoreCase("yes");
                    currentStep = Step.CONFIRM;
                    saveTraining(userId);
                    break;
                case CONFIRM:
                    botService.sendText(userId, CONFIRMATION_MESSAGE);
                    return true;
            }
        } catch (NumberFormatException e) {
            botService.sendText(userId, "Неверный формат числа. Пожалуйста, введите корректное число.");
        } catch (Exception e) {
            botService.sendText(userId, ERROR_MESSAGE);
        }
        return false;
    }

    @Override
    public CommandName getName() {
        return CommandName.ADD_TRAINING;
    }

    private void sendDateSelectionPrompt(long userId) {
        InlineKeyboardMarkup keyboard = createDateSelectionKeyboard();
        botService.sendWithInlineKeyboard(userId, SELECT_DATE_PROMPT, keyboard);
    }

    private InlineKeyboardMarkup createDateSelectionKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> totalList = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalDate day = today.minusDays(today.getDayOfWeek().getValue() - 1);

        for (int calendarRow = 0; calendarRow < 4; calendarRow++) {
            List<InlineKeyboardButton> keyboardButtonRow = new ArrayList<>();
            for (int calendarColumn = 0; calendarColumn < 7; calendarColumn++) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(day.isBefore(today) ? "X" : String.valueOf(day.getDayOfMonth()));
                button.setCallbackData(day.format(dateFormatter));
                keyboardButtonRow.add(button);
                day = day.plusDays(1);
            }
            totalList.add(keyboardButtonRow);
        }
        addCancelButton(totalList);
        inlineKeyboardMarkup.setKeyboard(totalList);
        return inlineKeyboardMarkup;
    }

    private void sendHourSelectionPrompt(long userId) {
        InlineKeyboardMarkup keyboard = createHourSelectionKeyboard();
        botService.sendWithInlineKeyboard(userId, SELECT_HOUR_PROMPT, keyboard);
    }

    private InlineKeyboardMarkup createHourSelectionKeyboard() {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        int startHour = 8;
        int endHour = 21;

        List<InlineKeyboardButton> row = new ArrayList<>();
        for (int hour = startHour; hour <= endHour; hour++) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(String.format("%02d", hour));
            button.setCallbackData(String.format("%02d", hour));

            row.add(button);

            if (row.size() == 7 || hour == endHour) {
                rows.add(new ArrayList<>(row));
                row.clear();
            }
        }

        addCancelButton(rows);
        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private void sendMinuteSelectionPrompt(long userId) {
        InlineKeyboardMarkup keyboard = createMinuteSelectionKeyboard();
        botService.sendWithInlineKeyboard(userId, SELECT_MINUTE_PROMPT, keyboard);
    }

    private InlineKeyboardMarkup createMinuteSelectionKeyboard() {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (int minute = 0; minute < 60; minute += 30) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(String.format("%02d", minute));
            button.setCallbackData(String.format("%02d", minute));
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            rows.add(row);
        }

        addCancelButton(rows);
        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private void sendDurationSelectionPrompt(long userId) {
        InlineKeyboardMarkup keyboard = createDurationSelectionKeyboard();
        botService.sendWithInlineKeyboard(userId, SELECT_DURATION_PROMPT, keyboard);
    }

    private InlineKeyboardMarkup createDurationSelectionKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        int[] durations = {30, 60, 90, 120};
        for (int duration : durations) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(duration + " минут");
            button.setCallbackData(String.valueOf(duration));
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            rows.add(row);
        }

        addCancelButton(rows);
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    private void sendLocationSelectionPrompt(long userId) {
        InlineKeyboardMarkup keyboard = createCancelKeyboard(userId);
        botService.sendText(userId, SELECT_LOCATION_PROMPT);
        botService.sendWithInlineKeyboard(userId, "Для отмены нажмите кнопку 'Отмена'.", keyboard);
    }

    private void sendNamePrompt(long userId) {
        InlineKeyboardMarkup keyboard = createCancelKeyboard(userId);
        botService.sendText(userId, ENTER_NAME_PROMPT);
        botService.sendWithInlineKeyboard(userId, "Для отмены нажмите кнопку 'Отмена'.", keyboard);
    }

    private void sendDescriptionPrompt(long userId) {
        InlineKeyboardMarkup keyboard = createCancelKeyboard(userId);
        botService.sendText(userId, ENTER_DESCRIPTION_PROMPT);
        botService.sendWithInlineKeyboard(userId, "Для отмены нажмите кнопку 'Отмена'.", keyboard);
    }

    private void sendParticipantsPrompt(long userId) {
        InlineKeyboardMarkup keyboard = createParticipantsSelectionKeyboard();
        botService.sendWithInlineKeyboard(userId, SELECT_PARTICIPANTS_PROMPT, keyboard);
    }

    private InlineKeyboardMarkup createParticipantsSelectionKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (int i = 2; i <= 6; i++) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(String.valueOf(i));
            button.setCallbackData(String.valueOf(i));
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            rows.add(row);
        }

        addCancelButton(rows);
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    private void sendCostPrompt(long userId) {
        InlineKeyboardMarkup keyboard = createCancelKeyboard(userId);
        botService.sendText(userId, ENTER_COST_PROMPT);
        botService.sendWithInlineKeyboard(userId, "Для отмены нажмите кнопку 'Отмена'.", keyboard);
    }

    private void sendRecurringPrompt(long userId) {
        InlineKeyboardMarkup keyboard = createRecurringSelectionKeyboard();
        botService.sendWithInlineKeyboard(userId, RECURRING_PROMPT, keyboard);
    }

    private InlineKeyboardMarkup createRecurringSelectionKeyboard() {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton yesButton = new InlineKeyboardButton();
        yesButton.setText("Да");
        yesButton.setCallbackData("yes");

        InlineKeyboardButton noButton = new InlineKeyboardButton();
        noButton.setText("Нет");
        noButton.setCallbackData("no");

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(yesButton);
        row.add(noButton);

        rows.add(row);
        addCancelButton(rows);
        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private void saveTraining(long userId) {
        TrainingObject trainingObject = new TrainingObject();
        trainingObject.setDate(selectedDate);
        trainingObject.setTrainingHour(selectedHour);
        trainingObject.setTrainingMinute(selectedMinute);
        trainingObject.setDuration(selectedDuration);
        trainingObject.setLocation(selectedLocation);
        trainingObject.setName(name);
        trainingObject.setDescription(description);
        trainingObject.setRecurring(recurring);
        trainingObject.setParticipants(participants);
        trainingObject.setCost(cost);

        trainingObjectService.save(trainingObject);
        botService.sendText(userId, CONFIRMATION_MESSAGE);
        commandContainer.clearActiveCommand(String.valueOf(userId));
    }

    private void cancelOperation(long userId) {
        botService.sendText(userId, CANCEL_MESSAGE);
        commandContainer.clearActiveCommand(String.valueOf(userId));
    }

    private void addCancelButton(List<List<InlineKeyboardButton>> rows) {
        InlineKeyboardButton cancelButton = new InlineKeyboardButton();
        cancelButton.setText("Отмена");
        cancelButton.setCallbackData("CANCEL");

        List<InlineKeyboardButton> cancelRow = new ArrayList<>();
        cancelRow.add(cancelButton);
        rows.add(cancelRow);
    }

    private InlineKeyboardMarkup createCancelKeyboard(long userId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        addCancelButton(rows);
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }
}
