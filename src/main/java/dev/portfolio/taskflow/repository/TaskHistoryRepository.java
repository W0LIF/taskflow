package dev.portfolio.taskflow.repository;

import dev.portfolio.taskflow.model.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {
    List<TaskHistory> findAllByTaskIdOrderByChangedAtDesc(Long taskId);
}