package com.example.taskmanager.service;

import com.example.taskmanager.bot.TaskManagerBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class TelegramService {

    @Autowired
    private TaskManagerBot bot;

    public void sendNotification(Long chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(message);
        
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendTaskReminder(Long chatId, String taskTitle, String dueDate) {
        String message = String.format("Напоминание!\nЗадача: %s\nСрок: %s", 
            taskTitle, dueDate);
        sendNotification(chatId, message);
    }
} 