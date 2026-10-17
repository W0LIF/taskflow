package dev.portfolio.taskflow.service;

import dev.portfolio.taskflow.enums.TaskStatus;
import dev.portfolio.taskflow.model.Project;
import dev.portfolio.taskflow.model.Task;
import dev.portfolio.taskflow.model.User;
import dev.portfolio.taskflow.repository.TaskRepository;
import dev.portfolio.taskflow.support.BaseIntegrationTest;
import dev.portfolio.taskflow.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class TaskConcurrencyTest extends BaseIntegrationTest {

    @Autowired TaskRepository taskRepository;
    @Autowired TransactionTemplate transactionTemplate;
    @Autowired TestDataFactory factory;

    @Test
    void twoConcurrentUpdates_onlyOneSucceeds() throws Exception {
        User user = factory.createRandomUser();
        Project project = factory.createProject(user);
        Task task = factory.createTask(project, user, "concurrent");
        Long taskId = task.getId();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(2);

        AtomicInteger successes = new AtomicInteger();
        AtomicInteger optimisticFailures = new AtomicInteger();

        Function<TaskStatus, Runnable> updateTask = newStatus -> () -> {
            try {
                startSignal.await();
                transactionTemplate.executeWithoutResult(status -> {
                    Task t = taskRepository.findById(taskId).orElseThrow();
                    t.setStatus(newStatus);
                    taskRepository.saveAndFlush(t);
                });
                successes.incrementAndGet();
            } catch (ObjectOptimisticLockingFailureException e) {
                optimisticFailures.incrementAndGet();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } finally {
                doneSignal.countDown();
            }
        };

        executor.submit(updateTask.apply(TaskStatus.IN_PROGRESS));
        executor.submit(updateTask.apply(TaskStatus.DONE));

        startSignal.countDown();
        boolean finished = doneSignal.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(finished).as("Both threads must finish in 10s").isTrue();
        assertThat(successes.get())
                .as("Exactly one transaction should commit")
                .isEqualTo(1);
        assertThat(optimisticFailures.get())
                .as("The other should fail with optimistic lock")
                .isEqualTo(1);

        Task finalTask = taskRepository.findById(taskId).orElseThrow();
        assertThat(finalTask.getStatus()).isIn(TaskStatus.IN_PROGRESS, TaskStatus.DONE);
        assertThat(finalTask.getVersion()).isEqualTo(1L);
    }
}