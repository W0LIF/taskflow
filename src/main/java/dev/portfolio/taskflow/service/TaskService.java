package dev.portfolio.taskflow.service;

import dev.portfolio.taskflow.dto.event.TaskEvent;
import dev.portfolio.taskflow.dto.request.TaskCreateRequest;
import dev.portfolio.taskflow.dto.request.TaskUpdateRequest;
import dev.portfolio.taskflow.dto.response.PageResponse;
import dev.portfolio.taskflow.dto.response.TaskHistoryResponse;
import dev.portfolio.taskflow.dto.response.TaskResponse;
import dev.portfolio.taskflow.enums.TaskStatus;
import dev.portfolio.taskflow.exception.ResourceNotFoundException;
import dev.portfolio.taskflow.mapper.TaskMapper;
import dev.portfolio.taskflow.model.*;
import dev.portfolio.taskflow.repository.*;
import dev.portfolio.taskflow.security.ProjectAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final LabelRepository labelRepository;
    private final TaskHistoryRepository taskHistoryRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final ProjectAccessService accessService;
    private final TaskHistoryRecorder historyRecorder;
    private final TaskEventPublisher eventPublisher;

    @Transactional
    public TaskResponse create(Long projectId, TaskCreateRequest request) {
        ProjectMember member = accessService.requireMembership(projectId);
        User current = member.getUser();
        Project project = member.getProject();

        Task task = Task.builder()
                .project(project)
                .title(request.title())
                .description(request.description())
                .reporter(current)
                .priority(request.priority())
                .dueDate(request.dueDate())
                .build();

        if (request.assigneeId() != null) {
            User assignee = requireProjectMember(projectId, request.assigneeId());
            task.setAssignee(assignee);
        }

        if (request.labelIds() != null && !request.labelIds().isEmpty()) {
            Set<Label> labels = loadLabelsForProject(projectId, request.labelIds());
            task.setLabels(labels);
        }

        taskRepository.save(task);

        eventPublisher.publish(projectId, TaskEvent.of(
                TaskEvent.EventType.TASK_CREATED,
                projectId, task.getId(),
                null, null, task.getTitle(),
                current.getUsername()
        ));

        return taskMapper.toResponse(task);
    }

    @Transactional(readOnly = true)
    public TaskResponse getById(Long taskId) {
        Task task = getTaskOrThrow(taskId);
        accessService.requireMembership(task.getProject().getId());
        return taskMapper.toResponse(task);
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> getByProject(Long projectId,
                                                   TaskStatus status,
                                                   Long assigneeId,
                                                   Pageable pageable) {
        accessService.requireMembership(projectId);
        Page<Task> page = taskRepository.findWithFilters(projectId, status, assigneeId, pageable);
        return PageResponse.of(page, taskMapper::toResponse);
    }

    @Transactional
    public TaskResponse update(Long taskId, TaskUpdateRequest request) {
        Task task = getTaskOrThrow(taskId);
        ProjectMember member = accessService.requireMembership(task.getProject().getId());
        User current = member.getUser();
        Long projectId = task.getProject().getId();

        if (request.status() != null && request.status() != task.getStatus()) {
            historyRecorder.record(task, current, "status", task.getStatus(), request.status());
            eventPublisher.publish(projectId, TaskEvent.of(
                    TaskEvent.EventType.TASK_UPDATED,
                    projectId, taskId, "status",
                    task.getStatus(), request.status(),
                    current.getUsername()
            ));
            task.setStatus(request.status());
        }

        if (request.title() != null) {
            historyRecorder.record(task, current, "title", task.getTitle(), request.title());
            task.setTitle(request.title());
        }

        if (request.description() != null) {
            historyRecorder.record(task, current, "description",
                    task.getDescription(), request.description());
            task.setDescription(request.description());
        }

        if (request.priority() != null && request.priority() != task.getPriority()) {
            historyRecorder.record(task, current, "priority", task.getPriority(), request.priority());
            task.setPriority(request.priority());
        }

        if (request.assigneeId() != null) {
            User oldAssignee = task.getAssignee();
            User newAssignee = requireProjectMember(task.getProject().getId(), request.assigneeId());
            if (oldAssignee == null || !oldAssignee.getId().equals(newAssignee.getId())) {
                historyRecorder.record(task, current, "assignee",
                        oldAssignee != null ? oldAssignee.getUsername() : null,
                        newAssignee.getUsername());
                task.setAssignee(newAssignee);
            }
        }

        if (request.dueDate() != null
                && !request.dueDate().equals(task.getDueDate())) {
            historyRecorder.record(task, current, "dueDate", task.getDueDate(), request.dueDate());
            task.setDueDate(request.dueDate());
        }

        return taskMapper.toResponse(task);
    }

    @Transactional
    public void delete(Long taskId) {
        Task task = getTaskOrThrow(taskId);
        ProjectMember member = accessService.requireMembership(task.getProject().getId());

        User current = member.getUser();
        boolean isReporter = task.getReporter().getId().equals(current.getId());
        boolean isOwnerOrAdmin = member.getProjectRole() == dev.portfolio.taskflow.enums.ProjectRole.OWNER
                || member.getProjectRole() == dev.portfolio.taskflow.enums.ProjectRole.ADMIN;

        if (!isReporter && !isOwnerOrAdmin) {
            throw new dev.portfolio.taskflow.exception.AccessDeniedException(
                    "Only reporter or project admin can delete this task");
        }

        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    public List<TaskHistoryResponse> getHistory(Long taskId) {
        Task task = getTaskOrThrow(taskId);
        accessService.requireMembership(task.getProject().getId());
        return taskHistoryRepository.findAllByTaskIdOrderByChangedAtDesc(taskId)
                .stream().map(taskMapper::toHistoryResponse).toList();
    }

    private Task getTaskOrThrow(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
    }

    private User requireProjectMember(Long projectId, Long userId) {
        accessService.requireMembershipForUser(projectId, userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private Set<Label> loadLabelsForProject(Long projectId, Set<Long> labelIds) {
        List<Label> labels = labelRepository.findAllById(labelIds);
        for (Label label : labels) {
            if (!label.getProject().getId().equals(projectId)) {
                throw new dev.portfolio.taskflow.exception.BusinessException(
                        "Label " + label.getId() + " does not belong to this project");
            }
        }
        if (labels.size() != labelIds.size()) {
            throw new ResourceNotFoundException("Some labels not found");
        }
        return new HashSet<>(labels);
    }
}