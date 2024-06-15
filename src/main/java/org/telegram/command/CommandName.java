package org.telegram.command;

import org.telegram.command.impl.*;

public enum CommandName {
    NEW_BOOKING("Тренировки", NewBooking.class, false),
    ADD_TRAINING("Добавить тренировку", AddTraining.class, true),
    EDIT_OBJECT("Изменить тренировку", EditObject.class, true),
    DELETE_OBJECT("Удалить тренировку", DeleteObject.class, true),
    ADD_SUBSCRIPTION("Добавить абонемент", AddSubscription.class, true),
    SUBSCRIPTION_STATS("Статистика абонементов", SubscriptionStats.class, true),
    START("/start", StartCommand.class, false),
    HELP("help", HelpCommand.class, false),
    MY_BOOKINGS("Мои записи", MyBookings.class, false);

    private final String text;
    private final Class<? extends Command> className;
    private final boolean isAdmin;

    CommandName(String text, Class<? extends Command> className, boolean isAdmin) {
        this.text = text;
        this.className = className;
        this.isAdmin = isAdmin;
    }

    public String getText() {
        return text;
    }

    public Class<? extends Command> getClassName() {
        return className;
    }

    public boolean isAdmin() {
        return isAdmin;
    }
}