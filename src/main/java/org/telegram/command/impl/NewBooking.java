package org.telegram.command.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.bot.BotService;
import org.telegram.bot.Button;
import org.telegram.bot.UpdateUtil;
import org.telegram.command.Command;
import org.telegram.command.CommandName;
import org.telegram.models.Booking;
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
public class NewBooking implements Command {

    private enum Step {
        BEGIN,
        SELECT_DATE,
        CONFIRMATION
    }

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yy");

    private static final String SELECT_DATE = "Выберите дату:";
    private static final String INCORRECT_DAY = "На этот день не получится, выберите другой";
    private static final String CONFIRMATION_TEXT = "Готово!\nЗаписались на %s\n%s";

    private final BotService botService;
    private final BookingObjectService bookingObjectService;
    private final BookingService bookingService;
    private final UserService userService;

    private final Map<Long, Step> usersSteps = new HashMap<>();
    private boolean isFinished;
    private Booking booking;

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
        List<Button> buttons = new ArrayList<>();

        Optional<User> userOpt = userService.findById(userId);
        booking = new Booking();
        booking.setUser(userOpt.orElseGet(() -> userService.create(update)));

        botService.sendMarkup(userId, SELECT_DATE, makeCalendar());
        usersSteps.put(userId, Step.SELECT_DATE);
    }

    private void selectDate(Long userId, String input) {
        try {
            LocalDate selectedDate = LocalDate.parse(input, dateFormatter);
            if (selectedDate.isBefore(LocalDate.now())) {
                botService.sendText(userId, INCORRECT_DAY);
            } else {
                booking.setDate(selectedDate);
                bookingService.save(booking);

                usersSteps.put(userId, Step.CONFIRMATION);
                confirmation(userId);
            }
        } catch (DateTimeParseException e) {
            botService.sendText(userId, INCORRECT_DAY);
        }
    }

    private void confirmation(Long userId) {
        String confirmationText = String.format(CONFIRMATION_TEXT,
                booking.getDate().format(dateFormatter), booking.getCourt().getName());
        botService.sendText(userId, confirmationText);

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
}
