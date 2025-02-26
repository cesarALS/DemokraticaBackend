package com.demokratica.backend.Model;

import java.time.LocalDateTime;

import com.demokratica.backend.Exceptions.InvalidTimesException;

public class Placeholder {
    public enum EventStatus {NOT_STARTED, ONGOING, FINISHED}

    public enum ActivityType {POLL, TIDEMAN, PLANNING_POKER, WORD_CLOUD, TEXT}

    public static EventStatus getEventStatus(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        if (start.isAfter(end)) {
            throw new InvalidTimesException(start, end);
        }
        if (start.isBefore(now) && end.isBefore(now)) {
            return EventStatus.FINISHED;
        }
        if (start.isBefore(now) && end.isAfter(now)) {
            return EventStatus.ONGOING;
        }
        //if start.isAfter(now) && end.isAfter(now)
        return EventStatus.NOT_STARTED;
    }
}
