package dev.portfolio.taskflow.mapper;

import dev.portfolio.taskflow.dto.response.TaskHistoryResponse;
import dev.portfolio.taskflow.dto.response.TaskResponse;
import dev.portfolio.taskflow.model.Task;
import dev.portfolio.taskflow.model.TaskHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskMapper {

    private final UserMapper userMapper;
    private final LabelMapper labelMapper;

    public TaskResponse toResponse(Task task) {
        List<dev.portfolio.taskflow.dto.response.LabelResponse> labels =
                task.getLabels().stream().map(labelMapper::toResponse).toList();

        return new TaskResponse(
                task.getId(),
                task.getProject().getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                userMapper.toSummary(task.getAssignee()),
                userMapper.toSummary(task.getReporter()),
                task.getDueDate(),
                labels,
                task.getComments().size(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    public TaskHistoryResponse toHistoryResponse(TaskHistory history) {
        return new TaskHistoryResponse(
                history.getId(),
                userMapper.toSummary(history.getUser()),
                history.getFieldChanged(),
                history.getOldValue(),
                history.getNewValue(),
                history.getChangedAt()
        );
    }
}