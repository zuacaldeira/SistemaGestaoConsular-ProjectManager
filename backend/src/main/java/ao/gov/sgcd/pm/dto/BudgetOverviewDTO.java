package ao.gov.sgcd.pm.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetOverviewDTO {

    // Project totals
    private BigDecimal totalBudget;
    private BigDecimal totalSpent;
    private BigDecimal remaining;
    private BigDecimal burnRatePerWeek;
    private BigDecimal projectedTotal;
    private BigDecimal projectedVariance; // positive = under budget, negative = over budget
    private String currency;
    private BigDecimal hourlyRate;
    private BigDecimal contingencyPercent;
    private Double budgetUsedPercent;

    // ROI indicators
    private BigDecimal costPerSession;
    private BigDecimal costPerHour;

    // Timing
    private Long weeksElapsed;
    private Long weeksRemaining;

    // Sprint breakdown
    private List<SprintBudgetDTO> sprints;
}
