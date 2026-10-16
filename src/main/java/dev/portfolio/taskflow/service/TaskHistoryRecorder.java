package dev.portfolio.taskflow.service;

import dev.portfolio.taskflow.model.Task;
import dev.portfolio.taskflow.model.TaskHistory;
import dev.portfolio.taskflow.model.User;
import dev.portfolio.taskflow.repository.TaskHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskHistoryRecorder {
    private final TaskHistoryRepository taskHistoryRepository;

    public void record(Task task, User user, String field, Object oldValue, Object newValue){
        String oldStr = oldValue == null ? null : oldValue.toString();
        String newStr = newValue == null ? null : newValue.toString();

        if (java.util.Objects.equals(oldStr, newStr)){
            return;
        }

        TaskHistory history = TaskHistory.builder()
                .task(task)
                .user(user)
                .fieldChanged(field)
                .oldValue(oldStr)
                .newValue(newStr)
                .build();

        taskHistoryRepository.save(history);
    }
}
