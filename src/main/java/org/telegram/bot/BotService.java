package org.telegram.bot;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.List;

public interface BotService {
    void sendText(Long chatId, String text);

    void sendWithKeyboard(Long chatId, String text, List<Button> buttons);

    void sendMarkup(Long chatId, String text, ReplyKeyboard markup);

    void sendWithInlineKeyboard(Long chatId, String text, InlineKeyboardMarkup inlineKeyboard);
}
