package com.example.taskmanager.model;

import java.time.LocalTime;
import java.time.LocalDateTime;

public enum TimeLabel {
    MORNING("Утро", LocalTime.of(6, 0), LocalTime.of(12, 0)),
    AFTERNOON("День", LocalTime.of(12, 0), LocalTime.of(17, 0)),
    EVENING("Вечер", LocalTime.of(17, 0), LocalTime.of(22, 0)),
    NIGHT("Ночь", LocalTime.of(22, 0), LocalTime.of(6, 0));

    private final String name;
    private final LocalTime startTime;
    private final LocalTime endTime;

    TimeLabel(String name, LocalTime startTime, LocalTime endTime) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getName() {
        return name;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public static TimeLabel fromDateTime(LocalDateTime dateTime) {
        LocalTime time = dateTime.toLocalTime();
        for (TimeLabel label : TimeLabel.values()) {
            if (isTimeBetween(time, label.getStartTime(), label.getEndTime())) {
                return label;
            }
        }
        return MORNING; // значение по умолчанию
    }

    private static boolean isTimeBetween(LocalTime time, LocalTime start, LocalTime end) {
        if (end.isBefore(start)) {
            return !time.isBefore(start) || !time.isAfter(end);
        }
        return !time.isBefore(start) && !time.isAfter(end);
    }
}
