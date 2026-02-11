package ao.gov.sgcd.pm.controller;

import ao.gov.sgcd.pm.config.JwtTokenProvider;
import ao.gov.sgcd.pm.dto.ReportDTO;
import ao.gov.sgcd.pm.entity.ReportType;
import ao.gov.sgcd.pm.service.PdfExportService;
import ao.gov.sgcd.pm.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private ReportService reportService;

    @MockBean
    private PdfExportService pdfExportService;

    private ReportDTO buildSampleReport(Long id, Long sprintId) {
        return ReportDTO.builder()
                .id(id)
                .sprintId(sprintId)
                .sprintNumber(sprintId.intValue())
                .sprintName("Sprint " + sprintId + " - Fundacao")
                .reportType(ReportType.SPRINT_END)
                .generatedAt(LocalDateTime.of(2026, 4, 13, 18, 0))
                .summaryPt("Sprint 1: 36/36 sessoes concluidas (100.0%).")
                .summaryEn("Sprint 1: 36/36 sessions completed (100.0%).")
                .metricsJson("{\"totalSessions\":36,\"completedSessions\":36}")
                .build();
    }

    @Test
    void findAll_shouldReturn200WithReportList() throws Exception {
        ReportDTO report1 = buildSampleReport(1L, 1L);
        ReportDTO report2 = buildSampleReport(2L, 2L);

        when(reportService.findAll()).thenReturn(List.of(report1, report2));

        mockMvc.perform(get("/v1/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].sprintId", is(1)))
                .andExpect(jsonPath("$[0].reportType", is("SPRINT_END")))
                .andExpect(jsonPath("$[0].summaryPt", notNullValue()))
                .andExpect(jsonPath("$[0].summaryEn", notNullValue()))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    void findAll_withNoReports_shouldReturn200WithEmptyList() throws Exception {
        when(reportService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/v1/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void findBySprintId_shouldReturn200WithReport() throws Exception {
        ReportDTO report = buildSampleReport(1L, 1L);
        when(reportService.findBySprintId(1L)).thenReturn(report);

        mockMvc.perform(get("/v1/reports/sprint/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.sprintId", is(1)))
                .andExpect(jsonPath("$.sprintNumber", is(1)))
                .andExpect(jsonPath("$.sprintName", is("Sprint 1 - Fundacao")))
                .andExpect(jsonPath("$.reportType", is("SPRINT_END")))
                .andExpect(jsonPath("$.summaryPt", containsString("36/36")))
                .andExpect(jsonPath("$.summaryEn", containsString("sessions completed")))
                .andExpect(jsonPath("$.metricsJson", notNullValue()));
    }

    @Test
    void findBySprintId_whenNotFound_shouldReturn500() throws Exception {
        when(reportService.findBySprintId(999L))
                .thenThrow(new RuntimeException("Relatorio nao encontrado para sprint: 999"));

        mockMvc.perform(get("/v1/reports/sprint/999"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void generateReport_shouldReturn200WithGeneratedReport() throws Exception {
        ReportDTO generatedReport = ReportDTO.builder()
                .id(3L)
                .sprintId(1L)
                .sprintNumber(1)
                .sprintName("Sprint 1 - Fundacao")
                .reportType(ReportType.SPRINT_END)
                .generatedAt(LocalDateTime.now())
                .summaryPt("Sprint 1 (Sprint 1 - Fundacao): 36/36 sessoes concluidas (100.0%).")
                .summaryEn("Sprint 1 (Sprint 1 - Foundation): 36/36 sessions completed (100.0%).")
                .metricsJson("{\"totalSessions\":36,\"completedSessions\":36,\"progressPercent\":100.0}")
                .build();

        when(reportService.generateReport(1L)).thenReturn(generatedReport);

        mockMvc.perform(post("/v1/reports/sprint/1/generate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.sprintId", is(1)))
                .andExpect(jsonPath("$.reportType", is("SPRINT_END")))
                .andExpect(jsonPath("$.generatedAt", notNullValue()))
                .andExpect(jsonPath("$.summaryPt", notNullValue()))
                .andExpect(jsonPath("$.summaryEn", notNullValue()))
                .andExpect(jsonPath("$.metricsJson", notNullValue()));
    }

    @Test
    void generateReport_whenSprintNotFound_shouldReturn500() throws Exception {
        when(reportService.generateReport(999L))
                .thenThrow(new RuntimeException("Sprint nao encontrado: 999"));

        mockMvc.perform(post("/v1/reports/sprint/999/generate"))
                .andExpect(status().isInternalServerError());
    }
}
