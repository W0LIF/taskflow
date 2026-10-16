package dev.portfolio.taskflow.repository;

import dev.portfolio.taskflow.enums.TaskStatus;
import dev.portfolio.taskflow.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("""
        SELECT t FROM Task t
        WHERE t.project.id = :projectId
          AND (:status IS NULL OR t.status = :status)
          AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId)
        """)
    Page<Task> findWithFilters(@Param("projectId") Long projectId,
                               @Param("status") TaskStatus status,
                               @Param("assigneeId") Long assigneeId,
                               Pageable pageable);
}