package ao.gov.sgcd.pm.service;

import ao.gov.sgcd.pm.dto.*;
import ao.gov.sgcd.pm.entity.*;
import ao.gov.sgcd.pm.mapper.BlockedDayMapper;
import ao.gov.sgcd.pm.mapper.SprintMapper;
import ao.gov.sgcd.pm.mapper.TaskMapper;
import ao.gov.sgcd.pm.repository.BlockedDayRepository;
import ao.gov.sgcd.pm.repository.ProjectBudgetRepository;
import ao.gov.sgcd.pm.repository.SprintRepository;
import ao.gov.sgcd.pm.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private BlockedDayRepository blockedDayRepository;

    @Mock
    private ProjectBudgetRepository projectBudgetRepository;

    @Mock
    private ProjectConfigService projectConfigService;

    @Mock
    private SprintMapper sprintMapper;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private BlockedDayMapper blockedDayMapper;

    @InjectMocks
    private DashboardService dashboardService;

    // --- Helper builders ---

    private Sprint buildSprint(Long id, int number, String name, SprintStatus status,
                               int totalSessions, int completedSessions) {
        return Sprint.builder()
                .id(id)
                .sprintNumber(number)
                .name(name)
                .nameEn(name + " EN")
                .status(status)
                .totalSessions(totalSessions)
                .completedSessions(completedSessions)
                .totalHours(totalSessions * 3)
                .actualHours(BigDecimal.valueOf(completedSessions * 3))
                .weeks(4)
                .startDate(LocalDate.of(2026, 3, 2))
                .endDate(LocalDate.of(2026, 4, 12))
                .color("#CC092F")
                .focus("Backend")
                .build();
    }

    private Task buildTask(Long id, String taskCode, Sprint sprint, TaskStatus status) {
        return Task.builder()
                .id(id)
                .taskCode(taskCode)
                .title("Tarefa " + taskCode)
                .sprint(sprint)
                .status(status)
                .sessionDate(LocalDate.now())
                .dayOfWeek("SEG")
                .weekNumber(1)
                .plannedHours(BigDecimal.valueOf(3.5))
                .actualHours(status == TaskStatus.COMPLETED ? BigDecimal.valueOf(3.5) : null)
                .sortOrder(1)
                .build();
    }

    // =====================================================================
    // getDeveloperDashboard
    // =====================================================================

    private void stubProjectConfig() {
        when(projectConfigService.getTotalSessions()).thenReturn(204);
        when(projectConfigService.getTotalHoursPlanned()).thenReturn(680);
        lenient().when(projectConfigService.getStartDate()).thenReturn(LocalDate.parse("2026-03-02"));
        lenient().when(projectConfigService.getTargetDate()).thenReturn(LocalDate.parse("2026-12-20"));
    }

    @Test
    void getDeveloperDashboard_shouldAggregateAllMetrics() {
        // given
        stubProjectConfig();
        int completedCount = 50;
        BigDecimal totalHoursSpent = BigDecimal.valueOf(170.0);

        when(taskRepository.countByStatus(TaskStatus.COMPLETED)).thenReturn(completedCount);
        when(taskRepository.sumActualHoursCompleted()).thenReturn(totalHoursSpent);

        // Active sprint
        Sprint activeSprint = buildSprint(2L, 2, "Backend Core", SprintStatus.ACTIVE, 40, 15);
        when(sprintRepository.findActiveSprint()).thenReturn(Optional.of(activeSprint));

        SprintDTO activeSprintDto = SprintDTO.builder()
                .id(2L).sprintNumber(2).name("Backend Core").status(SprintStatus.ACTIVE)
                .totalSessions(40).completedSessions(15)
                .build();
        when(sprintMapper.toDto(activeSprint)).thenReturn(activeSprintDto);

        // Today's task
        Task todayTask = buildTask(51L, "S2-11", activeSprint, TaskStatus.PLANNED);
        when(taskRepository.findBySessionDate(any(LocalDate.class))).thenReturn(Optional.of(todayTask));
        TaskDTO todayTaskDto = TaskDTO.builder().id(51L).taskCode("S2-11").status(TaskStatus.PLANNED).build();
        when(taskMapper.toDto(todayTask)).thenReturn(todayTaskDto);

        // Recent tasks
        Task recentTask = buildTask(50L, "S2-10", activeSprint, TaskStatus.COMPLETED);
        when(taskRepository.findRecentCompleted(any())).thenReturn(List.of(recentTask));
        TaskDTO recentDto = TaskDTO.builder().id(50L).taskCode("S2-10").status(TaskStatus.COMPLETED).build();
        when(taskMapper.toDto(recentTask)).thenReturn(recentDto);

        // Sprint summaries
        Sprint sprint1 = buildSprint(1L, 1, "Fundacao", SprintStatus.COMPLETED, 36, 36);
        when(sprintRepository.findAllOrdered()).thenReturn(List.of(sprint1, activeSprint));

        // Upcoming blocked days
        when(blockedDayRepository.findUpcoming(any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(blockedDayMapper.toDtoList(anyList())).thenReturn(Collections.emptyList());

        // Week tasks
        Task weekTask = buildTask(51L, "S2-11", activeSprint, TaskStatus.COMPLETED);
        weekTask.setActualHours(BigDecimal.valueOf(3.5));
        when(taskRepository.findByWeek(any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(weekTask));

        // when
        DashboardDTO result = dashboardService.getDeveloperDashboard();

        // then
        assertNotNull(result);
        assertEquals(204, result.getTotalSessions());
        assertEquals(completedCount, result.getCompletedSessions());
        assertEquals(680, result.getTotalHoursPlanned());
        assertEquals(totalHoursSpent, result.getTotalHoursSpent());

        double expectedProgress = (50 * 100.0) / 204;
        assertEquals(expectedProgress, result.getProjectProgress(), 0.01);

        assertNotNull(result.getActiveSprint());
        assertEquals("Backend Core", result.getActiveSprint().getName());

        assertNotNull(result.getTodayTask());
        assertEquals("S2-11", result.getTodayTask().getTaskCode());

        assertNotNull(result.getRecentTasks());
        assertEquals(1, result.getRecentTasks().size());

        assertNotNull(result.getSprintSummaries());
        assertEquals(2, result.getSprintSummaries().size());
        assertEquals("COMPLETED", result.getSprintSummaries().get(0).getStatus());
        assertEquals("ACTIVE", result.getSprintSummaries().get(1).getStatus());

        assertNotNull(result.getWeekProgress());
        assertEquals(1, result.getWeekProgress().getWeekTasks());
        assertEquals(1, result.getWeekProgress().getWeekCompleted());
    }

    @Test
    void getDeveloperDashboard_shouldHandleNoActiveSprint() {
        // given
        stubProjectConfig();
        when(taskRepository.countByStatus(TaskStatus.COMPLETED)).thenReturn(0);
        when(taskRepository.sumActualHoursCompleted()).thenReturn(BigDecimal.ZERO);
        when(sprintRepository.findActiveSprint()).thenReturn(Optional.empty());
        when(taskRepository.findBySessionDate(any(LocalDate.class))).thenReturn(Optional.empty());
        when(taskRepository.findUpcomingPlanned(any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(taskRepository.findRecentCompleted(any())).thenReturn(Collections.emptyList());
        when(sprintRepository.findAllOrdered()).thenReturn(Collections.emptyList());
        when(blockedDayRepository.findUpcoming(any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(blockedDayMapper.toDtoList(anyList())).thenReturn(Collections.emptyList());
        when(taskRepository.findByWeek(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // when
        DashboardDTO result = dashboardService.getDeveloperDashboard();

        // then
        assertNotNull(result);
        assertNull(result.getActiveSprint());
        assertNull(result.getTodayTask());
        assertEquals(0.0, result.getProjectProgress());
        assertEquals(0, result.getCompletedSessions());
        assertNotNull(result.getRecentTasks());
        assertTrue(result.getRecentTasks().isEmpty());
        assertNotNull(result.getSprintSummaries());
        assertTrue(result.getSprintSummaries().isEmpty());

        assertNotNull(result.getWeekProgress());
        assertEquals(0, result.getWeekProgress().getWeekTasks());
        assertEquals(0, result.getWeekProgress().getWeekCompleted());
        assertEquals(BigDecimal.ZERO, result.getWeekProgress().getWeekHoursPlanned());
        assertEquals(BigDecimal.ZERO, result.getWeekProgress().getWeekHoursSpent());
    }

    @Test
    void getDeveloperDashboard_shouldFallBackToUpcomingTaskWhenNoTodayTask() {
        // given
        stubProjectConfig();
        when(taskRepository.countByStatus(TaskStatus.COMPLETED)).thenReturn(10);
        when(taskRepository.sumActualHoursCompleted()).thenReturn(BigDecimal.valueOf(35.0));
        when(sprintRepository.findActiveSprint()).thenReturn(Optional.empty());

        // No task for today, but upcoming planned exists
        when(taskRepository.findBySessionDate(any(LocalDate.class))).thenReturn(Optional.empty());
        Sprint sprint = buildSprint(1L, 1, "Fundacao", SprintStatus.ACTIVE, 36, 10);
        Task upcoming = buildTask(11L, "S1-11", sprint, TaskStatus.PLANNED);
        when(taskRepository.findUpcomingPlanned(any(LocalDate.class))).thenReturn(List.of(upcoming));
        TaskDTO upcomingDto = TaskDTO.builder().id(11L).taskCode("S1-11").status(TaskStatus.PLANNED).build();
        when(taskMapper.toDto(upcoming)).thenReturn(upcomingDto);

        when(taskRepository.findRecentCompleted(any())).thenReturn(Collections.emptyList());
        when(sprintRepository.findAllOrdered()).thenReturn(Collections.emptyList());
        when(blockedDayRepository.findUpcoming(any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(blockedDayMapper.toDtoList(anyList())).thenReturn(Collections.emptyList());
        when(taskRepository.findByWeek(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // when
        DashboardDTO result = dashboardService.getDeveloperDashboard();

        // then
        assertNotNull(result.getTodayTask());
        assertEquals("S1-11", result.getTodayTask().getTaskCode());
    }

    // =====================================================================
    // getStakeholderDashboard
    // =====================================================================

    @Test
    void getStakeholderDashboard_shouldReturnReadOnlyMetrics() {
        // given
        stubProjectConfig();
        int completedCount = 80;
        BigDecimal totalHoursSpent = BigDecimal.valueOf(270.0);

        when(taskRepository.countByStatus(TaskStatus.COMPLETED)).thenReturn(completedCount);
        when(taskRepository.sumActualHoursCompleted()).thenReturn(totalHoursSpent);

        Sprint sprint1 = buildSprint(1L, 1, "Fundacao", SprintStatus.COMPLETED, 36, 36);
        Sprint sprint2 = buildSprint(2L, 2, "Backend Core", SprintStatus.ACTIVE, 40, 20);
        Sprint sprint3 = buildSprint(3L, 3, "Frontend", SprintStatus.PLANNED, 35, 0);
        when(sprintRepository.findAllOrdered()).thenReturn(List.of(sprint1, sprint2, sprint3));

        // Week tasks
        Task weekTask = buildTask(81L, "S2-41", sprint2, TaskStatus.COMPLETED);
        weekTask.setActualHours(BigDecimal.valueOf(3.0));
        when(taskRepository.findByWeek(any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(weekTask));

        // when
        StakeholderDashboardDTO result = dashboardService.getStakeholderDashboard();

        // then
        assertNotNull(result);
        assertEquals("SGCD — Sistema de Gestão Consular Digital", result.getProjectName());
        assertEquals("Embaixada da República de Angola", result.getClient());
        assertEquals(204, result.getTotalSessions());
        assertEquals(completedCount, result.getCompletedSessions());
        assertEquals(680, result.getTotalHoursPlanned());
        assertEquals(totalHoursSpent, result.getTotalHoursSpent());

        double expectedProgress = (80 * 100.0) / 204;
        assertEquals(expectedProgress, result.getOverallProgress(), 0.01);

        assertEquals(LocalDate.parse("2026-03-02"), result.getStartDate());
        assertEquals(LocalDate.parse("2026-12-20"), result.getTargetDate());
        assertTrue(result.getDaysRemaining() >= 0);

        // Sprints
        assertNotNull(result.getSprints());
        assertEquals(3, result.getSprints().size());
        assertEquals("COMPLETED", result.getSprints().get(0).getStatus());
        assertEquals("ACTIVE", result.getSprints().get(1).getStatus());
        assertEquals("PLANNED", result.getSprints().get(2).getStatus());

        // Milestones: 3 sprints + 1 Go-Live
        assertNotNull(result.getMilestones());
        assertEquals(4, result.getMilestones().size());
        assertEquals("Sprint 1 Complete", result.getMilestones().get(0).getName());
        assertEquals("COMPLETED", result.getMilestones().get(0).getStatus());
        assertEquals("Sprint 2 Complete", result.getMilestones().get(1).getName());
        assertEquals("IN_PROGRESS", result.getMilestones().get(1).getStatus());
        assertEquals("Sprint 3 Complete", result.getMilestones().get(2).getName());
        assertEquals("FUTURE", result.getMilestones().get(2).getStatus());
        assertEquals("Go-Live", result.getMilestones().get(3).getName());

        // Weekly activity
        assertNotNull(result.getWeeklyActivity());
        assertEquals(1, result.getWeeklyActivity().getSessionsThisWeek());
        assertEquals(BigDecimal.valueOf(3.0), result.getWeeklyActivity().getHoursThisWeek());
        assertEquals(1, result.getWeeklyActivity().getTasksCompletedThisWeek());

        assertNotNull(result.getLastUpdated());
    }

    @Test
    void getStakeholderDashboard_shouldHandleEmptyData() {
        // given
        stubProjectConfig();
        when(taskRepository.countByStatus(TaskStatus.COMPLETED)).thenReturn(0);
        when(taskRepository.sumActualHoursCompleted()).thenReturn(BigDecimal.ZERO);
        when(sprintRepository.findAllOrdered()).thenReturn(Collections.emptyList());
        when(taskRepository.findByWeek(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // when
        StakeholderDashboardDTO result = dashboardService.getStakeholderDashboard();

        // then
        assertNotNull(result);
        assertEquals(0.0, result.getOverallProgress());
        assertEquals(0, result.getCompletedSessions());
        assertEquals(BigDecimal.ZERO, result.getTotalHoursSpent());
        assertNotNull(result.getSprints());
        assertTrue(result.getSprints().isEmpty());
        // Milestones should have only Go-Live
        assertNotNull(result.getMilestones());
        assertEquals(1, result.getMilestones().size());
        assertEquals("Go-Live", result.getMilestones().get(0).getName());

        assertNotNull(result.getWeeklyActivity());
        assertEquals(0, result.getWeeklyActivity().getSessionsThisWeek());
    }

    @Test
    void getStakeholderDashboard_shouldCalculateSprintProgress() {
        // given
        stubProjectConfig();
        when(taskRepository.countByStatus(TaskStatus.COMPLETED)).thenReturn(36);
        when(taskRepository.sumActualHoursCompleted()).thenReturn(BigDecimal.valueOf(108));

        Sprint completedSprint = buildSprint(1L, 1, "Fundacao", SprintStatus.COMPLETED, 36, 36);
        when(sprintRepository.findAllOrdered()).thenReturn(List.of(completedSprint));
        when(taskRepository.findByWeek(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // when
        StakeholderDashboardDTO result = dashboardService.getStakeholderDashboard();

        // then
        assertEquals(1, result.getSprints().size());
        assertEquals(100.0, result.getSprints().get(0).getProgress(), 0.01);
        assertEquals(36, result.getSprints().get(0).getSessions());
        assertEquals(36, result.getSprints().get(0).getCompletedSessions());
    }
}
