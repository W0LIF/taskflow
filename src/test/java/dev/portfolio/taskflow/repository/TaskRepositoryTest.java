package dev.portfolio.taskflow.repository;

import dev.portfolio.taskflow.enums.TaskPriority;
import dev.portfolio.taskflow.enums.TaskStatus;
import dev.portfolio.taskflow.model.Project;
import dev.portfolio.taskflow.model.Task;
import dev.portfolio.taskflow.model.User;
import dev.portfolio.taskflow.support.BaseIntegrationTest;
import dev.portfolio.taskflow.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TaskRepositoryTest extends BaseIntegrationTest {

    @Autowired TaskRepository taskRepository;
    @Autowired TestDataFactory factory;

    @Test
    void findWithFilters_byStatus_returnsOnlyMatching() {
        User user = factory.createRandomUser();
        Project project = factory.createProject(user);
        Task t1 = factory.createTask(project, user, "A");
        Task t2 = factory.createTask(project, user, "B");
        t1.setStatus(TaskStatus.DONE);
        t2.setStatus(TaskStatus.TODO);
        taskRepository.save(t1);
        taskRepository.save(t2);

        Page<Task> page = taskRepository.findWithFilters(
                project.getId(), TaskStatus.DONE, null, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("A");
    }

    @Test
    void findWithFilters_paginationAndSorting() {
        User user = factory.createRandomUser();
        Project project = factory.createProject(user);
        for (int i = 0; i < 5; i++) {
            Task t = factory.createTask(project, user, "task-" + i);
            t.setPriority(i % 2 == 0 ? TaskPriority.HIGH : TaskPriority.LOW);
            taskRepository.save(t);
        }

        Page<Task> page = taskRepository.findWithFilters(
                project.getId(), null, null,
                PageRequest.of(0, 2, Sort.by("priority").descending()));

        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    void findTasksWithUpcomingDeadline_respectsWindow() {
        User user = factory.createRandomUser();
        Project project = factory.createProject(user);

        Task soon = factory.createTask(project, user, "soon");
        soon.setAssignee(user);
        soon.setDueDate(LocalDateTime.now().plusHours(12));
        taskRepository.save(soon);

        Task far = factory.createTask(project, user, "far");
        far.setAssignee(user);
        far.setDueDate(LocalDateTime.now().plusDays(5));
        taskRepository.save(far);

        Task done = factory.createTask(project, user, "done");
        done.setAssignee(user);
        done.setStatus(TaskStatus.DONE);
        done.setDueDate(LocalDateTime.now().plusHours(6));
        taskRepository.save(done);

        var result = taskRepository.findTasksWithUpcomingDeadline(
                LocalDateTime.now(), LocalDateTime.now().plusHours(24));

        assertThat(result).extracting(Task::getTitle).containsExactly("soon");
    }
}