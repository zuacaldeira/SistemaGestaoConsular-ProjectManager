import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { DashboardService } from '../../core/services/dashboard.service';
import { ThemeService } from '../../core/services/theme.service';
import { StakeholderDashboard } from '../../core/models/dashboard.model';
import { ProgressBarComponent } from '../../shared/components/progress-bar.component';
import { AnimatedCounterComponent } from '../../shared/components/animated-counter.component';
import { SkeletonLoaderComponent } from '../../shared/components/skeleton-loader.component';
import { DatePtPipe } from '../../shared/pipes/date-pt.pipe';
import { HoursPipe } from '../../shared/pipes/hours.pipe';
import { EurPipe } from '../../shared/pipes/currency-eur.pipe';

@Component({
  selector: 'app-stakeholder',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatButtonModule, ProgressBarComponent,
    AnimatedCounterComponent, SkeletonLoaderComponent, DatePtPipe, HoursPipe, EurPipe],
  template: `
    @if (data) {
      <div class="stakeholder-page">
        <!-- Header -->
        <div class="sh-header">
          <button mat-icon-button class="theme-toggle" (click)="theme.toggle()" aria-label="Toggle dark mode">
            <mat-icon>{{ theme.isDarkMode() ? 'light_mode' : 'dark_mode' }}</mat-icon>
          </button>
          <h1>SGCD — Relatório de Progresso do Projecto</h1>
          <p class="client">{{ data.client }}</p>
          <p class="updated">Actualizado: {{ data.lastUpdated | datePt:'long' }}</p>
        </div>

        <!-- KPI Row -->
        <div class="kpi-row">
          <mat-card class="kpi">
            <div class="kpi-label">Progresso Global</div>
            <div class="kpi-value">
              <app-animated-counter [targetValue]="data.overallProgress" [decimals]="1" suffix="%" />
            </div>
            <app-progress-bar [value]="data.overallProgress" color="var(--angola-red)" />
          </mat-card>
          <mat-card class="kpi">
            <div class="kpi-label">Sessões</div>
            <div class="kpi-value">
              <app-animated-counter [targetValue]="data.completedSessions" /> / {{ data.totalSessions }}
            </div>
          </mat-card>
          <mat-card class="kpi">
            <div class="kpi-label">Horas</div>
            <div class="kpi-value">
              <app-animated-counter [targetValue]="data.totalHoursSpent" [decimals]="1" suffix="h" /> / {{ data.totalHoursPlanned | hours }}
            </div>
          </mat-card>
          <mat-card class="kpi">
            <div class="kpi-label">Prazo</div>
            <div class="kpi-value">
              <app-animated-counter [targetValue]="data.daysRemaining" /> dias
            </div>
            <div class="kpi-sub">{{ data.targetDate | datePt:'medium' }}</div>
          </mat-card>
        </div>

        <!-- Sprint Timeline -->
        <div class="timeline">
          @for (sprint of data.sprints; track sprint.number) {
            <div class="timeline-item">
              <div class="timeline-dot" [style.background]="sprint.color"
                   [class.completed]="sprint.status === 'COMPLETED'"
                   [class.active]="sprint.status === 'ACTIVE'"></div>
              <div class="timeline-line"></div>
            </div>
          }
        </div>

        <!-- Sprint Cards -->
        <div class="sprint-grid">
          @for (sprint of data.sprints; track sprint.number) {
            <mat-card class="sprint-card card-hover" [style.border-top-color]="sprint.color">
              <div class="sprint-num">Sprint {{ sprint.number }}</div>
              <h3>{{ sprint.name }}</h3>
              <p class="sprint-en">{{ sprint.nameEn }}</p>
              <app-progress-bar [value]="sprint.progress" [color]="sprint.color" />
              <div class="sprint-meta">
                <span>{{ sprint.completedSessions }}/{{ sprint.sessions }} sessões</span>
                <span>{{ sprint.hoursSpent | hours }} / {{ sprint.hours | hours }}</span>
              </div>
              <div class="sprint-dates">
                {{ sprint.startDate | datePt }} — {{ sprint.endDate | datePt }}
              </div>
              <div class="sprint-focus">{{ sprint.focus }}</div>
            </mat-card>
          }
        </div>

        <!-- Weekly Activity -->
        @if (data.weeklyActivity) {
          <h2>Actividade Semanal</h2>
          <div class="weekly-row">
            <mat-card class="weekly-card card-hover">
              <mat-icon>event</mat-icon>
              <div class="weekly-val">
                <app-animated-counter [targetValue]="data.weeklyActivity.sessionsThisWeek" />
              </div>
              <div class="weekly-lbl">Sessões esta semana</div>
            </mat-card>
            <mat-card class="weekly-card card-hover">
              <mat-icon>schedule</mat-icon>
              <div class="weekly-val">
                <app-animated-counter [targetValue]="data.weeklyActivity.hoursThisWeek" [decimals]="1" suffix="h" />
              </div>
              <div class="weekly-lbl">Horas esta semana</div>
            </mat-card>
            <mat-card class="weekly-card card-hover">
              <mat-icon>check_circle</mat-icon>
              <div class="weekly-val">
                <app-animated-counter [targetValue]="data.weeklyActivity.tasksCompletedThisWeek" />
              </div>
              <div class="weekly-lbl">Tarefas concluídas</div>
            </mat-card>
          </div>
        }

        <!-- Budget Summary -->
        @if (data.budget) {
          <h2>Orçamento</h2>
          <div class="budget-row">
            <mat-card class="budget-card card-hover">
              <mat-icon>account_balance</mat-icon>
              <div class="budget-val">{{ data.budget.totalBudget | eur }}</div>
              <div class="budget-lbl">Orçamento Total</div>
            </mat-card>
            <mat-card class="budget-card card-hover">
              <mat-icon>payments</mat-icon>
              <div class="budget-val">{{ data.budget.totalSpent | eur }}</div>
              <div class="budget-lbl">Gasto</div>
            </mat-card>
            <mat-card class="budget-card card-hover">
              <mat-icon>savings</mat-icon>
              <div class="budget-val" [class.budget-warning]="data.budget.remaining < 0">{{ data.budget.remaining | eur }}</div>
              <div class="budget-lbl">Restante</div>
            </mat-card>
            <mat-card class="budget-card card-hover">
              <mat-icon>donut_large</mat-icon>
              <div class="budget-val">
                <app-animated-counter [targetValue]="data.budget.budgetUsedPercent" [decimals]="1" suffix="%" />
              </div>
              <div class="budget-lbl">Utilizado</div>
              <app-progress-bar [value]="data.budget.budgetUsedPercent" color="var(--angola-red)" />
            </mat-card>
          </div>
        }

        <!-- Milestones -->
        <h2>Marcos do Projecto</h2>
        <div class="milestones">
          @for (m of data.milestones; track m.name) {
            <div class="milestone card-hover" [class]="'ms-' + m.status.toLowerCase()">
              <mat-icon>{{ m.status === 'COMPLETED' ? 'check_circle' : m.status === 'IN_PROGRESS' ? 'pending' : 'schedule' }}</mat-icon>
              <span class="ms-name">{{ m.name }}</span>
              <span class="ms-date">{{ m.targetDate | datePt }}</span>
            </div>
          }
        </div>
      </div>
    } @else {
      <div class="skeleton-stakeholder">
        <div class="sh-header">
          <app-skeleton-loader variant="text" width="400px" height="32px" />
          <app-skeleton-loader variant="text" width="200px" height="16px" />
        </div>
        <div class="kpi-row">
          <app-skeleton-loader variant="kpi" [count]="4" />
        </div>
        <div class="sprint-grid">
          <app-skeleton-loader variant="card" [count]="6" />
        </div>
      </div>
    }
  `,
  styles: [`
    .stakeholder-page { max-width: 1100px; margin: 0 auto; padding: 40px 24px; position: relative; }
    .sh-header { text-align: center; margin-bottom: 32px; }
    .sh-header h1 { font-family: 'Playfair Display', serif; color: var(--angola-red); margin: 0; }
    .client { font-size: 16px; color: var(--text-secondary); margin: 4px 0; }
    .updated { font-size: 13px; color: var(--text-muted); }
    .kpi-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 32px; }
    .kpi { padding: 20px; text-align: center; }
    .kpi-label { font-size: 12px; color: var(--text-muted); text-transform: uppercase; }
    .kpi-value { font-size: 28px; font-weight: 700; margin: 4px 0; }
    .kpi-sub { font-size: 13px; color: var(--text-secondary); }
    .timeline { display: flex; justify-content: center; margin-bottom: 24px; }
    .timeline-item { display: flex; align-items: center; }
    .timeline-dot { width: 16px; height: 16px; border-radius: 50%; border: 3px solid white; box-shadow: 0 0 0 2px var(--border); }
    .timeline-dot.active { box-shadow: 0 0 0 3px var(--color-blue); }
    .timeline-dot.completed { box-shadow: 0 0 0 3px var(--color-green); }
    .timeline-line { width: 60px; height: 3px; background: var(--border-light); }
    .timeline-item:last-child .timeline-line { display: none; }
    .sprint-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 32px; }
    .sprint-card { padding: 16px; border-top: 4px solid; }
    .sprint-num { font-weight: 700; font-size: 12px; color: var(--text-muted); }
    .sprint-card h3 { margin: 4px 0 2px; font-size: 16px; }
    .sprint-en { font-size: 13px; color: var(--text-muted); margin: 0 0 12px; }
    .sprint-meta { display: flex; justify-content: space-between; font-size: 12px; color: var(--text-secondary); margin-top: 8px; }
    .sprint-dates { font-size: 12px; color: var(--text-muted); margin-top: 4px; }
    .sprint-focus { font-size: 12px; color: var(--text-secondary); margin-top: 4px; font-weight: 600; }
    .weekly-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 32px; }
    .weekly-card { padding: 20px; text-align: center; }
    .weekly-card mat-icon { color: var(--angola-gold); font-size: 28px; width: 28px; height: 28px; margin-bottom: 8px; }
    .weekly-val { font-size: 28px; font-weight: 700; }
    .weekly-lbl { font-size: 12px; color: var(--text-muted); text-transform: uppercase; margin-top: 4px; }
    h2 { text-align: center; margin-bottom: 16px; }
    .milestones { display: flex; flex-direction: column; gap: 8px; }
    .milestone { display: flex; align-items: center; gap: 12px; padding: 12px 16px; background: var(--surface); border-radius: 8px; }
    .ms-name { flex: 1; font-weight: 600; }
    .ms-date { color: var(--text-muted); font-size: 13px; }
    .ms-completed mat-icon { color: var(--color-green); }
    .ms-in_progress mat-icon { color: var(--color-blue); }
    .ms-future mat-icon { color: var(--text-muted); }
    .budget-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 32px; }
    .budget-card { padding: 20px; text-align: center; }
    .budget-card mat-icon { color: var(--angola-gold); font-size: 28px; width: 28px; height: 28px; margin-bottom: 8px; }
    .budget-val { font-size: 22px; font-weight: 700; }
    .budget-lbl { font-size: 12px; color: var(--text-muted); text-transform: uppercase; margin-top: 4px; }
    .budget-warning { color: #f44336; }
    .theme-toggle { position: absolute; top: 16px; right: 16px; color: var(--text-secondary); }
    .skeleton-stakeholder { max-width: 1100px; margin: 0 auto; padding: 40px 24px; }
    .skeleton-stakeholder .sh-header { text-align: center; margin-bottom: 32px; display: flex; flex-direction: column; align-items: center; gap: 8px; }
    .skeleton-stakeholder .kpi-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 32px; }
    .skeleton-stakeholder .sprint-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }

    @media (max-width: 1024px) {
      .kpi-row { grid-template-columns: repeat(2, 1fr); }
      .sprint-grid { grid-template-columns: repeat(2, 1fr); }
      .budget-row { grid-template-columns: repeat(2, 1fr); }
    }
    @media (max-width: 600px) {
      .stakeholder-page { padding: 20px 12px; }
      .sh-header h1 { font-size: 22px; }
      .kpi-row { grid-template-columns: 1fr; }
      .sprint-grid { grid-template-columns: 1fr; }
      .weekly-row { grid-template-columns: 1fr; }
      .budget-row { grid-template-columns: 1fr; }
      .timeline { display: none; }
    }
  `]
})
export class StakeholderComponent implements OnInit {
  data: StakeholderDashboard | null = null;

  constructor(private dashboardService: DashboardService, private cdr: ChangeDetectorRef, public theme: ThemeService) {}

  ngOnInit(): void {
    this.dashboardService.getStakeholderDashboard().subscribe(d => {
      this.data = d;
      this.cdr.markForCheck();
    });
  }
}
