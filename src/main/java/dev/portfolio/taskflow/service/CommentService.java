package dev.portfolio.taskflow.service;

import dev.portfolio.taskflow.dto.request.CommentCreateRequest;
import dev.portfolio.taskflow.dto.response.CommentResponse;
import dev.portfolio.taskflow.exception.AccessDeniedException;
import dev.portfolio.taskflow.exception.ResourceNotFoundException;
import dev.portfolio.taskflow.mapper.CommentMapper;
import dev.portfolio.taskflow.model.Comment;
import dev.portfolio.taskflow.model.Task;
import dev.portfolio.taskflow.model.User;
import dev.portfolio.taskflow.repository.CommentRepository;
import dev.portfolio.taskflow.repository.TaskRepository;
import dev.portfolio.taskflow.security.ProjectAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final CommentMapper commentMapper;
    private final ProjectAccessService accessService;

    @Transactional
    public CommentResponse create(Long taskId, CommentCreateRequest request) {
        Task task = getTaskOrThrow(taskId);
        User current = accessService.requireMembership(task.getProject().getId()).getUser();

        Comment comment = Comment.builder()
                .task(task)
                .author(current)
                .content(request.content())
                .build();

        return commentMapper.toResponse(commentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getAll(Long taskId) {
        Task task = getTaskOrThrow(taskId);
        accessService.requireMembership(task.getProject().getId());
        return commentRepository.findAllByTaskIdOrderByCreatedAtAsc(taskId)
                .stream().map(commentMapper::toResponse).toList();
    }

    @Transactional
    public void delete(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        User current = accessService.getCurrentUser();

        if (!comment.getAuthor().getId().equals(current.getId())) {
            throw new AccessDeniedException("Only the author can delete this comment");
        }
        commentRepository.delete(comment);
    }

    private Task getTaskOrThrow(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
    }
}