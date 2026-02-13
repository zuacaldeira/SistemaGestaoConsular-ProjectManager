package ao.gov.sgcd.pm.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StakeholderDashboardDTOTest {

    // --- StakeholderDashboardDTO tests ---

    @Test
    void builder_shouldCreateDTOWithAllFields() {
        LocalDate startDate = LocalDate.of(2026, 1, 5);
        LocalDate targetDate = LocalDate.of(2026, 12, 31);
        LocalDateTime lastUpdated = LocalDateTime.of(2026, 6, 15, 10, 30);
        List<StakeholderDashboardDTO.StakeholderSprintDTO> sprints = new ArrayList<>();
        List<StakeholderDashboardDTO.MilestoneDTO> milestones = new ArrayList<>();
        StakeholderDashboardDTO.WeeklyActivityDTO weeklyActivity = StakeholderDashboardDTO.WeeklyActivityDTO.builder()
                .sessionsThisWeek(5)
                .build();

        StakeholderDashboardDTO dto = StakeholderDashboardDTO.builder()
                .projectName("SGCD MVP")
                .client("Embaixada de Angola")
                .overallProgress(45.0)
                .totalSessions(204)
                .completedSessions(92)
                .totalHoursPlanned(680)
                .totalHoursSpent(BigDecimal.valueOf(310.0))
                .startDate(startDate)
                .targetDate(targetDate)
                .daysRemaining(199L)
                .sprints(sprints)
                .milestones(milestones)
                .weeklyActivity(weeklyActivity)
                .lastUpdated(lastUpdated)
                .build();

        assertEquals("SGCD MVP", dto.getProjectName());
        assertEquals("Embaixada de Angola", dto.getClient());
        assertEquals(45.0, dto.getOverallProgress());
        assertEquals(204, dto.getTotalSessions());
        assertEquals(92, dto.getCompletedSessions());
        assertEquals(680, dto.getTotalHoursPlanned());
        assertEquals(BigDecimal.valueOf(310.0), dto.getTotalHoursSpent());
        assertEquals(startDate, dto.getStartDate());
        assertEquals(targetDate, dto.getTargetDate());
        assertEquals(199L, dto.getDaysRemaining());
        assertSame(sprints, dto.getSprints());
        assertSame(milestones, dto.getMilestones());
        assertSame(weeklyActivity, dto.getWeeklyActivity());
        assertEquals(lastUpdated, dto.getLastUpdated());
    }

    @Test
    void noArgConstructor_shouldCreateEmptyDTO() {
        StakeholderDashboardDTO dto = new StakeholderDashboardDTO();

        assertNull(dto.getProjectName());
        assertNull(dto.getClient());
        assertNull(dto.getOverallProgress());
        assertNull(dto.getTotalSessions());
        assertNull(dto.getCompletedSessions());
        assertNull(dto.getTotalHoursPlanned());
        assertNull(dto.getTotalHoursSpent());
        assertNull(dto.getStartDate());
        assertNull(dto.getTargetDate());
        assertNull(dto.getDaysRemaining());
        assertNull(dto.getSprints());
        assertNull(dto.getMilestones());
        assertNull(dto.getWeeklyActivity());
        assertNull(dto.getLastUpdated());
    }

    @Test
    void allArgsConstructor_shouldSetAllFields() {
        LocalDate startDate = LocalDate.of(2026, 1, 5);
        LocalDate targetDate = LocalDate.of(2026, 12, 31);
        LocalDateTime lastUpdated = LocalDateTime.of(2026, 6, 15, 10, 30);
        List<StakeholderDashboardDTO.StakeholderSprintDTO> sprints = new ArrayList<>();
        List<StakeholderDashboardDTO.MilestoneDTO> milestones = new ArrayList<>();
        StakeholderDashboardDTO.WeeklyActivityDTO weeklyActivity = StakeholderDashboardDTO.WeeklyActivityDTO.builder().build();

        StakeholderDashboardDTO dto = new StakeholderDashboardDTO(
                "SGCD MVP", "Embaixada de Angola", 45.0, 204, 92,
                680, BigDecimal.valueOf(310.0), startDate, targetDate,
                199L, sprints, milestones, weeklyActivity, null, lastUpdated
        );

        assertEquals("SGCD MVP", dto.getProjectName());
        assertEquals("Embaixada de Angola", dto.getClient());
        assertEquals(45.0, dto.getOverallProgress());
        assertEquals(204, dto.getTotalSessions());
        assertEquals(92, dto.getCompletedSessions());
        assertEquals(680, dto.getTotalHoursPlanned());
        assertEquals(BigDecimal.valueOf(310.0), dto.getTotalHoursSpent());
        assertEquals(startDate, dto.getStartDate());
        assertEquals(targetDate, dto.getTargetDate());
        assertEquals(199L, dto.getDaysRemaining());
        assertSame(sprints, dto.getSprints());
        assertSame(milestones, dto.getMilestones());
        assertSame(weeklyActivity, dto.getWeeklyActivity());
        assertEquals(lastUpdated, dto.getLastUpdated());
    }

    @Test
    void gettersAndSetters_shouldWorkForAllFields() {
        StakeholderDashboardDTO dto = new StakeholderDashboardDTO();
        LocalDate startDate = LocalDate.of(2026, 2, 1);
        LocalDate targetDate = LocalDate.of(2026, 11, 30);
        LocalDateTime lastUpdated = LocalDateTime.of(2026, 7, 1, 9, 0);
        List<StakeholderDashboardDTO.StakeholderSprintDTO> sprints = new ArrayList<>();
        List<StakeholderDashboardDTO.MilestoneDTO> milestones = new ArrayList<>();
        StakeholderDashboardDTO.WeeklyActivityDTO weeklyActivity = StakeholderDashboardDTO.WeeklyActivityDTO.builder().build();

        dto.setProjectName("SGCD");
        dto.setClient("Angola Embassy");
        dto.setOverallProgress(60.0);
        dto.setTotalSessions(204);
        dto.setCompletedSessions(122);
        dto.setTotalHoursPlanned(680);
        dto.setTotalHoursSpent(BigDecimal.valueOf(400.0));
        dto.setStartDate(startDate);
        dto.setTargetDate(targetDate);
        dto.setDaysRemaining(152L);
        dto.setSprints(sprints);
        dto.setMilestones(milestones);
        dto.setWeeklyActivity(weeklyActivity);
        dto.setLastUpdated(lastUpdated);

        assertEquals("SGCD", dto.getProjectName());
        assertEquals("Angola Embassy", dto.getClient());
        assertEquals(60.0, dto.getOverallProgress());
        assertEquals(204, dto.getTotalSessions());
        assertEquals(122, dto.getCompletedSessions());
        assertEquals(680, dto.getTotalHoursPlanned());
        assertEquals(BigDecimal.valueOf(400.0), dto.getTotalHoursSpent());
        assertEquals(startDate, dto.getStartDate());
        assertEquals(targetDate, dto.getTargetDate());
        assertEquals(152L, dto.getDaysRemaining());
        assertSame(sprints, dto.getSprints());
        assertSame(milestones, dto.getMilestones());
        assertSame(weeklyActivity, dto.getWeeklyActivity());
        assertEquals(lastUpdated, dto.getLastUpdated());
    }

    @Test
    void equals_reflexive() {
        StakeholderDashboardDTO dto = StakeholderDashboardDTO.builder()
                .projectName("SGCD").overallProgress(50.0).build();
        assertEquals(dto, dto);
    }

    @Test
    void equals_symmetric() {
        StakeholderDashboardDTO dto1 = StakeholderDashboardDTO.builder()
                .projectName("SGCD").overallProgress(50.0).totalSessions(204).build();
        StakeholderDashboardDTO dto2 = StakeholderDashboardDTO.builder()
                .projectName("SGCD").overallProgress(50.0).totalSessions(204).build();

        assertEquals(dto1, dto2);
        assertEquals(dto2, dto1);
    }

    @Test
    void equals_nullReturnsFalse() {
        StakeholderDashboardDTO dto = StakeholderDashboardDTO.builder().projectName("SGCD").build();
        assertNotEquals(null, dto);
    }

    @Test
    void equals_differentClassReturnsFalse() {
        StakeholderDashboardDTO dto = StakeholderDashboardDTO.builder().projectName("SGCD").build();
        assertNotEquals("a string", dto);
    }

    @Test
    void equals_differentValuesReturnsFalse() {
        StakeholderDashboardDTO dto1 = StakeholderDashboardDTO.builder()
                .projectName("SGCD").overallProgress(50.0).build();
        StakeholderDashboardDTO dto2 = StakeholderDashboardDTO.builder()
                .projectName("Other").overallProgress(75.0).build();

        assertNotEquals(dto1, dto2);
    }

    @Test
    void hashCode_equalObjectsSameHashCode() {
        StakeholderDashboardDTO dto1 = StakeholderDashboardDTO.builder()
                .projectName("SGCD").totalSessions(204).build();
        StakeholderDashboardDTO dto2 = StakeholderDashboardDTO.builder()
                .projectName("SGCD").totalSessions(204).build();

        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void hashCode_differentObjectsDifferentHashCode() {
        StakeholderDashboardDTO dto1 = StakeholderDashboardDTO.builder().projectName("SGCD").build();
        StakeholderDashboardDTO dto2 = StakeholderDashboardDTO.builder().projectName("Other").build();

        assertNotEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void toString_containsClassNameAndFieldValues() {
        StakeholderDashboardDTO dto = StakeholderDashboardDTO.builder()
                .projectName("SGCD MVP")
                .overallProgress(45.0)
                .totalSessions(204)
                .build();

        String result = dto.toString();
        assertTrue(result.contains("StakeholderDashboardDTO"));
        assertTrue(result.contains("SGCD MVP"));
        assertTrue(result.contains("204"));
    }

    // --- StakeholderSprintDTO tests ---

    @Test
    void stakeholderSprintDTO_builder_shouldCreateWithAllFields() {
        LocalDate startDate = LocalDate.of(2026, 1, 5);
        LocalDate endDate = LocalDate.of(2026, 2, 1);

        StakeholderDashboardDTO.StakeholderSprintDTO dto = StakeholderDashboardDTO.StakeholderSprintDTO.builder()
                .number(1)
                .name("Sprint 1 - Fundacao")
                .nameEn("Sprint 1 - Foundation")
                .progress(75.0)
                .status("ACTIVE")
                .startDate(startDate)
                .endDate(endDate)
                .sessions(36)
                .completedSessions(27)
                .hours(120)
                .hoursSpent(BigDecimal.valueOf(90.0))
                .color("#CC092F")
                .focus("Backend")
                .build();

        assertEquals(1, dto.getNumber());
        assertEquals("Sprint 1 - Fundacao", dto.getName());
        assertEquals("Sprint 1 - Foundation", dto.getNameEn());
        assertEquals(75.0, dto.getProgress());
        assertEquals("ACTIVE", dto.getStatus());
        assertEquals(startDate, dto.getStartDate());
        assertEquals(endDate, dto.getEndDate());
        assertEquals(36, dto.getSessions());
        assertEquals(27, dto.getCompletedSessions());
        assertEquals(120, dto.getHours());
        assertEquals(BigDecimal.valueOf(90.0), dto.getHoursSpent());
        assertEquals("#CC092F", dto.getColor());
        assertEquals("Backend", dto.getFocus());
    }

    @Test
    void stakeholderSprintDTO_noArgConstructor_shouldCreateEmpty() {
        StakeholderDashboardDTO.StakeholderSprintDTO dto = new StakeholderDashboardDTO.StakeholderSprintDTO();

        assertNull(dto.getNumber());
        assertNull(dto.getName());
        assertNull(dto.getNameEn());
        assertNull(dto.getProgress());
        assertNull(dto.getStatus());
        assertNull(dto.getStartDate());
        assertNull(dto.getEndDate());
        assertNull(dto.getSessions());
        assertNull(dto.getCompletedSessions());
        assertNull(dto.getHours());
        assertNull(dto.getHoursSpent());
        assertNull(dto.getColor());
        assertNull(dto.getFocus());
    }

    @Test
    void stakeholderSprintDTO_allArgsConstructor_shouldSetAllFields() {
        LocalDate startDate = LocalDate.of(2026, 3, 1);
        LocalDate endDate = LocalDate.of(2026, 4, 1);

        StakeholderDashboardDTO.StakeholderSprintDTO dto = new StakeholderDashboardDTO.StakeholderSprintDTO(
                2, "Sprint 2", "Sprint 2 EN", 50.0, "PLANNED",
                startDate, endDate, 40, 20, 140,
                BigDecimal.valueOf(70.0), "#1A1A1A", "Frontend"
        );

        assertEquals(2, dto.getNumber());
        assertEquals("Sprint 2", dto.getName());
        assertEquals("Sprint 2 EN", dto.getNameEn());
        assertEquals(50.0, dto.getProgress());
        assertEquals("PLANNED", dto.getStatus());
        assertEquals(startDate, dto.getStartDate());
        assertEquals(endDate, dto.getEndDate());
        assertEquals(40, dto.getSessions());
        assertEquals(20, dto.getCompletedSessions());
        assertEquals(140, dto.getHours());
        assertEquals(BigDecimal.valueOf(70.0), dto.getHoursSpent());
        assertEquals("#1A1A1A", dto.getColor());
        assertEquals("Frontend", dto.getFocus());
    }

    @Test
    void stakeholderSprintDTO_gettersAndSetters_shouldWork() {
        StakeholderDashboardDTO.StakeholderSprintDTO dto = new StakeholderDashboardDTO.StakeholderSprintDTO();
        LocalDate startDate = LocalDate.of(2026, 5, 1);
        LocalDate endDate = LocalDate.of(2026, 6, 1);

        dto.setNumber(3);
        dto.setName("Sprint 3");
        dto.setNameEn("Sprint 3 EN");
        dto.setProgress(100.0);
        dto.setStatus("COMPLETED");
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);
        dto.setSessions(30);
        dto.setCompletedSessions(30);
        dto.setHours(100);
        dto.setHoursSpent(BigDecimal.valueOf(98.0));
        dto.setColor("#F4B400");
        dto.setFocus("Integration");

        assertEquals(3, dto.getNumber());
        assertEquals("Sprint 3", dto.getName());
        assertEquals("Sprint 3 EN", dto.getNameEn());
        assertEquals(100.0, dto.getProgress());
        assertEquals("COMPLETED", dto.getStatus());
        assertEquals(startDate, dto.getStartDate());
        assertEquals(endDate, dto.getEndDate());
        assertEquals(30, dto.getSessions());
        assertEquals(30, dto.getCompletedSessions());
        assertEquals(100, dto.getHours());
        assertEquals(BigDecimal.valueOf(98.0), dto.getHoursSpent());
        assertEquals("#F4B400", dto.getColor());
        assertEquals("Integration", dto.getFocus());
    }

    @Test
    void stakeholderSprintDTO_equals_symmetric() {
        StakeholderDashboardDTO.StakeholderSprintDTO dto1 = StakeholderDashboardDTO.StakeholderSprintDTO.builder()
                .number(1).name("Sprint 1").progress(50.0).build();
        StakeholderDashboardDTO.StakeholderSprintDTO dto2 = StakeholderDashboardDTO.StakeholderSprintDTO.builder()
                .number(1).name("Sprint 1").progress(50.0).build();

        assertEquals(dto1, dto2);
        assertEquals(dto2, dto1);
    }

    @Test
    void stakeholderSprintDTO_equals_differentValuesReturnsFalse() {
        StakeholderDashboardDTO.StakeholderSprintDTO dto1 = StakeholderDashboardDTO.StakeholderSprintDTO.builder()
                .number(1).name("Sprint 1").build();
        StakeholderDashboardDTO.StakeholderSprintDTO dto2 = StakeholderDashboardDTO.StakeholderSprintDTO.builder()
                .number(2).name("Sprint 2").build();

        assertNotEquals(dto1, dto2);
    }

    @Test
    void stakeholderSprintDTO_hashCode_equalObjectsSameHashCode() {
        StakeholderDashboardDTO.StakeholderSprintDTO dto1 = StakeholderDashboardDTO.StakeholderSprintDTO.builder()
                .number(1).name("Sprint 1").build();
        StakeholderDashboardDTO.StakeholderSprintDTO dto2 = StakeholderDashboardDTO.StakeholderSprintDTO.builder()
                .number(1).name("Sprint 1").build();

        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void stakeholderSprintDTO_toString_containsFieldValues() {
        StakeholderDashboardDTO.StakeholderSprintDTO dto = StakeholderDashboardDTO.StakeholderSprintDTO.builder()
                .number(1).name("Sprint 1 - Fundacao").build();

        String result = dto.toString();
        assertTrue(result.contains("StakeholderSprintDTO"));
        assertTrue(result.contains("Sprint 1 - Fundacao"));
    }

    // --- MilestoneDTO tests ---

    @Test
    void milestoneDTO_builder_shouldCreateWithAllFields() {
        LocalDate targetDate = LocalDate.of(2026, 3, 31);

        StakeholderDashboardDTO.MilestoneDTO dto = StakeholderDashboardDTO.MilestoneDTO.builder()
                .name("Backend MVP")
                .targetDate(targetDate)
                .status("COMPLETED")
                .build();

        assertEquals("Backend MVP", dto.getName());
        assertEquals(targetDate, dto.getTargetDate());
        assertEquals("COMPLETED", dto.getStatus());
    }

    @Test
    void milestoneDTO_noArgConstructor_shouldCreateEmpty() {
        StakeholderDashboardDTO.MilestoneDTO dto = new StakeholderDashboardDTO.MilestoneDTO();

        assertNull(dto.getName());
        assertNull(dto.getTargetDate());
        assertNull(dto.getStatus());
    }

    @Test
    void milestoneDTO_allArgsConstructor_shouldSetAllFields() {
        LocalDate targetDate = LocalDate.of(2026, 6, 30);

        StakeholderDashboardDTO.MilestoneDTO dto = new StakeholderDashboardDTO.MilestoneDTO(
                "Frontend Complete", targetDate, "IN_PROGRESS"
        );

        assertEquals("Frontend Complete", dto.getName());
        assertEquals(targetDate, dto.getTargetDate());
        assertEquals("IN_PROGRESS", dto.getStatus());
    }

    @Test
    void milestoneDTO_gettersAndSetters_shouldWork() {
        StakeholderDashboardDTO.MilestoneDTO dto = new StakeholderDashboardDTO.MilestoneDTO();
        LocalDate targetDate = LocalDate.of(2026, 9, 30);

        dto.setName("Full Integration");
        dto.setTargetDate(targetDate);
        dto.setStatus("PLANNED");

        assertEquals("Full Integration", dto.getName());
        assertEquals(targetDate, dto.getTargetDate());
        assertEquals("PLANNED", dto.getStatus());
    }

    @Test
    void milestoneDTO_equals_symmetric() {
        LocalDate targetDate = LocalDate.of(2026, 3, 31);
        StakeholderDashboardDTO.MilestoneDTO dto1 = StakeholderDashboardDTO.MilestoneDTO.builder()
                .name("Milestone 1").targetDate(targetDate).status("PLANNED").build();
        StakeholderDashboardDTO.MilestoneDTO dto2 = StakeholderDashboardDTO.MilestoneDTO.builder()
                .name("Milestone 1").targetDate(targetDate).status("PLANNED").build();

        assertEquals(dto1, dto2);
        assertEquals(dto2, dto1);
    }

    @Test
    void milestoneDTO_equals_differentValuesReturnsFalse() {
        StakeholderDashboardDTO.MilestoneDTO dto1 = StakeholderDashboardDTO.MilestoneDTO.builder()
                .name("Milestone 1").build();
        StakeholderDashboardDTO.MilestoneDTO dto2 = StakeholderDashboardDTO.MilestoneDTO.builder()
                .name("Milestone 2").build();

        assertNotEquals(dto1, dto2);
    }

    @Test
    void milestoneDTO_hashCode_equalObjectsSameHashCode() {
        StakeholderDashboardDTO.MilestoneDTO dto1 = StakeholderDashboardDTO.MilestoneDTO.builder()
                .name("Milestone 1").status("PLANNED").build();
        StakeholderDashboardDTO.MilestoneDTO dto2 = StakeholderDashboardDTO.MilestoneDTO.builder()
                .name("Milestone 1").status("PLANNED").build();

        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void milestoneDTO_toString_containsFieldValues() {
        StakeholderDashboardDTO.MilestoneDTO dto = StakeholderDashboardDTO.MilestoneDTO.builder()
                .name("Backend MVP").status("COMPLETED").build();

        String result = dto.toString();
        assertTrue(result.contains("MilestoneDTO"));
        assertTrue(result.contains("Backend MVP"));
        assertTrue(result.contains("COMPLETED"));
    }

    // --- WeeklyActivityDTO tests ---

    @Test
    void weeklyActivityDTO_builder_shouldCreateWithAllFields() {
        StakeholderDashboardDTO.WeeklyActivityDTO dto = StakeholderDashboardDTO.WeeklyActivityDTO.builder()
                .sessionsThisWeek(5)
                .hoursThisWeek(BigDecimal.valueOf(17.5))
                .tasksCompletedThisWeek(4)
                .build();

        assertEquals(5, dto.getSessionsThisWeek());
        assertEquals(BigDecimal.valueOf(17.5), dto.getHoursThisWeek());
        assertEquals(4, dto.getTasksCompletedThisWeek());
    }

    @Test
    void weeklyActivityDTO_noArgConstructor_shouldCreateEmpty() {
        StakeholderDashboardDTO.WeeklyActivityDTO dto = new StakeholderDashboardDTO.WeeklyActivityDTO();

        assertNull(dto.getSessionsThisWeek());
        assertNull(dto.getHoursThisWeek());
        assertNull(dto.getTasksCompletedThisWeek());
    }

    @Test
    void weeklyActivityDTO_allArgsConstructor_shouldSetAllFields() {
        StakeholderDashboardDTO.WeeklyActivityDTO dto = new StakeholderDashboardDTO.WeeklyActivityDTO(
                3, BigDecimal.valueOf(10.5), 2
        );

        assertEquals(3, dto.getSessionsThisWeek());
        assertEquals(BigDecimal.valueOf(10.5), dto.getHoursThisWeek());
        assertEquals(2, dto.getTasksCompletedThisWeek());
    }

    @Test
    void weeklyActivityDTO_gettersAndSetters_shouldWork() {
        StakeholderDashboardDTO.WeeklyActivityDTO dto = new StakeholderDashboardDTO.WeeklyActivityDTO();

        dto.setSessionsThisWeek(4);
        dto.setHoursThisWeek(BigDecimal.valueOf(14.0));
        dto.setTasksCompletedThisWeek(3);

        assertEquals(4, dto.getSessionsThisWeek());
        assertEquals(BigDecimal.valueOf(14.0), dto.getHoursThisWeek());
        assertEquals(3, dto.getTasksCompletedThisWeek());
    }

    @Test
    void weeklyActivityDTO_equals_symmetric() {
        StakeholderDashboardDTO.WeeklyActivityDTO dto1 = StakeholderDashboardDTO.WeeklyActivityDTO.builder()
                .sessionsThisWeek(5).hoursThisWeek(BigDecimal.valueOf(17.5)).build();
        StakeholderDashboardDTO.WeeklyActivityDTO dto2 = StakeholderDashboardDTO.WeeklyActivityDTO.builder()
                .sessionsThisWeek(5).hoursThisWeek(BigDecimal.valueOf(17.5)).build();

        assertEquals(dto1, dto2);
        assertEquals(dto2, dto1);
    }

    @Test
    void weeklyActivityDTO_equals_differentValuesReturnsFalse() {
        StakeholderDashboardDTO.WeeklyActivityDTO dto1 = StakeholderDashboardDTO.WeeklyActivityDTO.builder()
                .sessionsThisWeek(5).build();
        StakeholderDashboardDTO.WeeklyActivityDTO dto2 = StakeholderDashboardDTO.WeeklyActivityDTO.builder()
                .sessionsThisWeek(3).build();

        assertNotEquals(dto1, dto2);
    }

    @Test
    void weeklyActivityDTO_hashCode_equalObjectsSameHashCode() {
        StakeholderDashboardDTO.WeeklyActivityDTO dto1 = StakeholderDashboardDTO.WeeklyActivityDTO.builder()
                .sessionsThisWeek(5).tasksCompletedThisWeek(4).build();
        StakeholderDashboardDTO.WeeklyActivityDTO dto2 = StakeholderDashboardDTO.WeeklyActivityDTO.builder()
                .sessionsThisWeek(5).tasksCompletedThisWeek(4).build();

        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void weeklyActivityDTO_toString_containsFieldValues() {
        StakeholderDashboardDTO.WeeklyActivityDTO dto = StakeholderDashboardDTO.WeeklyActivityDTO.builder()
                .sessionsThisWeek(5)
                .tasksCompletedThisWeek(4)
                .build();

        String result = dto.toString();
        assertTrue(result.contains("WeeklyActivityDTO"));
        assertTrue(result.contains("5"));
        assertTrue(result.contains("4"));
    }
}
