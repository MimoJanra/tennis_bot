package org.telegram.command;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.command.impl.AddTraining;

import java.util.HashMap;
import java.util.Map;

@Component
public class CommandContainer {

    private final ApplicationContext context;
    private final Map<String, Class<? extends Command>> commandClasses = new HashMap<>();

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

    public Command getCallbackCommand(String callbackData) {
        return context.getBean(AddTraining.class);
    }
}
