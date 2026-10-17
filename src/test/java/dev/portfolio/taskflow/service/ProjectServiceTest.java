package dev.portfolio.taskflow.service;

import dev.portfolio.taskflow.dto.request.ProjectCreateRequest;
import dev.portfolio.taskflow.dto.response.ProjectResponse;
import dev.portfolio.taskflow.enums.ProjectRole;
import dev.portfolio.taskflow.exception.AccessDeniedException;
import dev.portfolio.taskflow.model.Project;
import dev.portfolio.taskflow.model.User;
import dev.portfolio.taskflow.repository.ProjectMemberRepository;
import dev.portfolio.taskflow.support.BaseIntegrationTest;
import dev.portfolio.taskflow.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.*;

class ProjectServiceTest extends BaseIntegrationTest {

    @Autowired ProjectService projectService;
    @Autowired ProjectMemberRepository projectMemberRepository;
    @Autowired TestDataFactory factory;

    @Test
    void create_shouldCreateProjectAndMakeUserOwner() {
        User user = factory.createRandomUser();
        authenticateAs(user);

        ProjectResponse response = projectService.create(
                new ProjectCreateRequest("My Project", "desc"));

        assertThat(response.name()).isEqualTo("My Project");
        assertThat(response.owner().id()).isEqualTo(user.getId());
        assertThat(projectMemberRepository.existsByProjectIdAndUserId(
                response.id(), user.getId())).isTrue();
    }

    @Test
    void getById_asNonMember_shouldThrowAccessDenied() {
        User owner = factory.createRandomUser();
        User stranger = factory.createRandomUser();
        Project project = factory.createProject(owner);

        authenticateAs(stranger);

        assertThatThrownBy(() -> projectService.getById(project.getId()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getById_asMember_shouldSucceed() {
        User owner = factory.createRandomUser();
        User member = factory.createRandomUser();
        Project project = factory.createProject(owner);
        factory.addMember(project, member, ProjectRole.MEMBER);

        authenticateAs(member);

        ProjectResponse response = projectService.getById(project.getId());
        assertThat(response.id()).isEqualTo(project.getId());
    }

    @Test
    void delete_asNonOwner_shouldThrow() {
        User owner = factory.createRandomUser();
        User admin = factory.createRandomUser();
        Project project = factory.createProject(owner);
        factory.addMember(project, admin, ProjectRole.ADMIN);

        authenticateAs(admin);

        assertThatThrownBy(() -> projectService.delete(project.getId()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("OWNER");
    }

    @Test
    void getAllForCurrentUser_returnsOnlyAccessibleProjects() {
        User alice = factory.createRandomUser();
        User bob = factory.createRandomUser();
        factory.createProject(alice);
        factory.createProject(bob);

        authenticateAs(alice);

        var page = projectService.getAllForCurrentUser(
                org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(page.content()).hasSize(1);
        assertThat(page.content().get(0).owner().id()).isEqualTo(alice.getId());
    }

    private void authenticateAs(User user) {
        var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}