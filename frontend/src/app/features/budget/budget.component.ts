import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { BudgetService } from '../../core/services/budget.service';
import { AuthService } from '../../core/services/auth.service';
import { BudgetOverview, BudgetUpdate } from '../../core/models/budget.model';
import { AnimatedCounterComponent } from '../../shared/components/animated-counter.component';
import { SkeletonLoaderComponent } from '../../shared/components/skeleton-loader.component';
import { EurPipe } from '../../shared/pipes/currency-eur.pipe';

@Component({
  selector: 'app-budget',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatIconModule, MatButtonModule,
    MatFormFieldModule, MatInputModule, AnimatedCounterComponent, SkeletonLoaderComponent, EurPipe],
  template: `
    @if (data) {
      <div class="budget-page">
        <h1>Custos & Retorno</h1>

        <!-- KPI Row -->
        <div class="kpi-row">
          <mat-card class="kpi">
            <div class="kpi-label">Orçamento Total</div>
            <div class="kpi-value">{{ data.totalBudget | eur }}</div>
          </mat-card>
          <mat-card class="kpi">
            <div class="kpi-label">Gasto</div>
            <div class="kpi-value spent">{{ data.totalSpent | eur }}</div>
            <div class="kpi-sub">{{ data.budgetUsedPercent }}%</div>
          </mat-card>
          <mat-card class="kpi">
            <div class="kpi-label">Restante</div>
            <div class="kpi-value" [class.warning]="data.remaining < 0">{{ data.remaining | eur }}</div>
          </mat-card>
          <mat-card class="kpi">
            <div class="kpi-label">Burn Rate / Semana</div>
            <div class="kpi-value">{{ data.burnRatePerWeek | eur }}</div>
          </mat-card>
          <mat-card class="kpi">
            <div class="kpi-label">Projecção Total</div>
            <div class="kpi-value" [class.warning]="data.projectedVariance < 0"
                 [class.success]="data.projectedVariance >= 0">
              {{ data.projectedTotal | eur }}
            </div>
            <div class="kpi-sub" [class.warning]="data.projectedVariance < 0">
              {{ data.projectedVariance >= 0 ? 'Sob orçamento' : 'Acima do orçamento' }}:
              {{ data.projectedVariance | eur }}
            </div>
          </mat-card>
        </div>

        <!-- Sprint Cost Table -->
        <h2>Custos por Sprint</h2>
        <div class="table-wrap">
          <table class="cost-table">
            <thead>
              <tr>
                <th>Sprint</th>
                <th class="r">Horas Plan.</th>
                <th class="r">Horas Real</th>
                <th class="r">Custo Plan.</th>
                <th class="r">Custo Real</th>
                <th class="r">Variação</th>
              </tr>
            </thead>
            <tbody>
              @for (s of data.sprints; track s.sprintNumber) {
                <tr>
                  <td>
                    <span class="sprint-dot" [style.background]="s.color"></span>
                    Sprint {{ s.sprintNumber }}
                  </td>
                  <td class="r">{{ s.plannedHours }}h</td>
                  <td class="r">{{ s.actualHours }}h</td>
                  <td class="r">{{ s.plannedCost | eur }}</td>
                  <td class="r">{{ s.actualCost | eur }}</td>
                  <td class="r" [class.success]="s.variance >= 0" [class.warning]="s.variance < 0">
                    {{ s.variance | eur }}
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>

        <!-- Bar Chart -->
        <h2>Custo Planeado vs Real</h2>
        <div class="chart">
          @for (s of data.sprints; track s.sprintNumber) {
            <div class="chart-row">
              <div class="chart-label">S{{ s.sprintNumber }}</div>
              <div class="chart-bars">
                <div class="bar planned" [style.width.%]="getBarWidth(s.plannedCost)" title="Planeado: {{ s.plannedCost | eur }}"></div>
                <div class="bar actual" [style.width.%]="getBarWidth(s.actualCost)" [style.background]="s.color" title="Real: {{ s.actualCost | eur }}"></div>
              </div>
            </div>
          }
        </div>
        <div class="chart-legend">
          <span class="legend-item"><span class="legend-dot planned-dot"></span> Planeado</span>
          <span class="legend-item"><span class="legend-dot actual-dot"></span> Real</span>
        </div>

        <!-- ROI Section -->
        <h2>Indicadores ROI</h2>
        <div class="roi-row">
          <mat-card class="roi-card">
            <mat-icon>payments</mat-icon>
            <div class="roi-val">{{ data.costPerSession | eur }}</div>
            <div class="roi-lbl">Custo por Sessão</div>
          </mat-card>
          <mat-card class="roi-card">
            <mat-icon>schedule</mat-icon>
            <div class="roi-val">{{ data.costPerHour | eur }}</div>
            <div class="roi-lbl">Custo por Hora</div>
          </mat-card>
          <mat-card class="roi-card">
            <mat-icon>trending_up</mat-icon>
            <div class="roi-val" [class.success]="data.projectedVariance >= 0" [class.warning]="data.projectedVariance < 0">
              {{ data.projectedVariance | eur }}
            </div>
            <div class="roi-lbl">Projecção {{ data.projectedVariance >= 0 ? 'Sub' : 'Sobre' }} Orçamento</div>
          </mat-card>
          <mat-card class="roi-card">
            <mat-icon>calendar_today</mat-icon>
            <div class="roi-val">
              <app-animated-counter [targetValue]="data.weeksElapsed" /> / {{ data.weeksElapsed + data.weeksRemaining }}
            </div>
            <div class="roi-lbl">Semanas (Decorridas / Total)</div>
          </mat-card>
        </div>

        <!-- Edit Form (DEVELOPER only) -->
        @if (isDeveloper) {
          <h2>Configuração do Orçamento</h2>
          <mat-card class="edit-card">
            <div class="edit-grid">
              <mat-form-field>
                <mat-label>Taxa Horária (€)</mat-label>
                <input matInput type="number" [(ngModel)]="editForm.hourlyRate" min="1" step="0.50">
              </mat-form-field>
              <mat-form-field>
                <mat-label>Orçamento Total (€)</mat-label>
                <input matInput type="number" [(ngModel)]="editForm.totalBudget" min="1" step="100">
              </mat-form-field>
              <mat-form-field>
                <mat-label>Moeda</mat-label>
                <input matInput [(ngModel)]="editForm.currency" maxlength="3">
              </mat-form-field>
              <mat-form-field>
                <mat-label>Contingência (%)</mat-label>
                <input matInput type="number" [(ngModel)]="editForm.contingencyPercent" min="0" max="100" step="1">
              </mat-form-field>
            </div>
            <button mat-raised-button color="primary" (click)="saveBudget()" [disabled]="saving">
              {{ saving ? 'A guardar...' : 'Guardar Alterações' }}
            </button>
          </mat-card>
        }
      </div>
    } @else {
      <div class="budget-page">
        <h1>Custos & Retorno</h1>
        <div class="kpi-row">
          <app-skeleton-loader variant="kpi" [count]="5" />
        </div>
        <app-skeleton-loader variant="card" [count]="2" />
      </div>
    }
  `,
  styles: [`
    .budget-page { max-width: 1100px; margin: 0 auto; }
    h1 { font-family: 'Playfair Display', serif; color: var(--angola-red); margin-bottom: 24px; }
    h2 { margin: 28px 0 12px; color: var(--text-primary); }

    .kpi-row { display: grid; grid-template-columns: repeat(5, 1fr); gap: 16px; margin-bottom: 24px; }
    .kpi { padding: 20px; text-align: center; }
    .kpi-label { font-size: 12px; color: var(--text-muted); text-transform: uppercase; }
    .kpi-value { font-size: 22px; font-weight: 700; margin: 4px 0; }
    .kpi-sub { font-size: 13px; color: var(--text-secondary); }

    .success { color: #4caf50 !important; }
    .warning { color: #f44336 !important; }
    .spent { color: var(--angola-red); }

    .table-wrap { overflow-x: auto; }
    .cost-table { width: 100%; border-collapse: collapse; font-size: 14px; }
    .cost-table th { text-align: left; padding: 10px 12px; border-bottom: 2px solid var(--border-light); color: var(--text-muted); font-size: 12px; text-transform: uppercase; }
    .cost-table td { padding: 10px 12px; border-bottom: 1px solid var(--border-light); }
    .cost-table .r { text-align: right; }
    .sprint-dot { display: inline-block; width: 10px; height: 10px; border-radius: 50%; margin-right: 8px; vertical-align: middle; }

    .chart { display: flex; flex-direction: column; gap: 10px; margin-bottom: 8px; }
    .chart-row { display: flex; align-items: center; gap: 12px; }
    .chart-label { width: 30px; font-weight: 700; font-size: 13px; color: var(--text-secondary); text-align: right; }
    .chart-bars { flex: 1; display: flex; flex-direction: column; gap: 3px; }
    .bar { height: 16px; border-radius: 4px; min-width: 2px; transition: width 0.5s ease; }
    .bar.planned { background: var(--border-light); }
    .chart-legend { display: flex; gap: 24px; justify-content: center; margin-bottom: 16px; font-size: 13px; color: var(--text-secondary); }
    .legend-item { display: flex; align-items: center; gap: 6px; }
    .legend-dot { width: 12px; height: 12px; border-radius: 3px; }
    .planned-dot { background: var(--border-light); }
    .actual-dot { background: var(--angola-red); }

    .roi-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
    .roi-card { padding: 20px; text-align: center; }
    .roi-card mat-icon { color: var(--angola-gold); font-size: 28px; width: 28px; height: 28px; margin-bottom: 8px; }
    .roi-val { font-size: 22px; font-weight: 700; }
    .roi-lbl { font-size: 12px; color: var(--text-muted); text-transform: uppercase; margin-top: 4px; }

    .edit-card { padding: 24px; }
    .edit-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 16px; }

    @media (max-width: 1024px) {
      .kpi-row { grid-template-columns: repeat(3, 1fr); }
      .roi-row { grid-template-columns: repeat(2, 1fr); }
      .edit-grid { grid-template-columns: repeat(2, 1fr); }
    }
    @media (max-width: 600px) {
      .kpi-row { grid-template-columns: 1fr; }
      .roi-row { grid-template-columns: 1fr; }
      .edit-grid { grid-template-columns: 1fr; }
    }
  `]
})
export class BudgetComponent implements OnInit {
  data: BudgetOverview | null = null;
  isDeveloper = false;
  saving = false;
  editForm: BudgetUpdate = { hourlyRate: 85, totalBudget: 57800, currency: 'EUR', contingencyPercent: 10 };
  private maxCost = 0;

  constructor(private budgetService: BudgetService, private auth: AuthService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.isDeveloper = this.auth.getRole() === 'DEVELOPER';
    this.budgetService.getBudgetOverview().subscribe({
      next: d => {
        this.data = d;
        this.maxCost = Math.max(...d.sprints.map(s => Math.max(s.plannedCost, s.actualCost)), 1);
        this.editForm = {
          hourlyRate: d.hourlyRate,
          totalBudget: d.totalBudget,
          currency: d.currency,
          contingencyPercent: d.contingencyPercent
        };
        this.cdr.markForCheck();
      }
    });
  }

  getBarWidth(cost: number): number {
    return this.maxCost > 0 ? (cost / this.maxCost) * 100 : 0;
  }

  saveBudget(): void {
    this.saving = true;
    this.budgetService.updateBudget(this.editForm).subscribe({
      next: d => {
        this.data = d;
        this.maxCost = Math.max(...d.sprints.map(s => Math.max(s.plannedCost, s.actualCost)), 1);
        this.saving = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.saving = false;
        this.cdr.markForCheck();
      }
    });
  }
}
