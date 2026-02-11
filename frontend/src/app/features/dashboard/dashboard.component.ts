import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { DashboardService } from '../../core/services/dashboard.service';
import { Dashboard } from '../../core/models/dashboard.model';
import { StatusBadgeComponent } from '../../shared/components/status-badge.component';
import { ProgressBarComponent } from '../../shared/components/progress-bar.component';
import { DatePtPipe } from '../../shared/pipes/date-pt.pipe';
import { HoursPipe } from '../../shared/pipes/hours.pipe';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatChipsModule,
    StatusBadgeComponent, ProgressBarComponent, DatePtPipe, HoursPipe],
  template: `
    @if (dashboard) {
      <h2>Dashboard</h2>

      <!-- KPI Row -->
      <div class="kpi-row">
        <mat-card class="kpi-card">
          <div class="kpi-label">Sprint Actual</div>
          <div class="kpi-value">{{ dashboard.activeSprint?.name || 'N/A' }}</div>
          <div class="kpi-sub">Sprint {{ dashboard.activeSprint?.sprintNumber }}</div>
        </mat-card>
        <mat-card class="kpi-card">
          <div class="kpi-label">Progresso Global</div>
          <div class="kpi-value">{{ dashboard.completedSessions }}/{{ dashboard.totalSessions }}</div>
          <app-progress-bar [value]="dashboard.projectProgress" color="var(--angola-red)" />
        </mat-card>
        <mat-card class="kpi-card">
          <div class="kpi-label">Horas</div>
          <div class="kpi-value">{{ dashboard.totalHoursSpent | hours }} / {{ dashboard.totalHoursPlanned | hours }}</div>
          <app-progress-bar [value]="hoursPercent" color="var(--angola-gold)" />
        </mat-card>
        <mat-card class="kpi-card">
          <div class="kpi-label">Esta Semana</div>
          <div class="kpi-value">{{ dashboard.weekProgress.weekCompleted }}/{{ dashboard.weekProgress.weekTasks }}</div>
          <app-progress-bar [value]="weekPercent" color="var(--color-blue)" />
        </mat-card>
      </div>

      <!-- Today's Task -->
      @if (dashboard.todayTask) {
        <mat-card class="today-card">
          <div class="today-header">
            <app-status-badge [status]="dashboard.todayTask.status" />
            <span class="task-code">{{ dashboard.todayTask.taskCode }}</span>
            <span class="task-date">{{ dashboard.todayTask.sessionDate | datePt }}</span>
            <span class="task-day">{{ dashboard.todayTask.dayOfWeek }}</span>
            <span class="task-hours">{{ dashboard.todayTask.plannedHours | hours }}</span>
          </div>
          <h3>{{ dashboard.todayTask.title }}</h3>
          @if (dashboard.todayTask.deliverables) {
            <ul class="deliverables">
              @for (d of dashboard.todayTask.deliverables; track d) {
                <li>{{ d }}</li>
              }
            </ul>
          }
          @if (dashboard.todayTask.validationCriteria?.length) {
            <div class="validation-section">
              <h4>Critérios de Validação</h4>
              <ul class="validation-list">
                @for (v of dashboard.todayTask.validationCriteria; track v) {
                  <li><mat-icon class="check-icon">check_circle_outline</mat-icon> {{ v }}</li>
                }
              </ul>
            </div>
          }
          <div class="today-actions">
            <a mat-raised-button class="btn-primary" [routerLink]="['/tasks', dashboard.todayTask.id]">
              <mat-icon>visibility</mat-icon> Ver Tarefa
            </a>
            <a mat-stroked-button [routerLink]="['/prompts']">
              <mat-icon>content_copy</mat-icon> Copiar Prompt
            </a>
          </div>
        </mat-card>
      }

      <!-- Sprint Progress -->
      <h3>Progresso dos Sprints</h3>
      <div class="sprint-grid">
        @for (s of dashboard.sprintSummaries; track s.sprintNumber) {
          <mat-card class="sprint-mini" [style.border-left-color]="s.color">
            <div class="sprint-mini-header">S{{ s.sprintNumber }}</div>
            <div class="sprint-mini-name">{{ s.name }}</div>
            <app-progress-bar [value]="s.progress" [color]="s.color" />
          </mat-card>
        }
      </div>

      <!-- Recent Tasks -->
      @if (dashboard.recentTasks?.length) {
        <h3>Tarefas Recentes</h3>
        <div class="recent-list">
          @for (t of dashboard.recentTasks; track t.id) {
            <a class="recent-item" [routerLink]="['/tasks', t.id]">
              <app-status-badge [status]="t.status" />
              <span class="task-code">{{ t.taskCode }}</span>
              <span class="task-title">{{ t.title }}</span>
              <span class="task-date">{{ t.sessionDate | datePt }}</span>
              <span class="task-hours">{{ t.plannedHours | hours }}</span>
            </a>
          }
        </div>
      }

      <!-- Upcoming Blocked Days -->
      @if (dashboard.upcomingBlockedDays?.length) {
        <h3>Dias Bloqueados</h3>
        <div class="blocked-list">
          @for (b of dashboard.upcomingBlockedDays; track b.id) {
            <div class="blocked-item">
              <mat-icon>block</mat-icon>
              <span>{{ b.blockedDate | datePt }} — {{ b.reason }} ({{ b.hoursLost | hours }})</span>
            </div>
          }
        </div>
      }
    } @else {
      <p>A carregar dashboard...</p>
    }
  `,
  styles: [`
    h2 { margin: 0 0 20px; }
    .kpi-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
    .kpi-card { padding: 16px; }
    .kpi-label { font-size: 12px; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.5px; }
    .kpi-value { font-size: 22px; font-weight: 700; margin: 4px 0; }
    .kpi-sub { font-size: 13px; color: var(--text-secondary); }
    .today-card { padding: 20px; margin-bottom: 24px; border-left: 4px solid var(--color-blue); }
    .today-header { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
    .today-header .task-code { font-weight: 700; }
    .today-header .task-date, .today-header .task-day, .today-header .task-hours { color: var(--text-secondary); font-size: 14px; }
    .today-card h3 { margin: 8px 0 12px; }
    .deliverables { margin: 0 0 16px; padding-left: 20px; }
    .deliverables li { margin: 4px 0; color: var(--text-secondary); }
    .validation-section { margin-bottom: 16px; }
    .validation-section h4 { margin: 0 0 8px; font-size: 14px; color: var(--text-secondary); }
    .validation-list { list-style: none; padding: 0; margin: 0; }
    .validation-list li { display: flex; align-items: center; gap: 6px; margin: 4px 0; font-size: 13px; color: var(--color-green); }
    .check-icon { font-size: 16px; width: 16px; height: 16px; color: var(--color-green); }
    .today-actions { display: flex; gap: 12px; }
    .sprint-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-bottom: 24px; }
    .sprint-mini { padding: 12px; border-left: 4px solid; }
    .sprint-mini-header { font-weight: 700; font-size: 16px; }
    .sprint-mini-name { font-size: 13px; color: var(--text-secondary); margin-bottom: 8px; }
    .recent-list { display: flex; flex-direction: column; gap: 4px; margin-bottom: 24px; }
    .recent-item {
      display: flex; align-items: center; gap: 12px; padding: 8px 12px;
      text-decoration: none; color: inherit; border-radius: 6px;
      transition: background 0.2s;
    }
    .recent-item:hover { background: var(--surface-alt); }
    .recent-item .task-code { font-weight: 600; min-width: 50px; }
    .recent-item .task-title { flex: 1; }
    .recent-item .task-date, .recent-item .task-hours { color: var(--text-muted); font-size: 13px; }
    .blocked-list { display: flex; flex-direction: column; gap: 8px; }
    .blocked-item { display: flex; align-items: center; gap: 8px; color: var(--text-secondary); font-size: 14px; }
    .blocked-item mat-icon { color: var(--angola-red); font-size: 18px; width: 18px; height: 18px; }
  `]
})
export class DashboardComponent implements OnInit {
  dashboard: Dashboard | null = null;

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.dashboardService.getDeveloperDashboard().subscribe(d => this.dashboard = d);
  }

  get hoursPercent(): number {
    if (!this.dashboard) return 0;
    return this.dashboard.totalHoursPlanned > 0
      ? (this.dashboard.totalHoursSpent * 100) / this.dashboard.totalHoursPlanned : 0;
  }

  get weekPercent(): number {
    if (!this.dashboard?.weekProgress) return 0;
    return this.dashboard.weekProgress.weekTasks > 0
      ? (this.dashboard.weekProgress.weekCompleted * 100) / this.dashboard.weekProgress.weekTasks : 0;
  }
}
