package dev.portfolio.taskflow.service;

import dev.portfolio.taskflow.dto.request.TaskCreateRequest;
import dev.portfolio.taskflow.dto.request.TaskUpdateRequest;
import dev.portfolio.taskflow.dto.response.TaskHistoryResponse;
import dev.portfolio.taskflow.dto.response.TaskResponse;
import dev.portfolio.taskflow.enums.TaskPriority;
import dev.portfolio.taskflow.enums.TaskStatus;
import dev.portfolio.taskflow.exception.AccessDeniedException;
import dev.portfolio.taskflow.model.Project;
import dev.portfolio.taskflow.model.Task;
import dev.portfolio.taskflow.model.User;
import dev.portfolio.taskflow.support.BaseIntegrationTest;
import dev.portfolio.taskflow.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class TaskServiceTest extends BaseIntegrationTest {

    @Autowired TaskService taskService;
    @Autowired TestDataFactory factory;

    @Test
    void create_shouldSetReporterAndDefaults() {
        User user = factory.createRandomUser();
        Project project = factory.createProject(user);
        authenticateAs(user);

        TaskResponse response = taskService.create(project.getId(),
                new TaskCreateRequest("Implement login", "desc", null, null, null, Set.of()));

        assertThat(response.reporter().id()).isEqualTo(user.getId());
        assertThat(response.status()).isEqualTo(TaskStatus.TODO);
        assertThat(response.priority()).isEqualTo(TaskPriority.MEDIUM);
    }

    @Test
    void update_status_writesHistory() {
        User user = factory.createRandomUser();
        Project project = factory.createProject(user);
        Task task = factory.createTask(project, user, "task");
        authenticateAs(user);

        taskService.update(task.getId(),
                new TaskUpdateRequest(null, null, TaskStatus.IN_PROGRESS, null, null, null));

        List<TaskHistoryResponse> history = taskService.getHistory(task.getId());

        assertThat(history).hasSize(1);
        assertThat(history.get(0).fieldChanged()).isEqualTo("status");
        assertThat(history.get(0).oldValue()).isEqualTo("TODO");
        assertThat(history.get(0).newValue()).isEqualTo("IN_PROGRESS");
        assertThat(history.get(0).user().id()).isEqualTo(user.getId());
    }

    @Test
    void update_sameStatus_doesNotWriteHistory() {
        User user = factory.createRandomUser();
        Project project = factory.createProject(user);
        Task task = factory.createTask(project, user, "task");
        authenticateAs(user);

        taskService.update(task.getId(),
                new TaskUpdateRequest(null, null, TaskStatus.TODO, null, null, null));

        assertThat(taskService.getHistory(task.getId())).isEmpty();
    }

    @Test
    void update_multipleFields_writesOneHistoryRecordPerChange() {
        User user = factory.createRandomUser();
        Project project = factory.createProject(user);
        Task task = factory.createTask(project, user, "task");
        authenticateAs(user);

        taskService.update(task.getId(),
                new TaskUpdateRequest("new title", null, TaskStatus.DONE, TaskPriority.HIGH, null, null));

        List<TaskHistoryResponse> history = taskService.getHistory(task.getId());
        assertThat(history)
                .extracting(TaskHistoryResponse::fieldChanged)
                .containsExactlyInAnyOrder("title", "status", "priority");
    }

    @Test
    void update_assigneeMustBeProjectMember() {
        User owner = factory.createRandomUser();
        User stranger = factory.createRandomUser();
        Project project = factory.createProject(owner);
        Task task = factory.createTask(project, owner, "task");
        authenticateAs(owner);

        assertThatThrownBy(() -> taskService.update(task.getId(),
                new TaskUpdateRequest(null, null, null, null, stranger.getId(), null)))
                .isInstanceOf(dev.portfolio.taskflow.exception.BusinessException.class)
                .hasMessageContaining("not a member");
    }

    @Test
    void delete_onlyReporterOrAdminCanDelete() {
        User owner = factory.createRandomUser();
        User member = factory.createRandomUser();
        Project project = factory.createProject(owner);
        factory.addMember(project, member, dev.portfolio.taskflow.enums.ProjectRole.MEMBER);
        Task task = factory.createTask(project, owner, "task");

        authenticateAs(member);

        assertThatThrownBy(() -> taskService.delete(task.getId()))
                .isInstanceOf(AccessDeniedException.class);
    }

    private void authenticateAs(User user) {
        var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}