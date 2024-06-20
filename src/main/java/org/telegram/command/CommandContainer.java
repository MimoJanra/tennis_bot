package org.telegram.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.command.impl.CancelCommand;
import org.telegram.models.User;
import org.telegram.service.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class CommandContainer {

    private static final Logger logger = LoggerFactory.getLogger(CommandContainer.class);

    private final ApplicationContext context;
    private final UserService userService;
    private final Map<String, Class<? extends Command>> commandClasses = new HashMap<>();
    private final Map<String, Command> activeCommands = new HashMap<>();

    public CommandContainer(ApplicationContext context, UserService userService) {
        this.context = context;
        this.userService = userService;

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
        logger.info("Setting active command for chatId {}: {}", chatId, command.getClass().getSimpleName());
        if (isAdminCommand(command) && !isAdminUser(chatId)) {
            logger.error("Unauthorized access attempt by chatId {}: {}", chatId, command.getClass().getSimpleName());
            throw new SecurityException("Unauthorized access to admin command");
        }
        activeCommands.put(chatId, command);
    }

    public void clearActiveCommand(String chatId) {
        logger.info("Clearing active command for chatId {}", chatId);
        activeCommands.remove(chatId);
    }

    private boolean isAdminCommand(Command command) {
        for (CommandName commandName : CommandName.values()) {
            if (commandName.getClassName().equals(command.getClass()) && commandName.isAdmin()) {
                return true;
            }
        }
        return false;
    }

    private boolean isAdminUser(String chatId) {
        long userId = Long.parseLong(chatId);
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            boolean isAdmin = user.isAdmin();
            logger.info("isAdmin check for chatId {}: userId {}: userName {}: isAdmin {}", chatId, user.getId(), user.getUsername(), isAdmin);
            return isAdmin;
        }
        logger.info("No user found for chatId {}", chatId);
        return false;
    }
}
