import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MonitorService } from '../../core/services/monitor.service';
import { MonitorHealth, ServiceHealth } from '../../core/models/monitor.model';
import { SkeletonLoaderComponent } from '../../shared/components/skeleton-loader.component';
import { DatePtPipe } from '../../shared/pipes/date-pt.pipe';

@Component({
  selector: 'app-monitor',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, SkeletonLoaderComponent, DatePtPipe],
  template: `
    @if (health) {
      <div class="monitor-page">
        <h1>Monitor do Sistema</h1>

        <!-- Overall Status Banner -->
        <div class="status-banner" [class]="'status-' + health.overallStatus.toLowerCase()">
          <mat-icon>{{ health.overallStatus === 'UP' ? 'check_circle' : health.overallStatus === 'DEGRADED' ? 'warning' : 'error' }}</mat-icon>
          <span class="status-text">
            {{ health.overallStatus === 'UP' ? 'Todos os serviços operacionais' :
               health.overallStatus === 'DEGRADED' ? 'Alguns serviços indisponíveis' :
               'Sistema indisponível' }}
          </span>
          <span class="status-time">Verificado: {{ health.checkedAt | datePt:'long' }}</span>
        </div>

        <!-- PM Services -->
        <h2>Plataforma PM</h2>
        <div class="service-grid">
          @for (svc of pmServices; track svc.name) {
            <mat-card class="service-card" [class]="'svc-' + svc.status.toLowerCase()">
              <div class="svc-header">
                <span class="svc-dot" [class]="'dot-' + svc.status.toLowerCase()"></span>
                <span class="svc-name">{{ svc.name }}</span>
              </div>
              <div class="svc-details">
                <div class="svc-row">
                  <span class="svc-label">Estado</span>
                  <span class="svc-val" [class]="'val-' + svc.status.toLowerCase()">{{ svc.status }}</span>
                </div>
                <div class="svc-row">
                  <span class="svc-label">Tempo</span>
                  <span class="svc-val">{{ svc.responseTimeMs }}ms</span>
                </div>
                <div class="svc-row">
                  <span class="svc-label">Tipo</span>
                  <span class="svc-val">{{ svc.type }}</span>
                </div>
                @if (svc.errorMessage) {
                  <div class="svc-error">{{ svc.errorMessage }}</div>
                }
              </div>
            </mat-card>
          }
        </div>

        <!-- MVP Services -->
        <h2>Plataforma MVP</h2>
        <div class="service-grid">
          @for (svc of mvpServices; track svc.name) {
            <mat-card class="service-card" [class]="'svc-' + svc.status.toLowerCase()">
              <div class="svc-header">
                <span class="svc-dot" [class]="'dot-' + svc.status.toLowerCase()"></span>
                <span class="svc-name">{{ svc.name }}</span>
              </div>
              <div class="svc-details">
                <div class="svc-row">
                  <span class="svc-label">Estado</span>
                  <span class="svc-val" [class]="'val-' + svc.status.toLowerCase()">{{ svc.status }}</span>
                </div>
                <div class="svc-row">
                  <span class="svc-label">Tempo</span>
                  <span class="svc-val">{{ svc.responseTimeMs }}ms</span>
                </div>
                <div class="svc-row">
                  <span class="svc-label">Tipo</span>
                  <span class="svc-val">{{ svc.type }}</span>
                </div>
                @if (svc.errorMessage) {
                  <div class="svc-error">{{ svc.errorMessage }}</div>
                }
              </div>
            </mat-card>
          }
        </div>
      </div>
    } @else {
      <div class="monitor-page">
        <h1>Monitor do Sistema</h1>
        <app-skeleton-loader variant="card" [count]="3" />
        <app-skeleton-loader variant="card" [count]="3" />
      </div>
    }
  `,
  styles: [`
    .monitor-page { max-width: 1100px; margin: 0 auto; }
    h1 { font-family: 'Playfair Display', serif; color: var(--angola-red); margin-bottom: 24px; }
    h2 { margin: 24px 0 12px; color: var(--text-primary); }

    .status-banner {
      display: flex; align-items: center; gap: 12px;
      padding: 16px 24px; border-radius: 12px; margin-bottom: 24px;
      font-weight: 600; flex-wrap: wrap;
    }
    .status-banner mat-icon { font-size: 28px; width: 28px; height: 28px; }
    .status-text { flex: 1; font-size: 16px; }
    .status-time { font-size: 13px; font-weight: 400; opacity: 0.8; }
    .status-up { background: #e8f5e9; color: #2e7d32; }
    .status-degraded { background: #fff3e0; color: #e65100; }
    .status-down { background: #ffebee; color: #c62828; }

    :host-context(.dark-mode) .status-up { background: #1b5e20; color: #a5d6a7; }
    :host-context(.dark-mode) .status-degraded { background: #e65100; color: #ffcc80; }
    :host-context(.dark-mode) .status-down { background: #b71c1c; color: #ef9a9a; }

    .service-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
    .service-card { padding: 16px; }
    .svc-header { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
    .svc-dot { width: 12px; height: 12px; border-radius: 50%; flex-shrink: 0; }
    .dot-up { background: #4caf50; }
    .dot-down { background: #f44336; }
    .dot-unknown { background: #9e9e9e; }
    .svc-name { font-weight: 700; font-size: 15px; }
    .svc-details { display: flex; flex-direction: column; gap: 6px; }
    .svc-row { display: flex; justify-content: space-between; font-size: 13px; }
    .svc-label { color: var(--text-muted); }
    .svc-val { font-weight: 600; }
    .val-up { color: #4caf50; }
    .val-down { color: #f44336; }
    .val-unknown { color: #9e9e9e; }
    .svc-error { font-size: 12px; color: #f44336; margin-top: 4px; word-break: break-all; }

    @media (max-width: 1024px) { .service-grid { grid-template-columns: repeat(2, 1fr); } }
    @media (max-width: 600px) { .service-grid { grid-template-columns: 1fr; } }
  `]
})
export class MonitorComponent implements OnInit, OnDestroy {
  health: MonitorHealth | null = null;
  pmServices: ServiceHealth[] = [];
  mvpServices: ServiceHealth[] = [];
  private intervalId: any;

  constructor(private monitorService: MonitorService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadHealth();
    this.intervalId = setInterval(() => this.loadHealth(), 30000);
  }

  ngOnDestroy(): void {
    if (this.intervalId) clearInterval(this.intervalId);
  }

  private loadHealth(): void {
    this.monitorService.getHealth().subscribe({
      next: h => {
        this.health = h;
        this.pmServices = h.services.filter(s => s.group === 'PM');
        this.mvpServices = h.services.filter(s => s.group === 'MVP');
        this.cdr.markForCheck();
      },
      error: () => {
        this.cdr.markForCheck();
      }
    });
  }
}
