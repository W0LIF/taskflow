package dev.portfolio.taskflow.service;

import dev.portfolio.taskflow.dto.request.AddMemberRequest;
import dev.portfolio.taskflow.dto.request.ProjectCreateRequest;
import dev.portfolio.taskflow.dto.request.ProjectUpdateRequest;
import dev.portfolio.taskflow.dto.response.PageResponse;
import dev.portfolio.taskflow.dto.response.ProjectResponse;
import dev.portfolio.taskflow.enums.ProjectRole;
import dev.portfolio.taskflow.exception.BusinessException;
import dev.portfolio.taskflow.exception.ResourceNotFoundException;
import dev.portfolio.taskflow.mapper.ProjectMapper;
import dev.portfolio.taskflow.model.Project;
import dev.portfolio.taskflow.model.ProjectMember;
import dev.portfolio.taskflow.model.User;
import dev.portfolio.taskflow.repository.ProjectMemberRepository;
import dev.portfolio.taskflow.repository.ProjectRepository;
import dev.portfolio.taskflow.repository.UserRepository;
import dev.portfolio.taskflow.security.ProjectAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;
    private final ProjectAccessService accessService;

    @Transactional
    public ProjectResponse create(ProjectCreateRequest request) {
        User current = accessService.getCurrentUser();

        Project project = Project.builder()
                .name(request.name())
                .description(request.description())
                .owner(current)
                .build();

        projectRepository.save(project);

        ProjectMember ownerMembership = ProjectMember.builder()
                .project(project)
                .user(current)
                .projectRole(ProjectRole.OWNER)
                .build();
        projectMemberRepository.save(ownerMembership);

        return projectMapper.toResponse(project);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getById(Long projectId) {
        accessService.requireMembership(projectId);
        Project project = accessService.getProjectOrThrow(projectId);
        return projectMapper.toResponse(project);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectResponse> getAllForCurrentUser(Pageable pageable) {
        User current = accessService.getCurrentUser();
        Page<Project> page = projectRepository.findAllAccessibleByUser(current.getId(), pageable);
        return PageResponse.of(page, projectMapper::toResponse);
    }

    @Transactional
    public ProjectResponse update(Long projectId, ProjectUpdateRequest request) {
        accessService.requireAdminAccess(projectId);
        Project project = accessService.getProjectOrThrow(projectId);

        if (request.name() != null) project.setName(request.name());
        if (request.description() != null) project.setDescription(request.description());

        return projectMapper.toResponse(project);
    }

    @Transactional
    public void delete(Long projectId) {
        accessService.requireOwnerAccess(projectId);
        projectRepository.deleteById(projectId);
    }

    @Transactional
    public ProjectResponse addMember(Long projectId, AddMemberRequest request) {
        accessService.requireAdminAccess(projectId);
        Project project = accessService.getProjectOrThrow(projectId);

        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, request.userId())) {
            throw new BusinessException("User is already a member of this project");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.userId()));

        if (request.projectRole() == ProjectRole.OWNER) {
            throw new BusinessException("Cannot assign OWNER role; transfer ownership instead");
        }

        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(user)
                .projectRole(request.projectRole())
                .build();
        projectMemberRepository.save(member);

        project.getMembers().add(member);
        return projectMapper.toResponse(project);
    }

    @Transactional
    public void removeMember(Long projectId, Long userId) {
        ProjectMember requester = accessService.requireAdminAccess(projectId);
        Project project = accessService.getProjectOrThrow(projectId);

        if (project.getOwner().getId().equals(userId)) {
            throw new BusinessException("Cannot remove project owner");
        }

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        if (member.getProjectRole() == ProjectRole.ADMIN
                && requester.getProjectRole() != ProjectRole.OWNER) {
            throw new dev.portfolio.taskflow.exception.AccessDeniedException(
                    "Only OWNER can remove ADMIN members");
        }

        projectMemberRepository.delete(member);
    }
}