package org.telegram.command;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface Command {
    boolean execute(Update update, boolean isBeginning);

    CommandName getName();
}
