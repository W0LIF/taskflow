package dev.portfolio.taskflow.controller;

import dev.portfolio.taskflow.dto.request.LabelCreateRequest;
import dev.portfolio.taskflow.dto.response.LabelResponse;
import dev.portfolio.taskflow.service.LabelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/labels")
@RequiredArgsConstructor
public class LabelController {

    private final LabelService labelService;

    @PostMapping
    public ResponseEntity<LabelResponse> create(@PathVariable Long projectId,
                                                @Valid @RequestBody LabelCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(labelService.create(projectId, request));
    }

    @GetMapping
    public ResponseEntity<List<LabelResponse>> getAll(@PathVariable Long projectId) {
        return ResponseEntity.ok(labelService.getAll(projectId));
    }

    @DeleteMapping("/{labelId}")
    public ResponseEntity<Void> delete(@PathVariable Long projectId,
                                       @PathVariable Long labelId) {
        labelService.delete(projectId, labelId);
        return ResponseEntity.noContent().build();
    }
}