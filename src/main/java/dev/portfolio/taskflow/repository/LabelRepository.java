package dev.portfolio.taskflow.repository;

import dev.portfolio.taskflow.model.Label;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabelRepository extends JpaRepository<Label, Long> {
    List<Label> findAllByProjectId(Long projectId);
}