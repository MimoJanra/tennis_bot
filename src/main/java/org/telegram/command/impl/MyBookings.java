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
import org.telegram.models.Booking;
import org.telegram.models.User;
import org.telegram.service.BookingService;
import org.telegram.service.UserService;

import java.util.*;

@RequiredArgsConstructor
@Component
@Scope("prototype")
public class MyBookings implements Command {

    private enum Step {
        BEGIN,
        DELETE,
        CONFIRM
    }

    private static final String NO_BOOKINGS = "У вас пока нет бронирований";
    private static final String DELETE = "Удалить";
    private static final String CANCEL = "Отмена";
    private static final String DONE = "Готово";
    private static final String CONFIRM = "Вы уверены что хотите удалить бронь?";

    private final BotService botService;
    private final BookingService bookingService;
    private final UserService userService;

    private final Map<Long, Step> usersSteps = new HashMap<>();
    private boolean isFinished;

    public boolean execute(Update update, boolean isBeginning) {
        Long userId = UpdateUtil.getUserId(update);
        String input = UpdateUtil.getInput(update);

        if (!usersSteps.containsKey(userId) || isBeginning) {
            usersSteps.put(userId, Step.BEGIN);
        }

        switch (usersSteps.get(userId)) {
            case BEGIN -> begin(update);
            case DELETE -> deleteBooking(userId, input);
            case CONFIRM -> confirmDeleting(userId, input);
        }

        return isFinished;
    }

    @Override
    public CommandName getName() {
        return CommandName.MY_BOOKINGS;
    }

    private void begin(Update update) {
        long userId = UpdateUtil.getUserId(update);
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            List<Booking> bookingList = user.isAdmin() ? bookingService.findAll() : bookingService.findByUserId(userId);

            if (bookingList.isEmpty()) {
                botService.sendText(userId, NO_BOOKINGS);
            } else {
                bookingList.forEach(booking -> botService.sendWithKeyboard(userId,
                        booking.getFullText(user.isAdmin()), getDeleteButton(booking)));
            }

            usersSteps.put(userId, Step.DELETE);
        }
    }

    private void deleteBooking(long userId, String input) {
        Optional<Booking> bookingOpt = bookingService.findById(input);
        if (bookingOpt.isPresent()) {
            botService.sendWithKeyboard(userId, CONFIRM, getConfirmButtons(bookingOpt.get()));
            usersSteps.put(userId, Step.CONFIRM);
        }
    }

    private void confirmDeleting(long userId, String input) {
        Optional<Booking> bookingOpt = bookingService.findById(input);
        if (bookingOpt.isPresent()) {
            bookingService.delete(bookingOpt.get());
            botService.sendText(userId, DONE);
        }

        usersSteps.put(userId, Step.BEGIN);
        isFinished = true;
    }

    private List<Button> getDeleteButton(Booking booking) {
        return List.of(new Button(String.valueOf(booking.getId()), DELETE));
    }

    private List<Button> getConfirmButtons(Booking booking) {
        return List.of(
                new Button(String.valueOf(booking.getId()), DELETE),
                new Button("0", CANCEL)
        );
    }

}
