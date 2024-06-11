package org.telegram.bot;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@RequiredArgsConstructor
@Component
public class Runner {

    private final Bot bot;
    private boolean isBotRegistered = false;

    @EventListener(ApplicationReadyEvent.class)
    public void registerBot() {
        if (!isBotRegistered) {
            System.out.println("Registering bot...");
            try {
                TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
                botsApi.registerBot(bot);
                isBotRegistered = true;
                System.out.println("Bot registered successfully.");

            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Bot is already registered.");
        }
    }
}
