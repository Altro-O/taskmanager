package com.example.taskmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import com.example.taskmanager.bot.TaskManagerBot;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class TelegramConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(TaskManagerBot bot) throws TelegramApiException {
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(bot);
            log.info("Telegram bot successfully registered");
            return api;
        } catch (TelegramApiRequestException e) {
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("Conflict")) {
                log.warn("Another bot instance is already running, skipping registration");
                return new TelegramBotsApi(DefaultBotSession.class);
            }
            log.error("Failed to register Telegram bot", e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while registering Telegram bot", e);
            throw e;
        }
    }
} 