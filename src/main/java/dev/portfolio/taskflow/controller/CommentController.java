package dev.portfolio.taskflow.controller;

import dev.portfolio.taskflow.dto.request.CommentCreateRequest;
import dev.portfolio.taskflow.dto.response.CommentResponse;
import dev.portfolio.taskflow.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> create(@PathVariable Long taskId,
                                                  @Valid @RequestBody CommentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.create(taskId, request));
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getAll(@PathVariable Long taskId) {
        return ResponseEntity.ok(commentService.getAll(taskId));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable Long taskId,
                                       @PathVariable Long commentId) {
        commentService.delete(commentId);
        return ResponseEntity.noContent().build();
    }
}