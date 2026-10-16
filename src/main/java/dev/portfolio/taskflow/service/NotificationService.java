package dev.portfolio.taskflow.service;

import dev.portfolio.taskflow.dto.response.NotificationResponse;
import dev.portfolio.taskflow.dto.response.PageResponse;
import dev.portfolio.taskflow.exception.AccessDeniedException;
import dev.portfolio.taskflow.exception.ResourceNotFoundException;
import dev.portfolio.taskflow.model.Notification;
import dev.portfolio.taskflow.model.User;
import dev.portfolio.taskflow.repository.NotificationRepository;
import dev.portfolio.taskflow.security.ProjectAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ProjectAccessService accessService;

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getMyNotifications(Pageable pageable) {
        User current = accessService.getCurrentUser();
        return PageResponse.of(
                notificationRepository.findAllByUserIdOrderByCreatedAtDesc(current.getId(), pageable),
                this::toResponse
        );
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        return notificationRepository.countByUserIdAndIsReadFalse(accessService.getCurrentUser().getId());
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notif.getUser().getId().equals(accessService.getCurrentUser().getId())) {
            throw new AccessDeniedException("Not your notification");
        }
        notif.setRead(true);
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getMessage(),
                n.isRead(),
                n.getTask() != null ? n.getTask().getId() : null,
                n.getProject() != null ? n.getProject().getId() : null,
                n.getCreatedAt()
        );
    }
}