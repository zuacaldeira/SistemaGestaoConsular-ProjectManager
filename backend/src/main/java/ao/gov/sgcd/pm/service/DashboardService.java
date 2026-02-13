package ao.gov.sgcd.pm.service;

import ao.gov.sgcd.pm.dto.*;
import ao.gov.sgcd.pm.entity.*;
import ao.gov.sgcd.pm.mapper.*;
import ao.gov.sgcd.pm.entity.ProjectBudget;
import ao.gov.sgcd.pm.repository.BlockedDayRepository;
import ao.gov.sgcd.pm.repository.ProjectBudgetRepository;
import ao.gov.sgcd.pm.repository.SprintRepository;
import ao.gov.sgcd.pm.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SprintRepository sprintRepository;
    private final TaskRepository taskRepository;
    private final BlockedDayRepository blockedDayRepository;
    private final ProjectBudgetRepository projectBudgetRepository;
    private final ProjectConfigService projectConfigService;
    private final SprintMapper sprintMapper;
    private final TaskMapper taskMapper;
    private final BlockedDayMapper blockedDayMapper;

    public DashboardDTO getDeveloperDashboard() {
        int totalSessions = projectConfigService.getTotalSessions();
        int completedSessions = taskRepository.countByStatus(TaskStatus.COMPLETED);
        BigDecimal totalHoursSpent = taskRepository.sumActualHoursCompleted();
        double projectProgress = totalSessions > 0 ? (completedSessions * 100.0) / totalSessions : 0;

        // Active sprint
        SprintDTO activeSprint = sprintRepository.findActiveSprint()
                .map(s -> {
                    SprintDTO dto = sprintMapper.toDto(s);
                    dto.setProgressPercent(s.getTotalSessions() > 0
                            ? (s.getCompletedSessions() * 100.0) / s.getTotalSessions() : 0);
                    return dto;
                })
                .orElse(null);

        // Today's task
        TaskDTO todayTask = null;
        var todayOpt = taskRepository.findBySessionDate(LocalDate.now());
        if (todayOpt.isPresent()) {
            todayTask = taskMapper.toDto(todayOpt.get());
        } else {
            var upcoming = taskRepository.findUpcomingPlanned(LocalDate.now());
            if (!upcoming.isEmpty()) {
                todayTask = taskMapper.toDto(upcoming.get(0));
            }
        }

        // Recent tasks
        List<TaskDTO> recentTasks = taskRepository.findRecentCompleted(
                        org.springframework.data.domain.PageRequest.of(0, 5))
                .stream().map(taskMapper::toDto).toList();

        // Sprint summaries
        List<DashboardDTO.SprintSummaryDTO> sprintSummaries = sprintRepository.findAllOrdered().stream()
                .map(s -> DashboardDTO.SprintSummaryDTO.builder()
                        .sprintNumber(s.getSprintNumber())
                        .name(s.getName())
                        .progress(s.getTotalSessions() > 0
                                ? (s.getCompletedSessions() * 100.0) / s.getTotalSessions() : 0)
                        .status(s.getStatus().name())
                        .color(s.getColor())
                        .build())
                .toList();

        // Upcoming blocked days
        List<BlockedDayDTO> upcomingBlocked = blockedDayMapper.toDtoList(
                blockedDayRepository.findUpcoming(LocalDate.now()));

        // Week progress
        LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        List<Task> weekTasks = taskRepository.findByWeek(weekStart, weekEnd);
        int weekCompleted = (int) weekTasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        BigDecimal weekHoursPlanned = weekTasks.stream()
                .map(Task::getPlannedHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal weekHoursSpent = weekTasks.stream()
                .filter(t -> t.getActualHours() != null)
                .map(Task::getActualHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardDTO.builder()
                .projectProgress(projectProgress)
                .totalSessions(totalSessions)
                .completedSessions(completedSessions)
                .totalHoursPlanned(projectConfigService.getTotalHoursPlanned())
                .totalHoursSpent(totalHoursSpent)
                .activeSprint(activeSprint)
                .todayTask(todayTask)
                .recentTasks(recentTasks)
                .sprintSummaries(sprintSummaries)
                .upcomingBlockedDays(upcomingBlocked)
                .weekProgress(DashboardDTO.WeekProgressDTO.builder()
                        .weekTasks(weekTasks.size())
                        .weekCompleted(weekCompleted)
                        .weekHoursPlanned(weekHoursPlanned)
                        .weekHoursSpent(weekHoursSpent)
                        .build())
                .build();
    }

    public ProjectProgressDTO getProjectProgress() {
        int totalSessions = projectConfigService.getTotalSessions();
        int totalHoursPlanned = projectConfigService.getTotalHoursPlanned();
        LocalDate startDate = projectConfigService.getStartDate();
        LocalDate targetDate = projectConfigService.getTargetDate();
        LocalDate today = LocalDate.now();

        int completedSessions = taskRepository.countByStatus(TaskStatus.COMPLETED);
        BigDecimal totalHoursSpent = taskRepository.sumActualHoursCompleted();
        double overallProgress = totalSessions > 0 ? (completedSessions * 100.0) / totalSessions : 0;
        long daysRemaining = Math.max(0, ChronoUnit.DAYS.between(today, targetDate));

        // Task status totals
        int totalPlanned = taskRepository.countByStatus(TaskStatus.PLANNED);
        int totalInProgress = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        int totalCompleted = completedSessions;
        int totalBlocked = taskRepository.countByStatus(TaskStatus.BLOCKED);
        int totalSkipped = taskRepository.countByStatus(TaskStatus.SKIPPED);

        // Grouped counts per sprint (single query)
        List<Object[]> grouped = taskRepository.countGroupedBySprintAndStatus();
        Map<Long, Map<TaskStatus, Integer>> sprintStatusCounts = new HashMap<>();
        for (Object[] row : grouped) {
            Long sprintId = (Long) row[0];
            TaskStatus status = (TaskStatus) row[1];
            int count = ((Number) row[2]).intValue();
            sprintStatusCounts.computeIfAbsent(sprintId, k -> new EnumMap<>(TaskStatus.class)).put(status, count);
        }

        // Sprint details
        List<Sprint> sprints = sprintRepository.findAllOrdered();
        List<ProjectProgressDTO.SprintProgressDTO> sprintDetails = sprints.stream()
                .map(s -> {
                    Map<TaskStatus, Integer> counts = sprintStatusCounts.getOrDefault(s.getId(), Collections.emptyMap());
                    double progress = s.getTotalSessions() > 0
                            ? (s.getCompletedSessions() * 100.0) / s.getTotalSessions() : 0;
                    return ProjectProgressDTO.SprintProgressDTO.builder()
                            .sprintNumber(s.getSprintNumber())
                            .name(s.getName())
                            .status(s.getStatus().name())
                            .color(s.getColor())
                            .startDate(s.getStartDate())
                            .endDate(s.getEndDate())
                            .totalSessions(s.getTotalSessions())
                            .completedSessions(s.getCompletedSessions())
                            .totalHours(s.getTotalHours())
                            .actualHours(s.getActualHours())
                            .progress(progress)
                            .plannedTasks(counts.getOrDefault(TaskStatus.PLANNED, 0))
                            .inProgressTasks(counts.getOrDefault(TaskStatus.IN_PROGRESS, 0))
                            .completedTasks(counts.getOrDefault(TaskStatus.COMPLETED, 0))
                            .blockedTasks(counts.getOrDefault(TaskStatus.BLOCKED, 0))
                            .skippedTasks(counts.getOrDefault(TaskStatus.SKIPPED, 0))
                            .build();
                })
                .toList();

        // Velocity
        long weeksElapsed = Math.max(1, ChronoUnit.WEEKS.between(startDate, today));
        long totalWeeks = Math.max(1, ChronoUnit.WEEKS.between(startDate, targetDate));
        long weeksRemaining = Math.max(0, totalWeeks - weeksElapsed);
        double avgSessionsPerWeek = weeksElapsed > 0 ? (double) completedSessions / weeksElapsed : 0;
        double avgHoursPerWeek = weeksElapsed > 0
                ? totalHoursSpent.divide(BigDecimal.valueOf(weeksElapsed), 1, RoundingMode.HALF_UP).doubleValue() : 0;

        return ProjectProgressDTO.builder()
                .totalSessions(totalSessions)
                .completedSessions(completedSessions)
                .totalHoursPlanned(totalHoursPlanned)
                .totalHoursSpent(totalHoursSpent)
                .overallProgress(overallProgress)
                .daysRemaining(daysRemaining)
                .startDate(startDate)
                .targetDate(targetDate)
                .totalPlanned(totalPlanned)
                .totalInProgress(totalInProgress)
                .totalCompleted(totalCompleted)
                .totalBlocked(totalBlocked)
                .totalSkipped(totalSkipped)
                .avgSessionsPerWeek(Math.round(avgSessionsPerWeek * 10) / 10.0)
                .avgHoursPerWeek(Math.round(avgHoursPerWeek * 10) / 10.0)
                .weeksElapsed(weeksElapsed)
                .weeksRemaining(weeksRemaining)
                .sprints(sprintDetails)
                .build();
    }

    public StakeholderDashboardDTO getStakeholderDashboard() {
        int totalSessions = projectConfigService.getTotalSessions();
        int completedSessions = taskRepository.countByStatus(TaskStatus.COMPLETED);
        BigDecimal totalHoursSpent = taskRepository.sumActualHoursCompleted();
        double overallProgress = totalSessions > 0 ? (completedSessions * 100.0) / totalSessions : 0;

        LocalDate startDate = projectConfigService.getStartDate();
        LocalDate targetDate = projectConfigService.getTargetDate();
        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), targetDate);

        // Sprint details
        List<StakeholderDashboardDTO.StakeholderSprintDTO> sprints = sprintRepository.findAllOrdered().stream()
                .map(s -> StakeholderDashboardDTO.StakeholderSprintDTO.builder()
                        .number(s.getSprintNumber())
                        .name(s.getName())
                        .nameEn(s.getNameEn())
                        .progress(s.getTotalSessions() > 0
                                ? (s.getCompletedSessions() * 100.0) / s.getTotalSessions() : 0)
                        .status(s.getStatus().name())
                        .startDate(s.getStartDate())
                        .endDate(s.getEndDate())
                        .sessions(s.getTotalSessions())
                        .completedSessions(s.getCompletedSessions())
                        .hours(s.getTotalHours())
                        .hoursSpent(s.getActualHours())
                        .color(s.getColor())
                        .focus(s.getFocus())
                        .build())
                .toList();

        // Milestones
        List<StakeholderDashboardDTO.MilestoneDTO> milestones = sprintRepository.findAllOrdered().stream()
                .map(s -> {
                    String milestoneStatus = switch (s.getStatus()) {
                        case COMPLETED -> "COMPLETED";
                        case ACTIVE -> "IN_PROGRESS";
                        case PLANNED -> "FUTURE";
                    };
                    return StakeholderDashboardDTO.MilestoneDTO.builder()
                            .name("Sprint " + s.getSprintNumber() + " Complete")
                            .targetDate(s.getEndDate())
                            .status(milestoneStatus)
                            .build();
                })
                .collect(Collectors.toList());
        milestones.add(StakeholderDashboardDTO.MilestoneDTO.builder()
                .name("Go-Live")
                .targetDate(targetDate)
                .status(daysRemaining <= 0 ? "COMPLETED" : "FUTURE")
                .build());

        // Weekly activity
        LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        List<Task> weekTasks = taskRepository.findByWeek(weekStart, weekEnd);
        int weekCompleted = (int) weekTasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        BigDecimal weekHours = weekTasks.stream()
                .filter(t -> t.getActualHours() != null)
                .map(Task::getActualHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Budget summary
        StakeholderDashboardDTO.BudgetSummaryDTO budgetSummary = projectBudgetRepository.findFirstByOrderByIdAsc()
                .map(pb -> {
                    BigDecimal totalSpent = totalHoursSpent.multiply(pb.getHourlyRate())
                            .setScale(2, RoundingMode.HALF_UP);
                    BigDecimal budgetRemaining = pb.getTotalBudget().subtract(totalSpent);
                    double budgetUsedPct = pb.getTotalBudget().compareTo(BigDecimal.ZERO) > 0
                            ? totalSpent.divide(pb.getTotalBudget(), 4, RoundingMode.HALF_UP).doubleValue() * 100
                            : 0;
                    return StakeholderDashboardDTO.BudgetSummaryDTO.builder()
                            .totalBudget(pb.getTotalBudget())
                            .totalSpent(totalSpent)
                            .remaining(budgetRemaining)
                            .budgetUsedPercent(Math.round(budgetUsedPct * 10) / 10.0)
                            .currency(pb.getCurrency())
                            .build();
                })
                .orElse(null);

        return StakeholderDashboardDTO.builder()
                .projectName("SGCD — Sistema de Gestão Consular Digital")
                .client("Embaixada da República de Angola")
                .overallProgress(overallProgress)
                .totalSessions(totalSessions)
                .completedSessions(completedSessions)
                .totalHoursPlanned(projectConfigService.getTotalHoursPlanned())
                .totalHoursSpent(totalHoursSpent)
                .startDate(startDate)
                .targetDate(targetDate)
                .daysRemaining(Math.max(0, daysRemaining))
                .sprints(sprints)
                .milestones(milestones)
                .weeklyActivity(StakeholderDashboardDTO.WeeklyActivityDTO.builder()
                        .sessionsThisWeek(weekTasks.size())
                        .hoursThisWeek(weekHours)
                        .tasksCompletedThisWeek(weekCompleted)
                        .build())
                .budget(budgetSummary)
                .lastUpdated(LocalDateTime.now())
                .build();
    }
}
