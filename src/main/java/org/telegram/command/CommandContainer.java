package org.telegram.command;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.command.impl.AddTraining;
import org.telegram.command.impl.CancelCommand;

import java.util.HashMap;
import java.util.Map;

@Component
public class CommandContainer {

    private final ApplicationContext context;
    private final Map<String, Class<? extends Command>> commandClasses = new HashMap<>();
    private final Map<String, Command> activeCommands = new HashMap<>();

    public CommandContainer(ApplicationContext context) {
        this.context = context;

        for (CommandName commandName : CommandName.values()) {
            commandClasses.put(commandName.getText(), commandName.getClassName());
        }
    }

    public Command getCommand(String commandName) {
        Class<? extends Command> commandClass = commandClasses.get(commandName);
        if (commandClass == null) {
            throw new IllegalArgumentException("Command not found: " + commandName);
        }
        return context.getBean(commandClass);
    }

    public boolean hasCommand(String name) {
        return commandClasses.containsKey(name);
    }

    public Command getCallbackCommand(String callbackData, String chatId) {
        if ("CANCEL".equalsIgnoreCase(callbackData)) {
            return context.getBean(CancelCommand.class);
        }
        return getActiveCommand(chatId);
    }

    public Command getActiveCommand(String chatId) {
        return activeCommands.get(chatId);
    }

    public void setActiveCommand(String chatId, Command command) {
        activeCommands.put(chatId, command);
    }

    public void clearActiveCommand(String chatId) {
        activeCommands.remove(chatId);
    }
}
