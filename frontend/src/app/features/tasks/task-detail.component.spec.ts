import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { of } from 'rxjs';
import { TaskDetailComponent } from './task-detail.component';
import { TaskService } from '../../core/services/task.service';
import { Task, Prompt } from '../../core/models/task.model';

const mockTask: Task = {
  id: 5, sprintId: 1, sprintNumber: 1, sprintName: 'Fundação',
  taskCode: 'S1-05', sessionDate: '2026-02-07', dayOfWeek: 'SEX',
  weekNumber: 1, plannedHours: 3.5, title: 'Entidades JPA',
  titleEn: 'JPA Entities', description: 'Criar entidades JPA',
  deliverables: ['Sprint.java', 'Task.java'], validationCriteria: ['mvn test'],
  coverageTarget: '80%', status: 'PLANNED', actualHours: 0,
  startedAt: '', completedAt: '', completionNotes: '', blockers: '',
  sortOrder: 5, notes: [], executions: []
};

const mockPrompt: Prompt = {
  taskId: 5, taskCode: 'S1-05', title: 'Entidades JPA',
  prompt: 'Create JPA entities for Sprint and Task'
};

const mockTaskStarted: Task = { ...mockTask, status: 'IN_PROGRESS', startedAt: '2026-02-07T09:00:00' };
const mockTaskCompleted: Task = { ...mockTask, status: 'COMPLETED', completedAt: '2026-02-07T12:00:00', actualHours: 3 };
const mockTaskBlocked: Task = { ...mockTask, status: 'BLOCKED', blockers: 'Missing dependency' };

describe('TaskDetailComponent', () => {
  let component: TaskDetailComponent;
  let fixture: ComponentFixture<TaskDetailComponent>;
  let mockTaskService: {
    findById: jest.Mock;
    start: jest.Mock;
    complete: jest.Mock;
    block: jest.Mock;
    skip: jest.Mock;
    getPrompt: jest.Mock;
    addNote: jest.Mock;
    getExecutions: jest.Mock;
  };
  let mockSnackBar: { open: jest.Mock };
  let mockDialog: { open: jest.Mock };

  beforeEach(async () => {
    mockTaskService = {
      findById: jest.fn().mockReturnValue(of(mockTask)),
      start: jest.fn().mockReturnValue(of(mockTaskStarted)),
      complete: jest.fn().mockReturnValue(of(mockTaskCompleted)),
      block: jest.fn().mockReturnValue(of(mockTaskBlocked)),
      skip: jest.fn().mockReturnValue(of({ ...mockTask, status: 'SKIPPED' })),
      getPrompt: jest.fn().mockReturnValue(of(mockPrompt)),
      addNote: jest.fn().mockReturnValue(of({ id: 1, taskId: 5, noteType: 'INFO', content: 'Test note', author: 'admin', createdAt: '2026-02-07' })),
      getExecutions: jest.fn().mockReturnValue(of([]))
    };

    mockSnackBar = {
      open: jest.fn()
    };

    mockDialog = {
      open: jest.fn().mockReturnValue({ afterClosed: () => of('Missing dependency') })
    };

    await TestBed.configureTestingModule({
      imports: [TaskDetailComponent, NoopAnimationsModule],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => '5' } } }
        },
        { provide: TaskService, useValue: mockTaskService },
        { provide: MatSnackBar, useValue: mockSnackBar },
        { provide: MatDialog, useValue: mockDialog }
      ]
    }).overrideComponent(TaskDetailComponent, {
      add: { providers: [
        { provide: MatSnackBar, useValue: mockSnackBar },
        { provide: MatDialog, useValue: mockDialog }
      ] }
    }).compileComponents();

    fixture = TestBed.createComponent(TaskDetailComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have null task and prompt initially', () => {
    expect(component.task).toBeNull();
    expect(component.prompt).toBeNull();
    expect(component.newNote).toBe('');
  });

  it('should call taskService.findById with route id on init', () => {
    fixture.detectChanges();
    expect(mockTaskService.findById).toHaveBeenCalledWith(5);
  });

  it('should populate task after init', () => {
    fixture.detectChanges();
    expect(component.task).toBe(mockTask);
  });

  it('should call taskService.start and update task on startTask', () => {
    fixture.detectChanges();
    component.startTask();
    expect(mockTaskService.start).toHaveBeenCalledWith(5);
    expect(component.task).toBe(mockTaskStarted);
    expect(mockSnackBar.open).toHaveBeenCalledWith('Tarefa iniciada!', 'OK', { duration: 3000 });
  });

  it('should not call start if task is null', () => {
    component.task = null;
    component.startTask();
    expect(mockTaskService.start).not.toHaveBeenCalled();
  });

  it('should call taskService.complete and update task on completeTask', () => {
    fixture.detectChanges();
    component.completeTask();
    expect(mockTaskService.complete).toHaveBeenCalledWith(5, undefined);
    expect(component.task).toBe(mockTaskCompleted);
    expect(mockSnackBar.open).toHaveBeenCalledWith('Tarefa concluída!', 'OK', { duration: 3000 });
  });

  it('should not call complete if task is null', () => {
    component.task = null;
    component.completeTask();
    expect(mockTaskService.complete).not.toHaveBeenCalled();
  });

  it('should call taskService.block with reason on blockTask', () => {
    fixture.detectChanges();
    component.blockTask();
    expect(mockDialog.open).toHaveBeenCalled();
    expect(mockTaskService.block).toHaveBeenCalledWith(5, 'Missing dependency');
    expect(component.task).toBe(mockTaskBlocked);
    expect(mockSnackBar.open).toHaveBeenCalledWith('Tarefa bloqueada', 'OK', { duration: 3000 });
  });

  it('should not call block if task is null', () => {
    component.task = null;
    component.blockTask();
    expect(mockTaskService.block).not.toHaveBeenCalled();
  });

  it('should not call block if user cancels the dialog', () => {
    fixture.detectChanges();
    mockDialog.open.mockReturnValue({ afterClosed: () => of(undefined) });
    component.blockTask();
    expect(mockTaskService.block).not.toHaveBeenCalled();
  });

  it('should not call block if user enters empty reason', () => {
    fixture.detectChanges();
    mockDialog.open.mockReturnValue({ afterClosed: () => of('') });
    component.blockTask();
    expect(mockTaskService.block).not.toHaveBeenCalled();
  });

  it('should call taskService.getPrompt on loadPrompt', () => {
    fixture.detectChanges();
    component.loadPrompt();
    expect(mockTaskService.getPrompt).toHaveBeenCalledWith(5);
    expect(component.prompt).toBe(mockPrompt);
  });

  it('should not call getPrompt if task is null', () => {
    component.task = null;
    component.loadPrompt();
    expect(mockTaskService.getPrompt).not.toHaveBeenCalled();
  });

  it('should call addNote and reload task', () => {
    fixture.detectChanges();
    mockTaskService.findById.mockClear();
    component.newNote = 'Test note';
    component.addNote();
    expect(mockTaskService.addNote).toHaveBeenCalledWith(5, { content: 'Test note', noteType: 'INFO' });
    expect(component.newNote).toBe('');
    expect(mockTaskService.findById).toHaveBeenCalledWith(5);
  });

  it('should not call addNote if task is null', () => {
    component.task = null;
    component.newNote = 'Test';
    component.addNote();
    expect(mockTaskService.addNote).not.toHaveBeenCalled();
  });

  it('should not call addNote if newNote is empty or whitespace', () => {
    fixture.detectChanges();
    component.newNote = '   ';
    component.addNote();
    expect(mockTaskService.addNote).not.toHaveBeenCalled();
  });
});
