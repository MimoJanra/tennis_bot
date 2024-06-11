package org.telegram.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.telegram.command.impl.*;

@RequiredArgsConstructor
@Getter
public enum CommandName {

    ADD_OBJECT("Добавить объект", AddObject.class),
    EDIT_OBJECT("Редактировать объект", EditObject.class),
    MY_BOOKINGS("Мои бронирования", MyBookings.class),
    NEW_BOOKING("Забронировать", NewBooking.class),
    START("/start", Start.class);

    private final String text;
    private final Class<? extends Command> className;
}