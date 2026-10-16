package dev.portfolio.taskflow.dto.event;

import java.time.LocalDateTime;

public record TaskEvent(
        EventType type,
        Long projectId,
        Long taskId,
        String fieldChanged,
        Object oldValue,
        Object newValue,
        String actorUsername,
        LocalDateTime timestamp
) {
    public enum EventType {
        TASK_CREATED,
        TASK_UPDATED,
        TASK_DELETED,
        COMMENT_ADDED
    }

    public static TaskEvent of(EventType type, Long projectId, Long taskId,
                               String field, Object oldVal, Object newVal, String actor) {
        return new TaskEvent(type, projectId, taskId, field, oldVal, newVal, actor, LocalDateTime.now());
    }
}