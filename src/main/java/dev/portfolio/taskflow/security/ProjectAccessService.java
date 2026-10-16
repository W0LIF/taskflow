package dev.portfolio.taskflow.security;

import dev.portfolio.taskflow.enums.ProjectRole;
import dev.portfolio.taskflow.exception.AccessDeniedException;
import dev.portfolio.taskflow.exception.ResourceNotFoundException;
import dev.portfolio.taskflow.model.Project;
import dev.portfolio.taskflow.model.ProjectMember;
import dev.portfolio.taskflow.model.User;
import dev.portfolio.taskflow.repository.ProjectMemberRepository;
import dev.portfolio.taskflow.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectAccessService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        throw new AccessDeniedException("Authentication required");
    }

    public Project getProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
    }

    public ProjectMember requireMembership(Long projectId) {
        User current = getCurrentUser();
        Project project = getProjectOrThrow(projectId);

        if (project.getOwner().getId().equals(current.getId())) {
            return ProjectMember.builder()
                    .project(project)
                    .user(current)
                    .projectRole(ProjectRole.OWNER)
                    .build();
        }

        return projectMemberRepository.findByProjectIdAndUserId(projectId, current.getId())
                .orElseThrow(() -> new AccessDeniedException(
                        "You are not a member of project " + projectId));
    }

    public ProjectMember requireAdminAccess(Long projectId) {
        ProjectMember member = requireMembership(projectId);
        if (member.getProjectRole() != ProjectRole.OWNER
                && member.getProjectRole() != ProjectRole.ADMIN) {
            throw new AccessDeniedException("Only project OWNER or ADMIN can perform this action");
        }
        return member;
    }

    public ProjectMember requireOwnerAccess(Long projectId) {
        ProjectMember member = requireMembership(projectId);
        if (member.getProjectRole() != ProjectRole.OWNER) {
            throw new AccessDeniedException("Only project OWNER can perform this action");
        }
        return member;
    }

    public void requireMembershipForUser(Long projectId, Long userId) {
        Project project = getProjectOrThrow(projectId);
        if (project.getOwner().getId().equals(userId)) return;
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new dev.portfolio.taskflow.exception.BusinessException(
                    "User " + userId + " is not a member of project " + projectId);
        }
    }
}