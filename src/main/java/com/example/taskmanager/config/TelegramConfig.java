package com.example.taskmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import com.example.taskmanager.bot.TaskManagerBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class TelegramConfig {

    private static final Logger log = LoggerFactory.getLogger(TelegramConfig.class);

    @Bean
    public TelegramBotsApi telegramBotsApi(TaskManagerBot bot) throws TelegramApiException {
        DefaultBotSession.setDefaultMaxThreads(1);
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        try {
            api.registerBot(bot);
        } catch (TelegramApiRequestException e) {
            if (e.getMessage().contains("Conflict: terminated by other getUpdates request")) {
                log.warn("Another bot instance is already running");
            } else {
                throw e;
            }
        }
        return api;
    }
} 