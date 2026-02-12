package ao.gov.sgcd.pm.controller;

import ao.gov.sgcd.pm.config.JwtTokenProvider;
import ao.gov.sgcd.pm.dto.StakeholderDashboardDTO;
import ao.gov.sgcd.pm.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StakeholderController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "sgcd-pm.stakeholder.token=sgcd-stakeholder-2026"
})
class StakeholderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private DashboardService dashboardService;

    private StakeholderDashboardDTO buildSampleDashboard() {
        return StakeholderDashboardDTO.builder()
                .projectName("SGCD - Sistema de Gestao Consular Digital")
                .client("Embaixada da Republica de Angola")
                .overallProgress(25.5)
                .totalSessions(204)
                .completedSessions(52)
                .totalHoursPlanned(680)
                .totalHoursSpent(BigDecimal.valueOf(175.5))
                .startDate(LocalDate.of(2026, 3, 2))
                .targetDate(LocalDate.of(2026, 12, 20))
                .daysRemaining(200L)
                .sprints(List.of())
                .milestones(List.of())
                .weeklyActivity(StakeholderDashboardDTO.WeeklyActivityDTO.builder()
                        .sessionsThisWeek(5)
                        .hoursThisWeek(BigDecimal.valueOf(17.5))
                        .tasksCompletedThisWeek(3)
                        .build())
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    @Test
    void getDashboard_withoutToken_shouldReturn403() throws Exception {
        mockMvc.perform(get("/v1/stakeholder"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getDashboard_withValidToken_shouldReturn200() throws Exception {
        when(dashboardService.getStakeholderDashboard()).thenReturn(buildSampleDashboard());

        mockMvc.perform(get("/v1/stakeholder")
                        .param("token", "sgcd-stakeholder-2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectName", is("SGCD - Sistema de Gestao Consular Digital")))
                .andExpect(jsonPath("$.client", is("Embaixada da Republica de Angola")));
    }

    @Test
    void getDashboard_withInvalidToken_shouldReturn403() throws Exception {
        mockMvc.perform(get("/v1/stakeholder")
                        .param("token", "invalid-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getDashboard_withEmptyToken_shouldReturn403() throws Exception {
        // Empty string does not equal the valid token
        mockMvc.perform(get("/v1/stakeholder")
                        .param("token", ""))
                .andExpect(status().isForbidden());
    }

    @Test
    void getDashboard_shouldReturnFullDashboardStructure() throws Exception {
        StakeholderDashboardDTO dashboard = StakeholderDashboardDTO.builder()
                .projectName("SGCD")
                .client("Embaixada")
                .overallProgress(50.0)
                .totalSessions(204)
                .completedSessions(102)
                .totalHoursPlanned(680)
                .totalHoursSpent(BigDecimal.valueOf(340))
                .startDate(LocalDate.of(2026, 3, 2))
                .targetDate(LocalDate.of(2026, 12, 20))
                .daysRemaining(150L)
                .sprints(List.of(
                        StakeholderDashboardDTO.StakeholderSprintDTO.builder()
                                .number(1)
                                .name("Sprint 1")
                                .progress(100.0)
                                .status("COMPLETED")
                                .build(),
                        StakeholderDashboardDTO.StakeholderSprintDTO.builder()
                                .number(2)
                                .name("Sprint 2")
                                .progress(75.0)
                                .status("ACTIVE")
                                .build()
                ))
                .milestones(List.of(
                        StakeholderDashboardDTO.MilestoneDTO.builder()
                                .name("Sprint 1 Complete")
                                .targetDate(LocalDate.of(2026, 4, 13))
                                .status("COMPLETED")
                                .build()
                ))
                .weeklyActivity(StakeholderDashboardDTO.WeeklyActivityDTO.builder()
                        .sessionsThisWeek(4)
                        .hoursThisWeek(BigDecimal.valueOf(14))
                        .tasksCompletedThisWeek(4)
                        .build())
                .lastUpdated(LocalDateTime.of(2026, 6, 15, 12, 0))
                .build();

        when(dashboardService.getStakeholderDashboard()).thenReturn(dashboard);

        mockMvc.perform(get("/v1/stakeholder")
                        .param("token", "sgcd-stakeholder-2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallProgress", is(50.0)))
                .andExpect(jsonPath("$.completedSessions", is(102)))
                .andExpect(jsonPath("$.daysRemaining", is(150)))
                .andExpect(jsonPath("$.sprints", hasSize(2)))
                .andExpect(jsonPath("$.sprints[0].number", is(1)))
                .andExpect(jsonPath("$.sprints[0].progress", is(100.0)))
                .andExpect(jsonPath("$.sprints[1].status", is("ACTIVE")))
                .andExpect(jsonPath("$.milestones", hasSize(1)))
                .andExpect(jsonPath("$.milestones[0].status", is("COMPLETED")))
                .andExpect(jsonPath("$.weeklyActivity.sessionsThisWeek", is(4)))
                .andExpect(jsonPath("$.lastUpdated", notNullValue()));
    }

    @Test
    void getDashboard_withWrongTokenValue_shouldReturn403() throws Exception {
        mockMvc.perform(get("/v1/stakeholder")
                        .param("token", "sgcd-stakeholder-2025"))
                .andExpect(status().isForbidden());
    }
}
