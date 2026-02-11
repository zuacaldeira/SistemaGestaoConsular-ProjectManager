import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiService } from '../../core/services/api.service';
import { SprintService } from '../../core/services/sprint.service';
import { Sprint } from '../../core/models/sprint.model';
import { DatePtPipe } from '../../shared/pipes/date-pt.pipe';

interface Report {
  id: number;
  sprintId: number;
  sprintNumber: number;
  sprintName: string;
  reportType: string;
  generatedAt: string;
  summaryPt: string;
  summaryEn: string;
}

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, MatSnackBarModule, DatePtPipe],
  template: `
    <h2>Relatórios</h2>

    <div class="generate-section">
      <h3>Gerar Relatório de Sprint</h3>
      <div class="sprint-buttons">
        @for (sprint of sprints; track sprint.id) {
          <button mat-stroked-button (click)="generate(sprint.id)" [disabled]="generating">
            Sprint {{ sprint.sprintNumber }}: {{ sprint.name }}
          </button>
        }
      </div>
    </div>

    @if (reports.length) {
      <h3>Relatórios Gerados</h3>
      @for (report of reports; track report.id) {
        <mat-card class="report-card">
          <div class="report-header">
            <span class="sprint-name">Sprint {{ report.sprintNumber }}: {{ report.sprintName }}</span>
            <span class="report-type">{{ report.reportType }}</span>
            <span class="report-date">{{ report.generatedAt | datePt }}</span>
            <button mat-icon-button (click)="downloadPdf(report.id)" title="Descarregar PDF">
              <mat-icon>picture_as_pdf</mat-icon>
            </button>
          </div>
          <p class="summary">{{ report.summaryPt }}</p>
          <p class="summary-en">{{ report.summaryEn }}</p>
        </mat-card>
      }
    } @else {
      <mat-card><p>Nenhum relatório gerado ainda.</p></mat-card>
    }
  `,
  styles: [`
    .generate-section { margin-bottom: 24px; }
    .sprint-buttons { display: flex; flex-wrap: wrap; gap: 8px; }
    .report-card { padding: 16px; margin-bottom: 12px; }
    .report-header { display: flex; gap: 16px; align-items: center; margin-bottom: 8px; }
    .sprint-name { font-weight: 700; }
    .report-type { font-size: 12px; background: var(--surface-alt); padding: 2px 8px; border-radius: 4px; }
    .report-date { font-size: 13px; color: var(--text-muted); }
    .summary { margin: 8px 0 4px; }
    .summary-en { color: var(--text-secondary); font-size: 14px; margin: 0; }
  `]
})
export class ReportsComponent implements OnInit {
  sprints: Sprint[] = [];
  reports: Report[] = [];
  generating = false;

  constructor(
    private api: ApiService,
    private sprintService: SprintService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.sprintService.findAll().subscribe(s => this.sprints = s);
    this.loadReports();
  }

  loadReports(): void {
    this.api.get<Report[]>('/reports').subscribe(r => this.reports = r);
  }

  generate(sprintId: number): void {
    this.generating = true;
    this.api.post<Report>(`/reports/sprint/${sprintId}/generate`).subscribe({
      next: () => {
        this.snackBar.open('Relatório gerado!', 'OK', { duration: 3000 });
        this.loadReports();
        this.generating = false;
      },
      error: () => { this.generating = false; }
    });
  }

  downloadPdf(reportId: number): void {
    this.api.getBlob(`/reports/${reportId}/pdf`).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `relatorio-${reportId}.pdf`;
      a.click();
      window.URL.revokeObjectURL(url);
    });
  }
}
