package dev.portfolio.taskflow.service;

import dev.portfolio.taskflow.model.Notification;
import dev.portfolio.taskflow.model.Task;
import dev.portfolio.taskflow.repository.NotificationRepository;
import dev.portfolio.taskflow.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeadlineNotificationScheduler {

    private static final String TYPE_DEADLINE_SOON = "DEADLINE_SOON";

    private final TaskRepository taskRepository;
    private final NotificationRepository notificationRepository;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void checkUpcomingDeadlines() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in24h = now.plusHours(24);

        List<Task> tasks = taskRepository.findTasksWithUpcomingDeadline(now, in24h);
        log.info("Deadline check: found {} tasks with upcoming deadlines", tasks.size());

        for (Task task : tasks) {
            if (notificationRepository.existsByUserIdAndTaskIdAndType(
                    task.getAssignee().getId(), task.getId(), TYPE_DEADLINE_SOON)) {
                continue;
            }

            Notification notif = Notification.builder()
                    .user(task.getAssignee())
                    .task(task)
                    .project(task.getProject())
                    .type(TYPE_DEADLINE_SOON)
                    .message("Дедлайн задачи «" + task.getTitle() + "» приближается: " + task.getDueDate())
                    .build();

            notificationRepository.save(notif);
            log.info("Notification created for user {} task {}",
                    task.getAssignee().getUsername(), task.getId());
        }
    }
}