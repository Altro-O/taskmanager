package com.example.taskmanager.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.Base64;

import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.service.UserService;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.model.Category;
import com.example.taskmanager.model.Priority;

@Component
public class TaskManagerBot extends TelegramLongPollingBot {

    @Autowired
    private UserService userService;
    
    @Autowired
    private TaskService taskService;

    @Value("${bot.username}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) return;
        
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        if (text.startsWith("/start")) {
            handleStart(chatId, text);
        } else if (text.startsWith("/link")) {
            handleLink(chatId, text);
        } else if (text.startsWith("/tasks")) {
            handleTasks(chatId);
        } else if (text.startsWith("/today")) {
            handleTodayTasks(chatId);
        } else if (text.startsWith("/new")) {
            handleNewTask(chatId, text);
        } else if (text.startsWith("/complete")) {
            handleComplete(chatId, text);
        } else if (text.startsWith("/settings")) {
            handleSettings(chatId);
        } else if (text.startsWith("/remind")) {
            handleReminders(chatId, text);
        }
    }

    private void handleStart(Long chatId, String text) {
        try {
            if (text.length() > 6) {
                String encodedEmail = text.substring(7);
                String email = new String(Base64.getUrlDecoder().decode(encodedEmail));
                
                userService.linkTelegramChat(email, chatId);
                sendMessage(chatId, "✅ Аккаунт успешно привязан к " + email);
            } else {
                sendMessage(chatId, "Привет! Я бот TaskManager.\n" +
                    "Для привязки аккаунта отсканируйте QR-код в веб-интерфейсе\n" +
                    "или используйте команду /link your@email.com");
            }
        } catch (Exception e) {
            sendMessage(chatId, "❌ Ошибка при привязке аккаунта: " + e.getMessage());
        }
    }

    private void handleLink(Long chatId, String text) {
        String[] parts = text.split(" ");
        if (parts.length != 2) {
            sendMessage(chatId, "Используйте формат: /link your@email.com");
            return;
        }

        String email = parts[1];
        try {
            userService.linkTelegramChat(email, chatId);
            sendMessage(chatId, "Аккаунт успешно привязан!");
        } catch (Exception e) {
            sendMessage(chatId, "Ошибка: " + e.getMessage());
        }
    }

    private void handleTasks(Long chatId) {
        try {
            List<Task> tasks = taskService.getTasksByTelegramChat(chatId);
            if (tasks.isEmpty()) {
                sendMessage(chatId, "У вас нет активных задач");
                return;
            }

            StringBuilder sb = new StringBuilder("Ваши задачи:\n\n");
            for (Task task : tasks) {
                sb.append("- ").append(task.getTitle())
                  .append(" (").append(task.getCategory()).append(")\n");
            }
            sendMessage(chatId, sb.toString());
        } catch (Exception e) {
            sendMessage(chatId, "Ошибка при получении задач");
        }
    }

    private void handleTodayTasks(Long chatId) {
        try {
            LocalDateTime start = LocalDateTime.now().withHour(0).withMinute(0);
            LocalDateTime end = start.plusDays(1);
            List<Task> tasks = taskService.getTodayTasks(chatId, start, end);
            
            if (tasks.isEmpty()) {
                sendMessage(chatId, "На сегодня задач нет");
                return;
            }

            StringBuilder sb = new StringBuilder("Задачи на сегодня:\n\n");
            for (Task task : tasks) {
                sb.append("- ").append(task.getTitle())
                  .append(" (до ").append(task.getDeadline().toLocalTime()).append(")\n");
            }
            sendMessage(chatId, sb.toString());
        } catch (Exception e) {
            sendMessage(chatId, "Ошибка при получении задач");
        }
    }

    private void handleNewTask(Long chatId, String text) {
        try {
            String[] parts = text.substring(5).split("\\|");
            if (parts.length < 5) {
                sendMessage(chatId, "Используйте формат:\n/new Название | Описание | Категория | Приоритет | Дедл��йн");
                return;
            }

            Task task = new Task();
            task.setTitle(parts[0].trim());
            task.setDescription(parts[1].trim());
            task.setCategory(Category.valueOf(parts[2].trim().toUpperCase()));
            task.setPriority(Priority.valueOf(parts[3].trim().toUpperCase()));
            task.setDeadline(LocalDateTime.parse(parts[4].trim(), DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
            
            User user = userService.findByTelegramChatId(chatId);
            task.setUser(user);
            
            taskService.createTask(task);
            sendMessage(chatId, "✅ Задача создана успешно!");
        } catch (Exception e) {
            sendMessage(chatId, "❌ Ошибка: " + e.getMessage());
        }
    }

    private void handleComplete(Long chatId, String text) {
        try {
            Long taskId = Long.parseLong(text.substring(10));
            taskService.completeTask(chatId, taskId);
            sendMessage(chatId, "✅ Задача отмечена как выполненная!");
        } catch (Exception e) {
            sendMessage(chatId, "❌ Ошибка: " + e.getMessage());
        }
    }

    private void handleSettings(Long chatId) {
        User user = userService.findByTelegramChatId(chatId);
        StringBuilder sb = new StringBuilder("⚙️ Настройки уведомлений:\n\n");
        sb.append("Telegram: ").append(user.isEnableTelegramNotifications() ? "✅" : "❌").append("\n");
        sb.append("Email: ").append(user.isEnableEmailNotifications() ? "✅" : "❌").append("\n");
        sb.append("Напоминать за (часов): ").append(user.getReminderHours().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "))).append("\n\n");
        sb.append("Команды:\n");
        sb.append("/remind add 24 - добавить напоминание за 24 часа\n");
        sb.append("/remind remove 24 - убрать напоминание за 24 часа\n");
        sb.append("/remind clear - убрать все напоминания");
        
        sendMessage(chatId, sb.toString());
    }

    private void handleReminders(Long chatId, String text) {
        try {
            String[] parts = text.split(" ");
            if (parts.length < 2) {
                sendMessage(chatId, "Используйте формат:\n/remind add 24\n/remind remove 24\n/remind clear");
                return;
            }

            User user = userService.findByTelegramChatId(chatId);
            String action = parts[1];
            
            switch (action) {
                case "add":
                    if (parts.length < 3) {
                        sendMessage(chatId, "Укажите количество часов: /remind add 24");
                        return;
                    }
                    int hours = Integer.parseInt(parts[2]);
                    userService.addReminder(user, hours);
                    sendMessage(chatId, "✅ Напоминание за " + hours + " часов добавлено");
                    break;
                
                case "remove":
                    if (parts.length < 3) {
                        sendMessage(chatId, "Укажите количество часов: /remind remove 24");
                        return;
                    }
                    hours = Integer.parseInt(parts[2]);
                    userService.removeReminder(user, hours);
                    sendMessage(chatId, "✅ Напоминание за " + hours + " часов удалено");
                    break;
                
                case "clear":
                    userService.clearReminders(user);
                    sendMessage(chatId, "✅ Все напоминания удалены");
                    break;
                
                default:
                    sendMessage(chatId, "❌ Неизвестная команда. Используйте:\n/remind add 24\n/remind remove 24\n/remind clear");
            }
        } catch (NumberFormatException e) {
            sendMessage(chatId, "❌ Ошибка: укажите корректное количество часов");
        } catch (Exception e) {
            sendMessage(chatId, "❌ Ошибка: " + e.getMessage());
        }
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
} 