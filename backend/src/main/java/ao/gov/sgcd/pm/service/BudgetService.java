package ao.gov.sgcd.pm.service;

import ao.gov.sgcd.pm.dto.BudgetOverviewDTO;
import ao.gov.sgcd.pm.dto.BudgetUpdateDTO;
import ao.gov.sgcd.pm.dto.SprintBudgetDTO;
import ao.gov.sgcd.pm.entity.ProjectBudget;
import ao.gov.sgcd.pm.entity.SprintBudget;
import ao.gov.sgcd.pm.exception.ResourceNotFoundException;
import ao.gov.sgcd.pm.repository.ProjectBudgetRepository;
import ao.gov.sgcd.pm.repository.SprintBudgetRepository;
import ao.gov.sgcd.pm.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetService {

    private final ProjectBudgetRepository projectBudgetRepository;
    private final SprintBudgetRepository sprintBudgetRepository;
    private final TaskRepository taskRepository;
    private final ProjectConfigService projectConfigService;

    public BudgetOverviewDTO getBudgetOverview() {
        ProjectBudget budget = projectBudgetRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new ResourceNotFoundException("ProjectBudget not found"));

        BigDecimal hourlyRate = budget.getHourlyRate();
        BigDecimal totalHoursSpent = taskRepository.sumActualHoursCompleted();
        BigDecimal totalSpent = totalHoursSpent.multiply(hourlyRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal remaining = budget.getTotalBudget().subtract(totalSpent);
        double budgetUsedPercent = budget.getTotalBudget().compareTo(BigDecimal.ZERO) > 0
                ? totalSpent.divide(budget.getTotalBudget(), 4, RoundingMode.HALF_UP).doubleValue() * 100
                : 0;

        // Timing
        LocalDate startDate = projectConfigService.getStartDate();
        LocalDate targetDate = projectConfigService.getTargetDate();
        LocalDate today = LocalDate.now();
        long weeksElapsed = Math.max(1, ChronoUnit.WEEKS.between(startDate, today));
        long totalWeeks = Math.max(1, ChronoUnit.WEEKS.between(startDate, targetDate));
        long weeksRemaining = Math.max(0, totalWeeks - weeksElapsed);

        // Burn rate and projection
        BigDecimal burnRatePerWeek = totalSpent.divide(BigDecimal.valueOf(weeksElapsed), 2, RoundingMode.HALF_UP);
        BigDecimal projectedTotal = burnRatePerWeek.multiply(BigDecimal.valueOf(totalWeeks)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal projectedVariance = budget.getTotalBudget().subtract(projectedTotal);

        // ROI indicators
        int completedSessions = taskRepository.countByStatus(ao.gov.sgcd.pm.entity.TaskStatus.COMPLETED);
        BigDecimal costPerSession = completedSessions > 0
                ? totalSpent.divide(BigDecimal.valueOf(completedSessions), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal costPerHour = totalHoursSpent.compareTo(BigDecimal.ZERO) > 0
                ? totalSpent.divide(totalHoursSpent, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Sprint breakdown
        List<SprintBudget> sprintBudgets = sprintBudgetRepository.findAllWithSprint();
        List<SprintBudgetDTO> sprintDTOs = sprintBudgets.stream()
                .map(sb -> {
                    BigDecimal actualHours = sb.getSprint().getActualHours() != null
                            ? sb.getSprint().getActualHours() : BigDecimal.ZERO;
                    BigDecimal actualCost = actualHours.multiply(hourlyRate).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal variance = sb.getPlannedCost().subtract(actualCost);

                    return SprintBudgetDTO.builder()
                            .sprintNumber(sb.getSprint().getSprintNumber())
                            .sprintName(sb.getSprint().getName())
                            .color(sb.getSprint().getColor())
                            .status(sb.getSprint().getStatus().name())
                            .plannedHours(sb.getSprint().getTotalHours())
                            .actualHours(actualHours)
                            .plannedCost(sb.getPlannedCost())
                            .actualCost(actualCost)
                            .variance(variance)
                            .build();
                })
                .toList();

        return BudgetOverviewDTO.builder()
                .totalBudget(budget.getTotalBudget())
                .totalSpent(totalSpent)
                .remaining(remaining)
                .burnRatePerWeek(burnRatePerWeek)
                .projectedTotal(projectedTotal)
                .projectedVariance(projectedVariance)
                .currency(budget.getCurrency())
                .hourlyRate(hourlyRate)
                .contingencyPercent(budget.getContingencyPercent())
                .budgetUsedPercent(Math.round(budgetUsedPercent * 10) / 10.0)
                .costPerSession(costPerSession)
                .costPerHour(costPerHour)
                .weeksElapsed(weeksElapsed)
                .weeksRemaining(weeksRemaining)
                .sprints(sprintDTOs)
                .build();
    }

    @Transactional
    public BudgetOverviewDTO updateBudget(BudgetUpdateDTO dto) {
        ProjectBudget budget = projectBudgetRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new ResourceNotFoundException("ProjectBudget not found"));

        budget.setHourlyRate(dto.getHourlyRate());
        budget.setTotalBudget(dto.getTotalBudget());
        budget.setCurrency(dto.getCurrency());
        budget.setContingencyPercent(dto.getContingencyPercent());
        projectBudgetRepository.save(budget);

        // Recalculate sprint planned costs
        List<SprintBudget> sprintBudgets = sprintBudgetRepository.findAllWithSprint();
        for (SprintBudget sb : sprintBudgets) {
            BigDecimal plannedCost = BigDecimal.valueOf(sb.getSprint().getTotalHours())
                    .multiply(dto.getHourlyRate())
                    .setScale(2, RoundingMode.HALF_UP);
            sb.setPlannedCost(plannedCost);
        }
        sprintBudgetRepository.saveAll(sprintBudgets);

        log.info("Budget updated: rate={}€/h, total={}€", dto.getHourlyRate(), dto.getTotalBudget());
        return getBudgetOverview();
    }
}
