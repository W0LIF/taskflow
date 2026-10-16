package dev.portfolio.taskflow.repository;

import dev.portfolio.taskflow.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("""
        SELECT DISTINCT p FROM Project p
        LEFT JOIN p.members m
        WHERE p.owner.id = :userId OR m.user.id = :userId
        """)
    Page<Project> findAllAccessibleByUser(@Param("userId") Long userId, Pageable pageable);
}