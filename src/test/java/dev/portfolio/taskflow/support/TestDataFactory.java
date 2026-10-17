package dev.portfolio.taskflow.support;

import dev.portfolio.taskflow.enums.ProjectRole;
import dev.portfolio.taskflow.enums.Role;
import dev.portfolio.taskflow.model.Project;
import dev.portfolio.taskflow.model.ProjectMember;
import dev.portfolio.taskflow.model.Task;
import dev.portfolio.taskflow.model.User;
import dev.portfolio.taskflow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TestDataFactory {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(String username) {
        User user = User.builder()
                .username(username)
                .email(username + "@test.com")
                .password(passwordEncoder.encode("password"))
                .fullName("Test " + username)
                .role(Role.USER)
                .build();
        return userRepository.save(user);
    }

    public User createRandomUser() {
        String name = "user_" + UUID.randomUUID().toString().substring(0, 8);
        return createUser(name);
    }

    public Project createProject(User owner) {
        Project project = Project.builder()
                .name("Project of " + owner.getUsername())
                .description("Test project")
                .owner(owner)
                .build();
        projectRepository.save(project);

        projectMemberRepository.save(ProjectMember.builder()
                .project(project)
                .user(owner)
                .projectRole(ProjectRole.OWNER)
                .build());

        return project;
    }

    public ProjectMember addMember(Project project, User user, ProjectRole role) {
        return projectMemberRepository.save(ProjectMember.builder()
                .project(project)
                .user(user)
                .projectRole(role)
                .build());
    }

    public Task createTask(Project project, User reporter, String title) {
        return taskRepository.save(Task.builder()
                .project(project)
                .title(title)
                .reporter(reporter)
                .build());
    }
}