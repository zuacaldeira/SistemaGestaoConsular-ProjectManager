package ao.gov.sgcd.pm.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class StakeholderDashboardDTO {
    private String projectName;
    private String client;
    private Double overallProgress;
    private Integer totalSessions;
    private Integer completedSessions;
    private Integer totalHoursPlanned;
    private BigDecimal totalHoursSpent;
    private LocalDate startDate;
    private LocalDate targetDate;
    private Long daysRemaining;
    private List<StakeholderSprintDTO> sprints;
    private List<MilestoneDTO> milestones;
    private WeeklyActivityDTO weeklyActivity;
    private BudgetSummaryDTO budget;
    private LocalDateTime lastUpdated;

    @Data
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class StakeholderSprintDTO {
        private Integer number;
        private String name;
        private String nameEn;
        private Double progress;
        private String status;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer sessions;
        private Integer completedSessions;
        private Integer hours;
        private BigDecimal hoursSpent;
        private String color;
        private String focus;
    }

    @Data
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class MilestoneDTO {
        private String name;
        private LocalDate targetDate;
        private String status;
    }

    @Data
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class WeeklyActivityDTO {
        private Integer sessionsThisWeek;
        private BigDecimal hoursThisWeek;
        private Integer tasksCompletedThisWeek;
    }

    @Data
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class BudgetSummaryDTO {
        private BigDecimal totalBudget;
        private BigDecimal totalSpent;
        private BigDecimal remaining;
        private Double budgetUsedPercent;
        private String currency;
    }
}
