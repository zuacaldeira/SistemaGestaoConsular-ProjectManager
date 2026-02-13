package ao.gov.sgcd.pm.repository;

import ao.gov.sgcd.pm.entity.SprintBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SprintBudgetRepository extends JpaRepository<SprintBudget, Long> {

    Optional<SprintBudget> findBySprintId(Long sprintId);

    @Query("SELECT sb FROM SprintBudget sb JOIN FETCH sb.sprint s ORDER BY s.sprintNumber ASC")
    List<SprintBudget> findAllWithSprint();
}
