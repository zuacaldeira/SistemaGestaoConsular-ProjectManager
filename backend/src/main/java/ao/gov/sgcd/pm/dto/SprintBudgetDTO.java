package ao.gov.sgcd.pm.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SprintBudgetDTO {
    private Integer sprintNumber;
    private String sprintName;
    private String color;
    private String status;
    private Integer plannedHours;
    private BigDecimal actualHours;
    private BigDecimal plannedCost;
    private BigDecimal actualCost;
    private BigDecimal variance; // planned - actual (positive = under budget)
}
