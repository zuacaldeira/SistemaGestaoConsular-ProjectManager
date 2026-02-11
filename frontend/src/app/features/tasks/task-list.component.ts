import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { TaskService } from '../../core/services/task.service';
import { SprintService } from '../../core/services/sprint.service';
import { Task } from '../../core/models/task.model';
import { Sprint } from '../../core/models/sprint.model';
import { StatusBadgeComponent } from '../../shared/components/status-badge.component';
import { DatePtPipe } from '../../shared/pipes/date-pt.pipe';
import { HoursPipe } from '../../shared/pipes/hours.pipe';

@Component({
  selector: 'app-task-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, MatTableModule, MatPaginatorModule, MatSelectModule,
    MatFormFieldModule, MatInputModule, StatusBadgeComponent, DatePtPipe, HoursPipe],
  template: `
    <h2>Tarefas</h2>

    <div class="filters">
      <mat-form-field appearance="outline">
        <mat-label>Status</mat-label>
        <mat-select [(ngModel)]="filterStatus" (selectionChange)="resetAndLoad()">
          <mat-option [value]="null">Todos</mat-option>
          <mat-option value="PLANNED">Planeado</mat-option>
          <mat-option value="IN_PROGRESS">Em Progresso</mat-option>
          <mat-option value="COMPLETED">Concluído</mat-option>
          <mat-option value="BLOCKED">Bloqueado</mat-option>
          <mat-option value="SKIPPED">Ignorado</mat-option>
        </mat-select>
      </mat-form-field>

      <mat-form-field appearance="outline">
        <mat-label>Sprint</mat-label>
        <mat-select [(ngModel)]="filterSprint" (selectionChange)="resetAndLoad()">
          <mat-option [value]="null">Todos</mat-option>
          @for (s of sprints; track s.id) {
            <mat-option [value]="s.id">Sprint {{ s.sprintNumber }}: {{ s.name }}</mat-option>
          }
        </mat-select>
      </mat-form-field>

      <mat-form-field appearance="outline">
        <mat-label>De</mat-label>
        <input matInput type="date" [(ngModel)]="filterFrom" (change)="resetAndLoad()">
      </mat-form-field>

      <mat-form-field appearance="outline">
        <mat-label>Até</mat-label>
        <input matInput type="date" [(ngModel)]="filterTo" (change)="resetAndLoad()">
      </mat-form-field>
    </div>

    <div class="task-table">
      @for (task of tasks; track task.id) {
        <a class="task-row" [routerLink]="['/tasks', task.id]">
          <app-status-badge [status]="task.status" />
          <span class="code">{{ task.taskCode }}</span>
          <span class="title">{{ task.title }}</span>
          <span class="sprint">S{{ task.sprintNumber }}</span>
          <span class="date">{{ task.sessionDate | datePt }}</span>
          <span class="day">{{ task.dayOfWeek }}</span>
          <span class="hours">{{ task.plannedHours | hours }}</span>
        </a>
      }
    </div>

    <mat-paginator
      [length]="totalElements"
      [pageSize]="pageSize"
      [pageSizeOptions]="[20, 50, 100]"
      (page)="onPage($event)" />
  `,
  styles: [`
    .filters { display: flex; gap: 16px; margin-bottom: 16px; flex-wrap: wrap; }
    mat-form-field { width: 200px; }
    .task-table { display: flex; flex-direction: column; gap: 2px; margin-bottom: 16px; }
    .task-row {
      display: flex; align-items: center; gap: 12px; padding: 10px 12px;
      text-decoration: none; color: inherit; border-radius: 6px; transition: background 0.2s;
      background: var(--surface);
    }
    .task-row:hover { background: var(--surface-alt); }
    .code { font-weight: 700; min-width: 55px; }
    .title { flex: 1; }
    .sprint { font-size: 13px; color: var(--color-blue); font-weight: 600; }
    .date, .day, .hours { font-size: 13px; color: var(--text-muted); }
  `]
})
export class TaskListComponent implements OnInit {
  tasks: Task[] = [];
  sprints: Sprint[] = [];
  filterStatus: string | null = null;
  filterSprint: number | null = null;
  filterFrom: string | null = null;
  filterTo: string | null = null;
  totalElements = 0;
  pageSize = 20;
  page = 0;

  constructor(
    private taskService: TaskService,
    private sprintService: SprintService
  ) {}

  ngOnInit(): void {
    this.sprintService.findAll().subscribe(s => this.sprints = s);
    this.load();
  }

  load(): void {
    const params: Record<string, any> = { page: this.page, size: this.pageSize };
    if (this.filterStatus) params['status'] = this.filterStatus;
    if (this.filterSprint) params['sprint'] = this.filterSprint;
    if (this.filterFrom) params['from'] = this.filterFrom;
    if (this.filterTo) params['to'] = this.filterTo;
    this.taskService.findAll(params).subscribe(p => {
      this.tasks = p.content;
      this.totalElements = p.totalElements;
    });
  }

  resetAndLoad(): void {
    this.page = 0;
    this.load();
  }

  onPage(event: PageEvent): void {
    this.page = event.pageIndex;
    this.pageSize = event.pageSize;
    this.load();
  }
}
