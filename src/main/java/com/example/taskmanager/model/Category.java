package com.example.taskmanager.model;

public enum Category {
    РАБОТА("Работа"),
    ЛИЧНОЕ("Личное"),
    ПОКУПКИ("Покупки"),
    УЧЕБА("Учеба");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}