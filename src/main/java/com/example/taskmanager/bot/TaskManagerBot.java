package com.example.taskmanager.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.service.UserService;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.model.Category;
import com.example.taskmanager.model.Priority;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String messageText = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        try {
            User user = userService.getUserByTelegramId(chatId);
            
            if (messageText.startsWith("/start")) {
                sendMessage(chatId, "Добро пожаловать в TaskManager! Используйте /help для списка команд.");
            } else if (messageText.startsWith("/tasks")) {
                showTasks(chatId);
            } else if (messageText.startsWith("/today")) {
                showTodayTasks(chatId);
            } else if (messageText.startsWith("/add")) {
                addTask(chatId, messageText);
            } else if (messageText.startsWith("/complete")) {
                completeTask(chatId, messageText);
            } else if (messageText.equals("/help")) {
                showHelp(chatId);
            }
        } catch (Exception e) {
            sendMessage(chatId, "Произошла ошибка: " + e.getMessage());
        }
    }

    private void showTasks(Long chatId) {
        List<Task> tasks = taskService.getTasksByTelegramChat(chatId);
        if (tasks.isEmpty()) {
            sendMessage(chatId, "У вас нет активных задач.");
            return;
        }

        StringBuilder message = new StringBuilder("Ваши задачи:\n\n");
        for (Task task : tasks) {
            message.append(formatTask(task));
        }
        sendMessage(chatId, message.toString());
    }

    private void showTodayTasks(Long chatId) {
        List<Task> tasks = taskService.getTodayTasks(chatId);
        if (tasks.isEmpty()) {
            sendMessage(chatId, "На сегодня нет задач.");
            return;
        }

        StringBuilder message = new StringBuilder("Задачи на сегодня:\n\n");
        for (Task task : tasks) {
            message.append(formatTask(task));
        }
        sendMessage(chatId, message.toString());
    }

    private void addTask(Long chatId, String message) {
        try {
            String[] parts = message.split(" ", 2);
            if (parts.length < 2) {
                sendMessage(chatId, "Используйте формат: /add Название задачи");
                return;
            }

            Task task = new Task();
            task.setTitle(parts[1]);
            task.setUser(userService.getUserByTelegramId(chatId));
            task.setCreatedAt(LocalDateTime.now());
            
            taskService.createTask(task);
            sendMessage(chatId, "Задача создана: " + task.getTitle());
        } catch (Exception e) {
            sendMessage(chatId, "Ошибка при создании задачи: " + e.getMessage());
        }
    }

    private void completeTask(Long chatId, String message) {
        try {
            String[] parts = message.split(" ", 2);
            if (parts.length < 2) {
                sendMessage(chatId, "Используйте формат: /complete ID_задачи");
                return;
            }

            Long taskId = Long.parseLong(parts[1]);
            taskService.completeTask(chatId, taskId);
            sendMessage(chatId, "Задача отмечена как выполненная!");
        } catch (NumberFormatException e) {
            sendMessage(chatId, "Неверный формат ID задачи");
        } catch (Exception e) {
            sendMessage(chatId, "Ошибка при выполнении задачи: " + e.getMessage());
        }
    }

    private String formatTask(Task task) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return String.format("ID: %d\n📌 %s\n🕒 %s\n%s\n\n",
            task.getId(),
            task.getTitle(),
            task.getDeadline() != null ? task.getDeadline().format(formatter) : "Без срока",
            task.isCompleted() ? "✅ Выполнено" : "⏳ В процессе"
        );
    }

    private void showHelp(Long chatId) {
        String helpText = """
            Доступные команды:
            /tasks - показать все задачи
            /today - задачи на сегодня
            /add [название] - создать задачу
            /complete [ID] - отметить задачу как выполненную
            /help - показать это сообщение
            """;
        sendMessage(chatId, helpText);
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