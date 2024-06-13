package org.telegram.command;

import org.telegram.command.impl.AddTraining;
import org.telegram.command.impl.EditObject;
import org.telegram.command.impl.HelpCommand;
import org.telegram.command.impl.MyBookings;
import org.telegram.command.impl.NewBooking;
import org.telegram.command.impl.StartCommand;

public enum CommandName {
    NEW_BOOKING("new_booking", NewBooking.class),
    ADD_TRAINING("add_training", AddTraining.class),
    EDIT_OBJECT("edit_object", EditObject.class),
    START("start", StartCommand.class),
    HELP("help", HelpCommand.class),
    MY_BOOKINGS("my_bookings", MyBookings.class);

    private final String text;
    private final Class<? extends Command> className;

    CommandName(String text, Class<? extends Command> className) {
        this.text = text;
        this.className = className;
    }

    public String getText() {
        return text;
    }

    public Class<? extends Command> getClassName() {
        return className;
    }
}
