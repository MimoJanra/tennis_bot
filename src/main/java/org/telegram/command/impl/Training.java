package org.telegram.command.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.bot.BotService;
import org.telegram.bot.UpdateUtil;
import org.telegram.command.Command;
import org.telegram.command.CommandName;
import org.telegram.models.Booking;
import org.telegram.models.BookingObject;
import org.telegram.models.User;
import org.telegram.service.BookingObjectService;
import org.telegram.service.BookingService;
import org.telegram.service.UserService;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@RequiredArgsConstructor
@Component
@Scope("prototype")
public class Training implements Command {

    private enum Step {
        BEGIN,
        SELECT_DATE,
        SELECT_TRAINING,
        ADMIN_CONFIRMATION,
        CONFIRMATION
    }

    @Value("${bot.admin-usernames}")
    private String adminUsername;

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yy");

    private static final String SELECT_DATE = "Выберите дату:";
    private static final String SELECT_TRAINING = "Выберите тренировку с доступными местами:";
    private static final String INCORRECT_DAY = "На этот день не получится, выберите другой";
    private static final String CONFIRMATION_TEXT = "Готово! Вы записались на тренировку %s\n%s";
    private static final String ADMIN_CONFIRMATION_TEXT = "Пользователь %s записался на тренировку %s в %s. Подтвердите запись.";
    private static final String APPROVAL_TEXT = "Ваша запись на тренировку %s в %s была подтверждена.";

    private final BotService botService;
    private final BookingService bookingService;
    private final BookingObjectService bookingObjectService;
    private final UserService userService;

    private final Map<Long, Step> usersSteps = new HashMap<>();
    private final Map<Long, Long> pendingApprovals = new HashMap<>();
    private boolean isFinished;
    private Booking booking;
    private LocalDate selectedDate;

    @Override
    public boolean execute(Update update, boolean isBeginning) {
        Long userId = UpdateUtil.getUserId(update);
        String input = UpdateUtil.getInput(update);

        if (!usersSteps.containsKey(userId) || isBeginning) {
            usersSteps.put(userId, Step.BEGIN);
        }

        switch (usersSteps.get(userId)) {
            case BEGIN -> begin(update);
            case SELECT_DATE -> selectDate(userId, input);
            case SELECT_TRAINING -> selectTraining(userId, input);
            case ADMIN_CONFIRMATION -> adminConfirmation(userId);
            case CONFIRMATION -> confirmation(userId);
        }

        return isFinished;
    }

    @Override
    public CommandName getName() {
        return CommandName.NEW_BOOKING;
    }

    private void begin(Update update) {
        long userId = UpdateUtil.getUserId(update);

        Optional<User> userOpt = userService.findById(userId);
        booking = new Booking();
        booking.setUser(userOpt.orElseGet(() -> userService.create(update)));

        botService.sendMarkup(userId, SELECT_DATE, makeCalendar());
        usersSteps.put(userId, Step.SELECT_DATE);
    }

    private void selectDate(Long userId, String input) {
        try {
            selectedDate = LocalDate.parse(input, dateFormatter);
            if (selectedDate.isBefore(LocalDate.now())) {
                botService.sendText(userId, INCORRECT_DAY);
            } else {
                List<BookingObject> availableTrainings = bookingObjectService.findAvailableByDate(selectedDate);
                if (availableTrainings.isEmpty()) {
                    botService.sendText(userId, "На выбранную дату нет доступных тренировок.");
                } else {
                    botService.sendMarkup(userId, SELECT_TRAINING, makeTrainingsList(availableTrainings));
                    usersSteps.put(userId, Step.SELECT_TRAINING);
                }
            }
        } catch (DateTimeParseException e) {
            botService.sendText(userId, INCORRECT_DAY);
        }
    }

    private void selectTraining(Long userId, String input) {
        try {
            Long trainingId = Long.parseLong(input);
            Optional<BookingObject> trainingOpt = bookingObjectService.findById(trainingId);
            if (trainingOpt.isPresent()) {
                BookingObject training = trainingOpt.get();
                training.bookSlot();
                bookingObjectService.save(training);

                booking.setBookingObject(training);
                booking.setDate(selectedDate);
                bookingService.save(booking);

                usersSteps.put(userId, Step.ADMIN_CONFIRMATION);
                adminConfirmation(userId);
            } else {
                botService.sendText(userId, "Выбранная тренировка не найдена.");
            }
        } catch (NumberFormatException e) {
            botService.sendText(userId, "Неверный формат ID тренировки.");
        }
    }

    private void adminConfirmation(Long userId) {
        BookingObject training = booking.getBookingObject();
        String adminText = String.format(ADMIN_CONFIRMATION_TEXT,
                booking.getUser().getName(), training.getName(), training.getLocation());

        Long adminUserId = userService.findByUsername(adminUsername).map(User::getId).orElse(null);
        if (adminUserId != null) {
            botService.sendMarkup(adminUserId, adminText, makeAdminApprovalButtons(userId));
            pendingApprovals.put(userId, adminUserId);
        }

        usersSteps.put(userId, Step.CONFIRMATION);
    }

    private InlineKeyboardMarkup makeAdminApprovalButtons(Long userId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton approveButton = new InlineKeyboardButton();
        approveButton.setText("Подтвердить");
        approveButton.setCallbackData("approve_" + userId);

        InlineKeyboardButton rejectButton = new InlineKeyboardButton();
        rejectButton.setText("Отклонить");
        rejectButton.setCallbackData("reject_" + userId);

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(approveButton);
        row.add(rejectButton);

        rows.add(row);
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    private void confirmation(Long userId) {
        BookingObject training = booking.getBookingObject();
        String confirmationText = String.format(CONFIRMATION_TEXT,
                training.getName(), training.getLocation());
        botService.sendText(userId, confirmationText);

        String adminText = String.format(APPROVAL_TEXT,
                training.getName(), training.getLocation());

        // Отправка уведомления пользователю
        botService.sendText(userId, adminText);

        usersSteps.put(userId, Step.BEGIN);
        isFinished = true;
    }

    private InlineKeyboardMarkup makeCalendar() {
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

    private InlineKeyboardMarkup makeTrainingsList(List<BookingObject> trainings) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (BookingObject training : trainings) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(training.getName() + " (" + training.getAvailableSlots() + " мест)");
            button.setCallbackData(training.getId().toString());
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            rows.add(row);
        }

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }
}
