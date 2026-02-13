package ao.gov.sgcd.pm.repository;

import ao.gov.sgcd.pm.entity.ProjectBudget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectBudgetRepository extends JpaRepository<ProjectBudget, Long> {
    Optional<ProjectBudget> findFirstByOrderByIdAsc();
}
