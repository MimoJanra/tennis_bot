package org.telegram.command.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.bot.BotService;
import org.telegram.bot.UpdateUtil;
import org.telegram.command.Command;
import org.telegram.command.CommandName;
import org.telegram.models.TrainingObject;
import org.telegram.models.User;
import org.telegram.service.TrainingObjectService;
import org.telegram.service.TrainingService;
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
public class Training implements Command {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yy");
    private static final String SELECT_DATE = "Выберите дату:";
    private static final String SELECT_TRAINING = "Выберите тренировку с доступными местами:";
    private static final String INCORRECT_DAY = "На этот день не получится, выберите другой";
    private static final String CONFIRMATION_TEXT = "Готово! Вы записались на тренировку %s\n%s";
    private static final String ADMIN_CONFIRMATION_TEXT = "Пользователь %s записался на тренировку %s в %s. Подтвердите запись.";
    private static final String APPROVAL_TEXT = "Ваша запись на тренировку %s в %s была подтверждена.";
    private final BotService botService;
    private final TrainingService trainingService;
    private final TrainingObjectService trainingObjectService;
    private final UserService userService;
    private final Map<Long, Step> usersSteps = new HashMap<>();
    private final Map<Long, Long> pendingApprovals = new HashMap<>();
    @Value("${bot.admin-usernames}")
    private String adminUsername;
    private boolean isFinished;
    private org.telegram.models.Training training;
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
        training = new org.telegram.models.Training();
        training.setUser(userOpt.orElseGet(() -> userService.createOrUpdate(update)));

        botService.sendMarkup(userId, SELECT_DATE, makeCalendar());
        usersSteps.put(userId, Step.SELECT_DATE);
    }

    private void selectDate(Long userId, String input) {
        try {
            selectedDate = LocalDate.parse(input, DateTimeFormatter.ISO_LOCAL_DATE);
            System.out.println("Selected date: " + selectedDate);
        } catch (DateTimeParseException e) {
            botService.sendText(userId, "Некорректная дата. Пожалуйста, выберите дату на клавиатуре.");
            return;
        }

        if (selectedDate.isBefore(LocalDate.now())) {
            botService.sendText(userId, INCORRECT_DAY);
        } else {
            List<TrainingObject> availableTrainings = trainingObjectService.findAvailableByDate(selectedDate);
            System.out.println("Available trainings: " + availableTrainings);
            if (availableTrainings.isEmpty()) {
                botService.sendText(userId, "На выбранную дату нет доступных тренировок.");
            } else {
                botService.sendMarkup(userId, SELECT_TRAINING, makeTrainingsList(availableTrainings));
                usersSteps.put(userId, Step.SELECT_TRAINING);
            }
        }
    }

    private void selectTraining(Long userId, String input) {
        try {
            Long trainingId = Long.parseLong(input);
            System.out.println("Training ID: " + trainingId);
            Optional<TrainingObject> trainingOpt = trainingObjectService.findById(trainingId);
            System.out.println("Training found: " + trainingOpt.isPresent());
            if (trainingOpt.isPresent()) {
                TrainingObject training = trainingOpt.get();
                System.out.println("Booking slot for training: " + training);
                training.bookSlot();
                trainingObjectService.save(training);

                this.training.setTrainingObject(training);
                this.training.setDate(selectedDate);
                trainingService.save(this.training);

                usersSteps.put(userId, Step.ADMIN_CONFIRMATION);
                adminConfirmation(userId);
            } else {
                botService.sendText(userId, "Выбранная тренировка не найдена.");
            }
        } catch (NumberFormatException e) {
            botService.sendText(userId, "Неверный формат ID тренировки.");
        } catch (Exception e) {
            botService.sendText(userId, "Произошла ошибка при выборе тренировки.");
            e.printStackTrace();
        }
    }

    private void adminConfirmation(Long userId) {
        TrainingObject training = this.training.getTrainingObject();
        String adminText = String.format(ADMIN_CONFIRMATION_TEXT,
                this.training.getUser().getName(), training.getName(), training.getLocation());

        Long adminChatId = userService.findByUsername(adminUsername).map(User::getChatId).orElse(null);

        if (adminChatId != null) {
            System.out.println("Sending admin confirmation to chat: " + adminChatId);
            System.out.println("Admin message: " + adminText);
            botService.sendMarkup(adminChatId, adminText, makeAdminApprovalButtons(userId));
            pendingApprovals.put(userId, adminChatId);
        } else {
            System.out.println("Admin user not found for username: " + adminUsername);
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
        TrainingObject training = this.training.getTrainingObject();
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
                button.setCallbackData(day.format(DateTimeFormatter.ISO_LOCAL_DATE));
                keyboardButtonRow.add(button);
                day = day.plusDays(1);
            }
            totalList.add(keyboardButtonRow);
        }

        InlineKeyboardButton cancelButton = new InlineKeyboardButton();
        cancelButton.setText("Отмена");
        cancelButton.setCallbackData("CANCEL");

        List<InlineKeyboardButton> cancelRow = new ArrayList<>();
        cancelRow.add(cancelButton);
        totalList.add(cancelRow);

        inlineKeyboardMarkup.setKeyboard(totalList);
        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup makeTrainingsList(List<TrainingObject> trainings) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (TrainingObject training : trainings) {
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

    private enum Step {
        BEGIN,
        SELECT_DATE,
        SELECT_TRAINING,
        ADMIN_CONFIRMATION,
        CONFIRMATION
    }
}
