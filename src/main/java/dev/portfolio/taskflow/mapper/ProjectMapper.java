package dev.portfolio.taskflow.mapper;

import dev.portfolio.taskflow.dto.response.ProjectMemberResponse;
import dev.portfolio.taskflow.dto.response.ProjectResponse;
import dev.portfolio.taskflow.model.Project;
import dev.portfolio.taskflow.model.ProjectMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProjectMapper {
    private final UserMapper userMapper;

    public ProjectResponse toResponse(Project project){
        List<ProjectMemberResponse> members = project.getMembers().stream()
                .map(this::toMemberResponse)
                .toList();

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                userMapper.toSummary(project.getOwner()),
                members,
                project.getTasks().size(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    public ProjectMemberResponse toMemberResponse(ProjectMember member) {
        return new ProjectMemberResponse(
                member.getId(),
                userMapper.toSummary(member.getUser()),
                member.getProjectRole(),
                member.getJoinedAt()
        );
    }
}
