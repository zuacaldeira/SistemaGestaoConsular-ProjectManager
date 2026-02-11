package ao.gov.sgcd.pm.service;

import ao.gov.sgcd.pm.dto.ReportDTO;
import ao.gov.sgcd.pm.entity.*;
import ao.gov.sgcd.pm.mapper.ReportMapper;
import ao.gov.sgcd.pm.repository.SprintReportRepository;
import ao.gov.sgcd.pm.repository.SprintRepository;
import ao.gov.sgcd.pm.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private SprintReportRepository reportRepository;

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ReportMapper reportMapper;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PdfExportService pdfExportService;

    @InjectMocks
    private ReportService reportService;

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
                .focus("Backend")
                .color("#CC092F")
                .build();
    }

    private SprintReport buildReport(Long id, Sprint sprint) {
        return SprintReport.builder()
                .id(id)
                .sprint(sprint)
                .reportType(ReportType.SPRINT_END)
                .generatedAt(LocalDateTime.of(2026, 4, 12, 18, 0))
                .summaryPt("Resumo em portugues")
                .summaryEn("Summary in English")
                .metricsJson("{\"totalSessions\": 36}")
                .build();
    }

    private ReportDTO buildReportDto(Long id, Long sprintId, int sprintNumber) {
        return ReportDTO.builder()
                .id(id)
                .sprintId(sprintId)
                .sprintNumber(sprintNumber)
                .sprintName("Sprint " + sprintNumber)
                .reportType(ReportType.SPRINT_END)
                .generatedAt(LocalDateTime.of(2026, 4, 12, 18, 0))
                .summaryPt("Resumo em portugues")
                .summaryEn("Summary in English")
                .metricsJson("{\"totalSessions\": 36}")
                .build();
    }

    // =====================================================================
    // findAll
    // =====================================================================

    @Test
    void findAll_shouldReturnAllReportsOrdered() {
        // given
        Sprint sprint1 = buildSprint(1L, 1, "Fundacao", SprintStatus.COMPLETED, 36, 36);
        Sprint sprint2 = buildSprint(2L, 2, "Backend Core", SprintStatus.COMPLETED, 40, 40);

        SprintReport report1 = buildReport(1L, sprint1);
        SprintReport report2 = buildReport(2L, sprint2);

        ReportDTO dto1 = buildReportDto(1L, 1L, 1);
        ReportDTO dto2 = buildReportDto(2L, 2L, 2);

        when(reportRepository.findAllByOrderByGeneratedAtDesc()).thenReturn(List.of(report2, report1));
        when(reportMapper.toDtoList(List.of(report2, report1))).thenReturn(List.of(dto2, dto1));

        // when
        List<ReportDTO> result = reportService.findAll();

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2, result.get(0).getSprintNumber()); // most recent first
        assertEquals(1, result.get(1).getSprintNumber());
        verify(reportRepository).findAllByOrderByGeneratedAtDesc();
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoReports() {
        // given
        when(reportRepository.findAllByOrderByGeneratedAtDesc()).thenReturn(Collections.emptyList());
        when(reportMapper.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        // when
        List<ReportDTO> result = reportService.findAll();

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // =====================================================================
    // findBySprintId
    // =====================================================================

    @Test
    void findBySprintId_shouldReturnLatestReport() {
        // given
        Sprint sprint = buildSprint(1L, 1, "Fundacao", SprintStatus.COMPLETED, 36, 36);
        SprintReport report = buildReport(1L, sprint);
        ReportDTO dto = buildReportDto(1L, 1L, 1);

        when(reportRepository.findBySprintIdOrderByGeneratedAtDesc(1L)).thenReturn(List.of(report));
        when(reportMapper.toDto(report)).thenReturn(dto);

        // when
        ReportDTO result = reportService.findBySprintId(1L);

        // then
        assertNotNull(result);
        assertEquals(1L, result.getSprintId());
        assertEquals(1, result.getSprintNumber());
    }

    @Test
    void findBySprintId_shouldReturnFirstWhenMultipleReportsExist() {
        // given
        Sprint sprint = buildSprint(1L, 1, "Fundacao", SprintStatus.COMPLETED, 36, 36);
        SprintReport report1 = buildReport(1L, sprint);
        report1.setGeneratedAt(LocalDateTime.of(2026, 4, 12, 18, 0));
        SprintReport report2 = buildReport(2L, sprint);
        report2.setGeneratedAt(LocalDateTime.of(2026, 4, 10, 12, 0));

        ReportDTO dto1 = buildReportDto(1L, 1L, 1);

        when(reportRepository.findBySprintIdOrderByGeneratedAtDesc(1L)).thenReturn(List.of(report1, report2));
        when(reportMapper.toDto(report1)).thenReturn(dto1);

        // when
        ReportDTO result = reportService.findBySprintId(1L);

        // then
        assertNotNull(result);
        assertEquals(1L, result.getId()); // latest report
    }

    @Test
    void findBySprintId_shouldThrowWhenNoReportsFound() {
        // given
        when(reportRepository.findBySprintIdOrderByGeneratedAtDesc(99L)).thenReturn(Collections.emptyList());

        // when & then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reportService.findBySprintId(99L));
        assertTrue(ex.getMessage().contains("Relatório não encontrado para sprint"));
        assertTrue(ex.getMessage().contains("99"));
    }

    // =====================================================================
    // generateReport
    // =====================================================================

    @Test
    void generateReport_shouldCreateReportWithCorrectMetrics() {
        // given
        Sprint sprint = buildSprint(1L, 1, "Fundacao", SprintStatus.COMPLETED, 36, 36);
        sprint.setActualHours(BigDecimal.valueOf(108.0));

        when(sprintRepository.findById(1L)).thenReturn(Optional.of(sprint));
        when(taskRepository.countBySprintIdAndStatus(1L, TaskStatus.COMPLETED)).thenReturn(32);
        when(taskRepository.countBySprintIdAndStatus(1L, TaskStatus.BLOCKED)).thenReturn(2);
        when(taskRepository.countBySprintIdAndStatus(1L, TaskStatus.SKIPPED)).thenReturn(2);

        ReportDTO expectedDto = buildReportDto(1L, 1L, 1);
        when(reportRepository.save(any(SprintReport.class))).thenAnswer(invocation -> {
            SprintReport saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(reportMapper.toDto(any(SprintReport.class))).thenReturn(expectedDto);

        // when
        ReportDTO result = reportService.generateReport(1L);

        // then
        assertNotNull(result);

        // Capture the saved report to verify its contents
        ArgumentCaptor<SprintReport> captor = ArgumentCaptor.forClass(SprintReport.class);
        verify(reportRepository).save(captor.capture());

        SprintReport saved = captor.getValue();
        assertEquals(sprint, saved.getSprint());
        assertEquals(ReportType.SPRINT_END, saved.getReportType());
        assertNotNull(saved.getGeneratedAt());

        // Verify summary in Portuguese
        assertNotNull(saved.getSummaryPt());
        assertTrue(saved.getSummaryPt().contains("Sprint 1"));
        assertTrue(saved.getSummaryPt().contains("Fundacao"));
        assertTrue(saved.getSummaryPt().contains("32/36"));
        assertTrue(saved.getSummaryPt().contains("2 tarefas bloqueadas"));
        assertTrue(saved.getSummaryPt().contains("2 ignoradas"));

        // Verify summary in English
        assertNotNull(saved.getSummaryEn());
        assertTrue(saved.getSummaryEn().contains("Sprint 1"));
        assertTrue(saved.getSummaryEn().contains("32/36"));
        assertTrue(saved.getSummaryEn().contains("2 blocked"));
        assertTrue(saved.getSummaryEn().contains("2 skipped"));

        // Verify metrics JSON
        assertNotNull(saved.getMetricsJson());
        assertTrue(saved.getMetricsJson().contains("\"totalSessions\""));
        assertTrue(saved.getMetricsJson().contains("36"));
        assertTrue(saved.getMetricsJson().contains("\"completedSessions\""));
        assertTrue(saved.getMetricsJson().contains("32"));
        assertTrue(saved.getMetricsJson().contains("\"blockedTasks\""));
        assertTrue(saved.getMetricsJson().contains("\"skippedTasks\""));
    }

    @Test
    void generateReport_shouldCalculateProgressPercentCorrectly() {
        // given
        Sprint sprint = buildSprint(1L, 1, "Fundacao", SprintStatus.ACTIVE, 36, 18);
        sprint.setActualHours(BigDecimal.valueOf(54.0));

        when(sprintRepository.findById(1L)).thenReturn(Optional.of(sprint));
        when(taskRepository.countBySprintIdAndStatus(1L, TaskStatus.COMPLETED)).thenReturn(18);
        when(taskRepository.countBySprintIdAndStatus(1L, TaskStatus.BLOCKED)).thenReturn(0);
        when(taskRepository.countBySprintIdAndStatus(1L, TaskStatus.SKIPPED)).thenReturn(0);

        when(reportRepository.save(any(SprintReport.class))).thenAnswer(i -> i.getArgument(0));
        when(reportMapper.toDto(any(SprintReport.class))).thenReturn(buildReportDto(1L, 1L, 1));

        // when
        reportService.generateReport(1L);

        // then
        ArgumentCaptor<SprintReport> captor = ArgumentCaptor.forClass(SprintReport.class);
        verify(reportRepository).save(captor.capture());

        SprintReport saved = captor.getValue();
        // Progress should be (18 * 100.0) / 36 = 50.0%
        assertTrue(saved.getSummaryPt().contains("50.0%") || saved.getSummaryPt().contains("50,0%"));
        assertTrue(saved.getMetricsJson().contains("50.0"));
    }

    @Test
    void generateReport_shouldHandleZeroTotalSessions() {
        // given
        Sprint sprint = buildSprint(1L, 1, "Fundacao", SprintStatus.PLANNED, 0, 0);
        sprint.setTotalHours(0);
        sprint.setActualHours(BigDecimal.ZERO);

        when(sprintRepository.findById(1L)).thenReturn(Optional.of(sprint));
        when(taskRepository.countBySprintIdAndStatus(eq(1L), any())).thenReturn(0);

        when(reportRepository.save(any(SprintReport.class))).thenAnswer(i -> i.getArgument(0));
        when(reportMapper.toDto(any(SprintReport.class))).thenReturn(buildReportDto(1L, 1L, 1));

        // when
        reportService.generateReport(1L);

        // then
        ArgumentCaptor<SprintReport> captor = ArgumentCaptor.forClass(SprintReport.class);
        verify(reportRepository).save(captor.capture());

        SprintReport saved = captor.getValue();
        assertTrue(saved.getSummaryPt().contains("0/0"));
        assertTrue(saved.getMetricsJson().contains("\"progressPercent\""));
    }

    @Test
    void generateReport_shouldThrowWhenSprintNotFound() {
        // given
        when(sprintRepository.findById(99L)).thenReturn(Optional.empty());

        // when & then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reportService.generateReport(99L));
        assertTrue(ex.getMessage().contains("Sprint não encontrado"));
        assertTrue(ex.getMessage().contains("99"));
    }

    // =====================================================================
    // findById
    // =====================================================================

    @Test
    void findById_shouldReturnReportDto() {
        // given
        Sprint sprint = buildSprint(1L, 1, "Fundacao", SprintStatus.COMPLETED, 36, 36);
        SprintReport report = buildReport(1L, sprint);
        ReportDTO dto = buildReportDto(1L, 1L, 1);

        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(reportMapper.toDto(report)).thenReturn(dto);

        // when
        ReportDTO result = reportService.findById(1L);

        // then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1, result.getSprintNumber());
        verify(reportRepository).findById(1L);
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        // given
        when(reportRepository.findById(99L)).thenReturn(Optional.empty());

        // when & then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reportService.findById(99L));
        assertTrue(ex.getMessage().contains("Relatório não encontrado"));
        assertTrue(ex.getMessage().contains("99"));
    }
}
