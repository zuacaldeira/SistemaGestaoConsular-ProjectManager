package ao.gov.sgcd.pm.service;

import ao.gov.sgcd.pm.dto.TaskDTO;
import ao.gov.sgcd.pm.dto.TaskNoteDTO;
import ao.gov.sgcd.pm.dto.TaskUpdateDTO;
import ao.gov.sgcd.pm.entity.*;
import ao.gov.sgcd.pm.mapper.TaskExecutionMapper;
import ao.gov.sgcd.pm.mapper.TaskMapper;
import ao.gov.sgcd.pm.mapper.TaskNoteMapper;
import ao.gov.sgcd.pm.repository.SprintReportRepository;
import ao.gov.sgcd.pm.repository.SprintRepository;
import ao.gov.sgcd.pm.repository.TaskExecutionRepository;
import ao.gov.sgcd.pm.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskExecutionRepository executionRepository;

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private SprintReportRepository reportRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TaskNoteMapper noteMapper;

    @Mock
    private TaskExecutionMapper executionMapper;

    @InjectMocks
    private TaskService taskService;

    // --- Helper builders ---

    private Sprint buildSprint(Long id, int number, SprintStatus status,
                               int totalSessions, int completedSessions) {
        return Sprint.builder()
                .id(id)
                .sprintNumber(number)
                .name("Sprint " + number)
                .nameEn("Sprint " + number + " EN")
                .status(status)
                .totalSessions(totalSessions)
                .completedSessions(completedSessions)
                .totalHours(totalSessions * 3)
                .actualHours(BigDecimal.valueOf(completedSessions * 3))
                .weeks(4)
                .startDate(LocalDate.of(2026, 3, 2))
                .endDate(LocalDate.of(2026, 4, 12))
                .build();
    }

    private Task buildTask(Long id, String taskCode, Sprint sprint, TaskStatus status) {
        return Task.builder()
                .id(id)
                .taskCode(taskCode)
                .title("Tarefa " + taskCode)
                .sprint(sprint)
                .status(status)
                .sessionDate(LocalDate.of(2026, 3, 10))
                .dayOfWeek("TER")
                .weekNumber(2)
                .plannedHours(BigDecimal.valueOf(3.5))
                .sortOrder(1)
                .notes(new ArrayList<>())
                .executions(new ArrayList<>())
                .build();
    }

    private TaskDTO buildTaskDto(Long id, String taskCode, TaskStatus status) {
        return TaskDTO.builder()
                .id(id)
                .taskCode(taskCode)
                .title("Tarefa " + taskCode)
                .status(status)
                .build();
    }

    // =====================================================================
    // findFiltered
    // =====================================================================

    @Test
    void findFiltered_shouldReturnMappedPage() {
        // given
        Sprint sprint = buildSprint(1L, 1, SprintStatus.ACTIVE, 36, 10);
        Task task = buildTask(1L, "S1-01", sprint, TaskStatus.PLANNED);
        TaskDTO dto = buildTaskDto(1L, "S1-01", TaskStatus.PLANNED);

        Pageable pageable = PageRequest.of(0, 20);
        Page<Task> page = new PageImpl<>(List.of(task), pageable, 1);

        when(taskRepository.findFiltered(eq(1L), eq(TaskStatus.PLANNED), any(), any(), eq(pageable)))
                .thenReturn(page);
        when(taskMapper.toDto(task)).thenReturn(dto);

        // when
        Page<TaskDTO> result = taskService.findFiltered(1L, TaskStatus.PLANNED, null, null, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("S1-01", result.getContent().get(0).getTaskCode());
    }

    @Test
    void findFiltered_shouldReturnEmptyPageWhenNoResults() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Task> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(taskRepository.findFiltered(any(), any(), any(), any(), eq(pageable))).thenReturn(emptyPage);

        // when
        Page<TaskDTO> result = taskService.findFiltered(null, null, null, null, pageable);

        // then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    // =====================================================================
    // findById
    // =====================================================================

    @Test
    void findById_shouldReturnTaskDto() {
        // given
        Sprint sprint = buildSprint(1L, 1, SprintStatus.ACTIVE, 36, 10);
        Task task = buildTask(1L, "S1-01", sprint, TaskStatus.PLANNED);
        TaskDTO dto = buildTaskDto(1L, "S1-01", TaskStatus.PLANNED);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskMapper.toDto(task)).thenReturn(dto);

        // when
        TaskDTO result = taskService.findById(1L);

        // then
        assertNotNull(result);
        assertEquals("S1-01", result.getTaskCode());
        verify(taskRepository).findById(1L);
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        // given
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        // when & then
        RuntimeException ex = assertThrows(RuntimeException.class, () -> taskService.findById(99L));
        assertTrue(ex.getMessage().contains("Tarefa não encontrada"));
        assertTrue(ex.getMessage().contains("99"));
    }

    // =====================================================================
    // findToday
    // =====================================================================

    @Test
    void findToday_shouldReturnTodayTask() {
        // given
        Sprint sprint = buildSprint(1L, 1, SprintStatus.ACTIVE, 36, 10);
        Task task = buildTask(1L, "S1-05", sprint, TaskStatus.PLANNED);
        task.setSessionDate(LocalDate.now());
        TaskDTO dto = buildTaskDto(1L, "S1-05", TaskStatus.PLANNED);

        when(taskRepository.findBySessionDate(LocalDate.now())).thenReturn(Optional.of(task));
        when(taskMapper.toDto(task)).thenReturn(dto);

        // when
        TaskDTO result = taskService.findToday();

        // then
        assertNotNull(result);
        assertEquals("S1-05", result.getTaskCode());
    }

    @Test
    void findToday_shouldFallBackToUpcomingPlanned() {
        // given
        when(taskRepository.findBySessionDate(any(LocalDate.class))).thenReturn(Optional.empty());

        Sprint sprint = buildSprint(1L, 1, SprintStatus.ACTIVE, 36, 10);
        Task upcoming = buildTask(2L, "S1-06", sprint, TaskStatus.PLANNED);
        TaskDTO dto = buildTaskDto(2L, "S1-06", TaskStatus.PLANNED);

        when(taskRepository.findUpcomingPlanned(any(LocalDate.class))).thenReturn(List.of(upcoming));
        when(taskMapper.toDto(upcoming)).thenReturn(dto);

        // when
        TaskDTO result = taskService.findToday();

        // then
        assertNotNull(result);
        assertEquals("S1-06", result.getTaskCode());
    }

    @Test
    void findToday_shouldReturnNullWhenNoTaskFound() {
        // given
        when(taskRepository.findBySessionDate(any(LocalDate.class))).thenReturn(Optional.empty());
        when(taskRepository.findUpcomingPlanned(any(LocalDate.class))).thenReturn(Collections.emptyList());

        // when
        TaskDTO result = taskService.findToday();

        // then
        assertNull(result);
    }

    // =====================================================================
    // findNext
    // =====================================================================

    @Test
    void findNext_shouldReturnFirstPlannedTask() {
        // given
        Sprint sprint = buildSprint(1L, 1, SprintStatus.ACTIVE, 36, 10);
        Task next = buildTask(3L, "S1-07", sprint, TaskStatus.PLANNED);
        TaskDTO dto = buildTaskDto(3L, "S1-07", TaskStatus.PLANNED);

        when(taskRepository.findNextPlanned()).thenReturn(List.of(next));
        when(taskMapper.toDto(next)).thenReturn(dto);

        // when
        TaskDTO result = taskService.findNext();

        // then
        assertNotNull(result);
        assertEquals("S1-07", result.getTaskCode());
    }

    @Test
    void findNext_shouldReturnNullWhenNoneAvailable() {
        // given
        when(taskRepository.findNextPlanned()).thenReturn(Collections.emptyList());

        // when
        TaskDTO result = taskService.findNext();

        // then
        assertNull(result);
    }

    // =====================================================================
    // update
    // =====================================================================

    @Test
    void update_shouldApplyPartialUpdates() {
        // given
        Sprint sprint = buildSprint(1L, 1, SprintStatus.ACTIVE, 36, 10);
        Task task = buildTask(1L, "S1-01", sprint, TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskUpdateDTO updateDto = TaskUpdateDTO.builder()
                .completionNotes("Notas de conclusao")
                .blockers("Nenhum blocker")
                .actualHours(BigDecimal.valueOf(4.0))
                .description("Descricao actualizada")
                .build();

        TaskDTO resultDto = buildTaskDto(1L, "S1-01", TaskStatus.IN_PROGRESS);
        when(taskMapper.toDto(any(Task.class))).thenReturn(resultDto);

        // when
        TaskDTO result = taskService.update(1L, updateDto);

        // then
        assertNotNull(result);
        assertEquals("Notas de conclusao", task.getCompletionNotes());
        assertEquals("Nenhum blocker", task.getBlockers());
        assertEquals(BigDecimal.valueOf(4.0), task.getActualHours());
        assertEquals("Descricao actualizada", task.getDescription());
        verify(taskRepository).save(task);
    }

    @Test
    void update_shouldSkipNullFields() {
        // given
        Sprint sprint = buildSprint(1L, 1, SprintStatus.ACTIVE, 36, 10);
        Task task = buildTask(1L, "S1-01", sprint, TaskStatus.IN_PROGRESS);
        task.setCompletionNotes("Original notes");
        task.setDescription("Original description");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskUpdateDTO updateDto = TaskUpdateDTO.builder().build(); // all null

        TaskDTO resultDto = buildTaskDto(1L, "S1-01", TaskStatus.IN_PROGRESS);
        when(taskMapper.toDto(any(Task.class))).thenReturn(resultDto);

        // when
        taskService.update(1L, updateDto);

        // then - original values preserved
        assertEquals("Original notes", task.getCompletionNotes());
        assertEquals("Original description", task.getDescription());
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        // given
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        TaskUpdateDTO dto = TaskUpdateDTO.builder().build();

        // when & then
        assertThrows(RuntimeException.class, () -> taskService.update(99L, dto));
    }

    // =====================================================================
    // startTask
    // =====================================================================

    @Test
    void startTask_shouldSetInProgressAndStartedAt() {
        // given
        Sprint sprint = buildSprint(1L, 1, SprintStatus.ACTIVE, 36, 10);
        Task task = buildTask(1L, "S1-01", sprint, TaskStatus.PLANNED);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskDTO dto = buildTaskDto(1L, "S1-01", TaskStatus.IN_PROGRESS);
        when(taskMapper.toDto(any(Task.class))).thenReturn(dto);

        // when
        TaskDTO result = taskService.startTask(1L);

        // then
        assertNotNull(result);
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        assertNotNull(task.getStartedAt());
        verify(taskRepository).save(task);
        // Sprint already ACTIVE, so should NOT be saved
        verify(sprintRepository, never()).save(any());
    }

    @Test
    void startTask_shouldActivatePlannedSprint() {
        // given
        Sprint sprint = buildSprint(1L, 1, SprintStatus.PLANNED, 36, 0);
        Task task = buildTask(1L, "S1-01", sprint, TaskStatus.PLANNED);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskDTO dto = buildTaskDto(1L, "S1-01", TaskStatus.IN_PROGRESS);
        when(taskMapper.toDto(any(Task.class))).thenReturn(dto);

        // when
        taskService.startTask(1L);

        // then
        assertEquals(SprintStatus.ACTIVE, sprint.getStatus());
        verify(sprintRepository).save(sprint);
    }

    @Test
    void startTask_shouldThrowWhenNotFound() {
        // given
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(RuntimeException.class, () -> taskService.startTask(99L));
    }

    // =====================================================================
    // completeTask
    // =====================================================================

    @Test
    void completeTask_shouldMarkCompletedWithDtoActualHours() {
        // given
        Sprint sprint = buildSprint(1L, 1, SprintStatus.ACTIVE, 36, 10);
        Task task = buildTask(1L, "S1-11", sprint, TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Remaining: still have tasks
        when(taskRepository.countBySprintIdAndStatus(1L, TaskStatus.PLANNED)).thenReturn(5);
        when(taskRepository.countBySprintIdAndStatus(1L, TaskStatus.IN_PROGRESS)).thenReturn(0);

        TaskUpdateDTO updateDto = TaskUpdateDTO.builder()
                .actualHours(BigDecimal.valueOf(4.0))
                .completionNotes("Tudo feito")
                .build();

        TaskDTO resultDto = buildTaskDto(1L, "S1-11", TaskStatus.COMPLETED);
        when(taskMapper.toDto(any(Task.class))).thenReturn(resultDto);

        // when
        TaskDTO result = taskService.completeTask(1L, updateDto);

        // then
        assertNotNull(result);
        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        assertNotNull(task.getCompletedAt());
        assertEquals(BigDecimal.valueOf(4.0), task.getActualHours());
        assertEquals("Tudo feito", task.getCompletionNotes());

        // Sprint metrics updated
        assertEquals(11, sprint.getCompletedSessions()); // 10 + 1
        assertEquals(BigDecimal.valueOf(30).add(BigDecimal.valueOf(4.0)), sprint.getActualHours()); // 30 + 4.0

        // Sprint NOT completed (remaining > 0)
        assertEquals(SprintStatus.ACTIVE, sprint.getStatus());
        verify(sprintRepository).save(sprint);
    }

    @Test
    void completeTask_shouldUseDefaultPlannedHoursWhenDtoIsNull() {
        // given
        Sprint sprint = buildSprint(1L, 1, SprintStatus.ACTIVE, 36, 10);
        Task task = buildTask(1L, "S1-11", sprint, TaskStatus.IN_PROGRESS);
        task.setPlannedHours(BigDecimal.valueOf(3.5));

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        when(taskRepository.countBySprintIdAndStatus(1L, TaskStatus.PLANNED)).thenReturn(5);
        when(taskRepository.countBySprintIdAndStatus(1L, TaskStatus.IN_PROGRESS)).thenReturn(0);

        TaskDTO resultDto = buildTaskDto(1L, "S1-11", TaskStatus.COMPLETED);
        when(taskMapper.toDto(any(Task.class))).thenReturn(resultDto);

        // when (null dto)
        taskService.completeTask(1L, null);

        // then
        assertEquals(BigDecimal.valueOf(3.5), task.getActualHours());
    }

    @Test
    void completeTask_shouldUseDefaultPlannedHoursWhenDtoActualHoursIsNull() {
        // given
        Sprint sprint = buildSprint(1L, 1, SprintStatus.ACTIVE, 36, 10);
        Task task = buildTask(1L, "S1-11", sprint, TaskStatus.IN_PROGRESS);
        task.setPlannedHours(BigDecimal.valueOf(3.5));

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        when(taskRepository.countBySprintIdAndStatus(1L, TaskStatus.PLANNED)).thenReturn(5);
        when(taskRepository.countBySprintIdAndStatus(1L, TaskStatus.IN_PROGRESS)).thenReturn(0);

        TaskUpdateDTO updateDto = TaskUpdateDTO.builder()
                .completionNotes("Notas")
                .build(); // actualHours is null

        TaskDTO resultDto = buildTaskDto(1L, "S1-11", TaskStatus.COMPLETED);
        when(taskMapper.toDto(any(Task.class))).thenReturn(resultDto);

        // when
        taskService.completeTask(1L, updateDto);

        // then
        assertEquals(BigDecimal.valueOf(3.5), task.getActualHours());
        assertEquals("Notas", task.getCompletionNotes());
    }

    @Test
    void completeTask_shouldCompleteSprintAndActivateNextWhenLastTask() {
        // given
        Sprint sprint = buildSprint(1L, 1, SprintStatus.ACTIVE, 36, 35);
        Task task = buildTask(36L, "S1-36", sprint, TaskStatus.IN_PROGRESS);
        task.setPlannedHours(BigDecimal.valueOf(3.0));

        when(taskRepository.findById(36L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // remaining == 0
        when(taskRepository.countBySprintIdAndStatus(1L, TaskStatus.PLANNED)).thenReturn(0);
        when(taskRepository.countBySprintIdAndStatus(1L, TaskStatus.IN_PROGRESS)).thenReturn(0);

        // Next planned sprint
        Sprint nextSprint = buildSprint(2L, 2, SprintStatus.PLANNED, 40, 0);
        when(sprintRepository.findFirstByStatusOrderBySprintNumberAsc(SprintStatus.PLANNED))
                .thenReturn(Optional.of(nextSprint));

        TaskDTO resultDto = buildTaskDto(36L, "S1-36", TaskStatus.COMPLETED);
        when(taskMapper.toDto(any(Task.class))).thenReturn(resultDto);

        TaskUpdateDTO updateDto = TaskUpdateDTO.builder()
                .actualHours(BigDecimal.valueOf(3.0))
                .build();

        // when
        taskService.completeTask(36L, updateDto);

        // then
        // Current sprint should be COMPLETED
        assertEquals(SprintStatus.COMPLETED, sprint.getStatus());
        assertEquals(36, sprint.getCompletedSessions()); // 35 + 1

        // Next sprint should be ACTIVE
        assertEquals(SprintStatus.ACTIVE, nextSprint.getStatus());

        // Verify saves: current sprint saved as COMPLETED, next sprint saved as ACTIVE
        verify(sprintRepository, times(2)).save(any(Sprint.class));
    }

    @Test
    void completeTask_shouldCompleteSprintWithoutActivatingNextWhenNoneAvailable() {
        // given
        Sprint sprint = buildSprint(6L, 6, SprintStatus.ACTIVE, 30, 29);
        Task task = buildTask(204L, "S6-30", sprint, TaskStatus.IN_PROGRESS);
        task.setPlannedHours(BigDecimal.valueOf(3.0));

        when(taskRepository.findById(204L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // remaining == 0
        when(taskRepository.countBySprintIdAndStatus(6L, TaskStatus.PLANNED)).thenReturn(0);
        when(taskRepository.countBySprintIdAndStatus(6L, TaskStatus.IN_PROGRESS)).thenReturn(0);

        // No next planned sprint
        when(sprintRepository.findFirstByStatusOrderBySprintNumberAsc(SprintStatus.PLANNED))
                .thenReturn(Optional.empty());

        TaskDTO resultDto = buildTaskDto(204L, "S6-30", TaskStatus.COMPLETED);
        when(taskMapper.toDto(any(Task.class))).thenReturn(resultDto);

        // when
        taskService.completeTask(204L, null);

        // then
        assertEquals(SprintStatus.COMPLETED, sprint.getStatus());
        // Only the current sprint was saved (once as COMPLETED)
        verify(sprintRepository, times(1)).save(sprint);
    }

    @Test
    void completeTask_shouldNotCompleteSprintWhenRemainingTasksExist() {
        // given
        Sprint sprint = buildSprint(1L, 1, SprintStatus.ACTIVE, 36, 10);
        Task task = buildTask(11L, "S1-11", sprint, TaskStatus.IN_PROGRESS);
        task.setPlannedHours(BigDecimal.valueOf(3.5));

        when(taskRepository.findById(11L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // remaining > 0
        when(taskRepository.countBySprintIdAndStatus(1L, TaskStatus.PLANNED)).thenReturn(20);
        when(taskRepository.countBySprintIdAndStatus(1L, TaskStatus.IN_PROGRESS)).thenReturn(2);

        TaskDTO resultDto = buildTaskDto(11L, "S1-11", TaskStatus.COMPLETED);
        when(taskMapper.toDto(any(Task.class))).thenReturn(resultDto);

        // when
        taskService.completeTask(11L, null);

        // then
        assertEquals(SprintStatus.ACTIVE, sprint.getStatus()); // NOT completed
        verify(sprintRepository).save(sprint); // still saved for metric updates
        verify(sprintRepository, never()).findFirstByStatusOrderBySprintNumberAsc(any());
    }

    @Test
    void completeTask_shouldThrowWhenNotFound() {
        // given
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(RuntimeException.class, () -> taskService.completeTask(99L, null));
    }

    // =====================================================================
    // blockTask
    // =====================================================================

    @Test
    void blockTask_shouldSetBlockedStatusAndReason() {
        // given
        Sprint sprint = buildSprint(1L, 1, SprintStatus.ACTIVE, 36, 10);
        Task task = buildTask(1L, "S1-01", sprint, TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskDTO dto = buildTaskDto(1L, "S1-01", TaskStatus.BLOCKED);
        when(taskMapper.toDto(any(Task.class))).thenReturn(dto);

        // when
        TaskDTO result = taskService.blockTask(1L, "Dependencia externa em atraso");

        // then
        assertNotNull(result);
        assertEquals(TaskStatus.BLOCKED, task.getStatus());
        assertEquals("Dependencia externa em atraso", task.getBlockers());
        verify(taskRepository).save(task);
    }

    @Test
    void blockTask_shouldThrowWhenNotFound() {
        // given
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(RuntimeException.class, () -> taskService.blockTask(99L, "reason"));
    }

    // =====================================================================
    // skipTask
    // =====================================================================

    @Test
    void skipTask_shouldSetSkippedStatus() {
        // given
        Sprint sprint = buildSprint(1L, 1, SprintStatus.ACTIVE, 36, 10);
        Task task = buildTask(1L, "S1-01", sprint, TaskStatus.PLANNED);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskDTO dto = buildTaskDto(1L, "S1-01", TaskStatus.SKIPPED);
        when(taskMapper.toDto(any(Task.class))).thenReturn(dto);

        // when
        TaskDTO result = taskService.skipTask(1L);

        // then
        assertNotNull(result);
        assertEquals(TaskStatus.SKIPPED, task.getStatus());
        verify(taskRepository).save(task);
    }

    @Test
    void skipTask_shouldThrowWhenNotFound() {
        // given
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(RuntimeException.class, () -> taskService.skipTask(99L));
    }

    // =====================================================================
    // addNote
    // =====================================================================

    @Test
    void addNote_shouldCreateNoteAndAddToTask() {
        // given
        Sprint sprint = buildSprint(1L, 1, SprintStatus.ACTIVE, 36, 10);
        Task task = buildTask(1L, "S1-01", sprint, TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskNoteDTO inputDto = TaskNoteDTO.builder()
                .noteType(NoteType.INFO)
                .content("Nota de progresso")
                .author("developer")
                .build();

        TaskNoteDTO outputDto = TaskNoteDTO.builder()
                .id(1L)
                .taskId(1L)
                .noteType(NoteType.INFO)
                .content("Nota de progresso")
                .author("developer")
                .build();

        when(noteMapper.toDto(any(TaskNote.class))).thenReturn(outputDto);

        // when
        TaskNoteDTO result = taskService.addNote(1L, inputDto);

        // then
        assertNotNull(result);
        assertEquals("Nota de progresso", result.getContent());
        assertEquals(NoteType.INFO, result.getNoteType());
        assertEquals(1, task.getNotes().size());
        verify(taskRepository).save(task);
    }

    @Test
    void addNote_shouldThrowWhenTaskNotFound() {
        // given
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        TaskNoteDTO dto = TaskNoteDTO.builder().build();

        // when & then
        assertThrows(RuntimeException.class, () -> taskService.addNote(99L, dto));
    }

    // =====================================================================
    // findRecent
    // =====================================================================

    @Test
    void findRecent_shouldReturnLimitedCompletedTasks() {
        // given
        Sprint sprint = buildSprint(1L, 1, SprintStatus.ACTIVE, 36, 10);
        Task task1 = buildTask(1L, "S1-01", sprint, TaskStatus.COMPLETED);
        Task task2 = buildTask(2L, "S1-02", sprint, TaskStatus.COMPLETED);

        when(taskRepository.findRecentCompleted(PageRequest.of(0, 5))).thenReturn(List.of(task1, task2));

        TaskDTO dto1 = buildTaskDto(1L, "S1-01", TaskStatus.COMPLETED);
        TaskDTO dto2 = buildTaskDto(2L, "S1-02", TaskStatus.COMPLETED);
        when(taskMapper.toDto(task1)).thenReturn(dto1);
        when(taskMapper.toDto(task2)).thenReturn(dto2);

        // when
        List<TaskDTO> result = taskService.findRecent(5);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(taskRepository).findRecentCompleted(PageRequest.of(0, 5));
    }

    @Test
    void findRecent_shouldReturnEmptyListWhenNone() {
        // given
        when(taskRepository.findRecentCompleted(PageRequest.of(0, 3))).thenReturn(Collections.emptyList());

        // when
        List<TaskDTO> result = taskService.findRecent(3);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
