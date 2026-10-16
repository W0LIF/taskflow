package dev.portfolio.taskflow.service;

import dev.portfolio.taskflow.dto.event.TaskEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskEventPublisher {
    private final SimpMessagingTemplate messagingTemplate;

    public void publish(Long projectId, TaskEvent event){
        messagingTemplate.convertAndSend(
                "/topic/projects/" + projectId,
                event
        );
    }
}
