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
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
@Scope("prototype")
public class AddTraining implements Command {

    private static final String SELECT_DATE_PROMPT = "Выберите дату для тренировки:";
    private static final String SELECT_HOUR_PROMPT = "Выберите час для тренировки:";
    private static final String SELECT_MINUTE_PROMPT = "Выберите минуты для тренировки:";
    private static final String SELECT_DURATION_PROMPT = "Выберите продолжительность тренировки:";
    private static final String SELECT_LOCATION_PROMPT = "Введите место для тренировки:";
    private static final String ENTER_NAME_PROMPT = "Введите название тренировки:";
    private static final String ENTER_DESCRIPTION_PROMPT = "Введите описание тренировки:";
    private static final String RECURRING_PROMPT = "Тренировка будет повторяться раз в неделю?";
    private static final String CONFIRMATION_MESSAGE = "Тренировка успешно добавлена!";
    private static final String ERROR_MESSAGE = "Ошибка при добавлении тренировки.";

    private final BotService botService;
    private final BookingObjectService bookingObjectService;

    private enum Step {
        SELECT_DATE,
        SELECT_HOUR,
        SELECT_MINUTE,
        SELECT_DURATION,
        SELECT_LOCATION,
        ENTER_NAME,
        ENTER_DESCRIPTION,
        SELECT_RECURRING,
        CONFIRM
    }

    private Step currentStep = Step.SELECT_DATE;
    private LocalDate selectedDate;
    private int selectedHour;
    private int selectedMinute;
    private String selectedLocation;
    private String name;
    private String description;
    private boolean recurring;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public boolean execute(Update update, boolean isBeginning) {
        long userId = UpdateUtil.getUserId(update);
        String input = UpdateUtil.getInput(update);

        try {
            if (update.hasCallbackQuery()) {
                input = update.getCallbackQuery().getData();
                System.out.println("Callback data received: " + input);
            } else {
                System.out.println("Text input received: " + input);
            }

            switch (currentStep) {
                case SELECT_DATE:
                    if (isBeginning) {
                        sendDateSelectionPrompt(userId);
                    } else {
                        selectedDate = LocalDate.parse(input, dateFormatter);
                        System.out.println("Selected date: " + selectedDate);
                        currentStep = Step.SELECT_HOUR;
                        sendHourSelectionPrompt(userId);
                    }
                    break;
                case SELECT_HOUR:
                    selectedHour = Integer.parseInt(input.trim());
                    System.out.println("Selected hour: " + selectedHour);
                    currentStep = Step.SELECT_MINUTE;
                    sendMinuteSelectionPrompt(userId);
                    break;
                case SELECT_MINUTE:
                    selectedMinute = Integer.parseInt(input.trim());
                    System.out.println("Selected minute: " + selectedMinute);
                    currentStep = Step.SELECT_DURATION;
                    sendDurationSelectionPrompt(userId);
                    break;
                case SELECT_DURATION:
                    int durationMinutes = Integer.parseInt(input.trim());
                    System.out.println("Selected duration: " + durationMinutes);
                    currentStep = Step.SELECT_LOCATION;
                    sendLocationSelectionPrompt(userId);
                    break;
                case SELECT_LOCATION:
                    selectedLocation = input;
                    System.out.println("Selected location: " + selectedLocation);
                    currentStep = Step.ENTER_NAME;
                    sendNamePrompt(userId);
                    break;
                case ENTER_NAME:
                    name = input;
                    System.out.println("Entered name: " + name);
                    currentStep = Step.ENTER_DESCRIPTION;
                    sendDescriptionPrompt(userId);
                    break;
                case ENTER_DESCRIPTION:
                    description = input;
                    System.out.println("Entered description: " + description);
                    currentStep = Step.SELECT_RECURRING;
                    sendRecurringPrompt(userId);
                    break;
                case SELECT_RECURRING:
                    recurring = input.equalsIgnoreCase("yes");
                    System.out.println("Recurring: " + recurring);
                    currentStep = Step.CONFIRM;
                    saveTraining(userId);
                    break;
                case CONFIRM:
                    botService.sendText(userId, CONFIRMATION_MESSAGE);
                    return true;
            }
        } catch (NumberFormatException e) {
            botService.sendText(userId, "Ошибка при выборе времени. Пожалуйста, выберите час и минуты с помощью клавиатуры.");
            System.err.println("Error: " + e.getMessage());
        } catch (Exception e) {
            botService.sendText(userId, ERROR_MESSAGE);
            System.err.println("Error: " + e.getMessage());
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
        int endHour = 22;

        List<InlineKeyboardButton> row = new ArrayList<>();
        for (int hour = startHour; hour <= endHour; hour++) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(String.format("%02d", hour));
            button.setCallbackData(String.valueOf(hour));

            row.add(button);

            if (row.size() == 7 || hour == endHour) {
                rows.add(new ArrayList<>(row));
                row.clear();
            }
        }

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
            button.setCallbackData(String.valueOf(minute));
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            rows.add(row);
        }

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

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    private void sendLocationSelectionPrompt(long userId) {
        botService.sendText(userId, SELECT_LOCATION_PROMPT);
    }

    private void sendNamePrompt(long userId) {
        botService.sendText(userId, ENTER_NAME_PROMPT);
    }

    private void sendDescriptionPrompt(long userId) {
        botService.sendText(userId, ENTER_DESCRIPTION_PROMPT);
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
        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private void saveTraining(long userId) {
        BookingObject bookingObject = new BookingObject();
        bookingObject.setDate(selectedDate);
        bookingObject.setTime(LocalTime.of(selectedHour, selectedMinute));
        bookingObject.setLocation(selectedLocation);
        bookingObject.setName(name);
        bookingObject.setDescription(description);
        bookingObject.setRecurring(recurring);
        bookingObjectService.save(bookingObject);
        botService.sendText(userId, CONFIRMATION_MESSAGE);
    }
}
