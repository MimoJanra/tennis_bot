package org.telegram.bot;

import org.telegram.telegrambots.meta.api.objects.Update;

public class UpdateUtil {
    public static long getUserId(Update update) {
        long userId = update.hasCallbackQuery()
                ? update.getCallbackQuery().getMessage().getChatId()
                : update.getMessage().getChatId();
        System.out.println("User ID: " + userId);
        return userId;
    }

    public static String getInput(Update update) {
        String input = update.hasCallbackQuery()
                ? update.getCallbackQuery().getData()
                : update.getMessage().getText();
        System.out.println("Input: " + input);
        return input;
    }
}
