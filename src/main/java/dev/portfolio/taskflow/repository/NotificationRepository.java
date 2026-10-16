package dev.portfolio.taskflow.repository;

import dev.portfolio.taskflow.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    long countByUserIdAndIsReadFalse(Long userId);
    boolean existsByUserIdAndTaskIdAndType(Long userId, Long taskId, String type);
}