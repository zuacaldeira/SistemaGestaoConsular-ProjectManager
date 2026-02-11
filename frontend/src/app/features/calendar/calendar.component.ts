import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DashboardService } from '../../core/services/dashboard.service';
import { CalendarData, CalendarDay } from '../../core/models/dashboard.model';
import { StatusBadgeComponent } from '../../shared/components/status-badge.component';

@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatTooltipModule, StatusBadgeComponent],
  template: `
    <div class="cal-header">
      <button mat-icon-button (click)="prevMonth()"><mat-icon>chevron_left</mat-icon></button>
      <h2>{{ monthNames[month - 1] }} {{ year }}</h2>
      <button mat-icon-button (click)="nextMonth()"><mat-icon>chevron_right</mat-icon></button>
    </div>

    @if (calendar) {
      <div class="cal-grid">
        <div class="cal-day-header">Seg</div>
        <div class="cal-day-header">Ter</div>
        <div class="cal-day-header">Qua</div>
        <div class="cal-day-header">Qui</div>
        <div class="cal-day-header">Sex</div>
        <div class="cal-day-header">Sáb</div>
        <div class="cal-day-header">Dom</div>

        @for (empty of leadingBlanks; track $index) {
          <div class="cal-cell empty"></div>
        }

        @for (day of calendar.days; track day.date) {
          <div class="cal-cell"
               [class.blocked]="day.isBlocked"
               [class.has-task]="!!day.task"
               [class.weekend]="day.dayOfWeek === 'SAT'">
            <span class="day-num">{{ getDayNum(day.date) }}</span>
            @if (day.isBlocked) {
              <span class="block-reason" [title]="day.blockReason">{{ day.blockReason }}</span>
            }
            @if (day.task) {
              <a class="task-link" [routerLink]="['/tasks', day.task.id]"
                 [matTooltip]="day.task.title + ' (' + day.task.plannedHours + 'h)'">
                <app-status-badge [status]="day.task.status" />
                <span class="task-code">{{ day.task.taskCode }}</span>
              </a>
            }
          </div>
        }
      </div>
    }
  `,
  styles: [`
    .cal-header { display: flex; align-items: center; gap: 16px; margin-bottom: 16px; }
    .cal-header h2 { margin: 0; min-width: 200px; text-align: center; }
    .cal-grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 4px; }
    .cal-day-header { text-align: center; font-weight: 700; font-size: 12px; color: var(--text-muted); padding: 8px; }
    .cal-cell {
      min-height: 80px; border: 1px solid var(--border-light); border-radius: 6px;
      padding: 6px; background: var(--surface); font-size: 12px;
    }
    .cal-cell.empty { background: transparent; border: none; }
    .cal-cell.blocked { background: #FEE2E2; border-color: #FECACA; }
    .cal-cell.has-task { border-color: var(--color-blue); }
    .cal-cell.weekend { background: var(--surface-alt); }
    .day-num { font-weight: 700; font-size: 14px; }
    .block-reason { display: block; color: var(--angola-red); font-size: 10px; margin-top: 2px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .task-link { display: flex; align-items: center; gap: 4px; margin-top: 4px; text-decoration: none; }
    .task-code { font-weight: 600; font-size: 11px; }
  `]
})
export class CalendarComponent implements OnInit {
  calendar: CalendarData | null = null;
  year = new Date().getFullYear();
  month = new Date().getMonth() + 1;
  leadingBlanks: number[] = [];

  monthNames = ['Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
    'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'];

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.dashboardService.getCalendar(this.year, this.month).subscribe(c => {
      this.calendar = c;
      const firstDay = new Date(this.year, this.month - 1, 1).getDay();
      // Convert Sunday=0 to Monday-based: Mon=0, Tue=1, ..., Sun=6
      const mondayBased = firstDay === 0 ? 6 : firstDay - 1;
      this.leadingBlanks = Array(mondayBased);
    });
  }

  getDayNum(date: string): number { return new Date(date).getDate(); }
  prevMonth(): void { if (this.month === 1) { this.month = 12; this.year--; } else { this.month--; } this.load(); }
  nextMonth(): void { if (this.month === 12) { this.month = 1; this.year++; } else { this.month++; } this.load(); }
}
