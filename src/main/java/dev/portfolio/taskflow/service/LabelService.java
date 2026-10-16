package dev.portfolio.taskflow.service;

import dev.portfolio.taskflow.dto.request.LabelCreateRequest;
import dev.portfolio.taskflow.dto.response.LabelResponse;
import dev.portfolio.taskflow.exception.BusinessException;
import dev.portfolio.taskflow.mapper.LabelMapper;
import dev.portfolio.taskflow.model.Label;
import dev.portfolio.taskflow.model.Project;
import dev.portfolio.taskflow.repository.LabelRepository;
import dev.portfolio.taskflow.security.ProjectAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LabelService {

    private final LabelRepository labelRepository;
    private final LabelMapper labelMapper;
    private final ProjectAccessService accessService;

    @Transactional
    public LabelResponse create(Long projectId, LabelCreateRequest request) {
        var member = accessService.requireAdminAccess(projectId);
        Project project = member.getProject();

        boolean exists = labelRepository.findAllByProjectId(projectId).stream()
                .anyMatch(l -> l.getName().equalsIgnoreCase(request.name()));
        if (exists) {
            throw new BusinessException("Label with this name already exists in the project");
        }

        Label label = Label.builder()
                .project(project)
                .name(request.name())
                .color(request.color() != null ? request.color() : "#CCCCCC")
                .build();

        return labelMapper.toResponse(labelRepository.save(label));
    }

    @Transactional(readOnly = true)
    public List<LabelResponse> getAll(Long projectId) {
        accessService.requireMembership(projectId);
        return labelRepository.findAllByProjectId(projectId)
                .stream().map(labelMapper::toResponse).toList();
    }

    @Transactional
    public void delete(Long projectId, Long labelId) {
        accessService.requireAdminAccess(projectId);
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new dev.portfolio.taskflow.exception.ResourceNotFoundException(
                        "Label not found"));
        if (!label.getProject().getId().equals(projectId)) {
            throw new BusinessException("Label does not belong to this project");
        }
        labelRepository.delete(label);
    }
}