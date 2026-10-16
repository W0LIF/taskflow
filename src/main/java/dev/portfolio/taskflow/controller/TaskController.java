package dev.portfolio.taskflow.controller;

import dev.portfolio.taskflow.dto.request.TaskCreateRequest;
import dev.portfolio.taskflow.dto.request.TaskUpdateRequest;
import dev.portfolio.taskflow.dto.response.PageResponse;
import dev.portfolio.taskflow.dto.response.TaskHistoryResponse;
import dev.portfolio.taskflow.dto.response.TaskResponse;
import dev.portfolio.taskflow.enums.TaskStatus;
import dev.portfolio.taskflow.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> create(
            @PathVariable Long projectId,
            @Valid @RequestBody TaskCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(projectId, request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<TaskResponse>> getAll(
            @PathVariable Long projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Long assigneeId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(taskService.getByProject(projectId, status, assigneeId, pageable));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getById(@PathVariable Long projectId,
                                                @PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getById(taskId));
    }

    @PatchMapping("/{taskId}")
    public ResponseEntity<TaskResponse> update(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskUpdateRequest request) {
        return ResponseEntity.ok(taskService.update(taskId, request));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> delete(@PathVariable Long projectId,
                                       @PathVariable Long taskId) {
        taskService.delete(taskId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{taskId}/history")
    public ResponseEntity<List<TaskHistoryResponse>> getHistory(@PathVariable Long projectId,
                                                                @PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getHistory(taskId));
    }
}