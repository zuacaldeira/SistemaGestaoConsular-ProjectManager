import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { BreakpointObserver } from '@angular/cdk/layout';
import { AuthService } from '../../core/services/auth.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, MatToolbarModule, MatListModule, MatIconModule, MatButtonModule],
  template: `
    <div class="layout">
      <mat-toolbar class="header">
        @if (isMobile) {
          <button mat-icon-button (click)="sidebarOpen = !sidebarOpen">
            <mat-icon>menu</mat-icon>
          </button>
        }
        <span class="logo">SGCD-PM</span>
        <span class="spacer"></span>
        <span class="user-info">{{ auth.getRole() }}</span>
        <button mat-icon-button (click)="auth.logout()">
          <mat-icon>logout</mat-icon>
        </button>
      </mat-toolbar>

      <div class="content-area">
        @if (sidebarOpen || !isMobile) {
          <nav class="sidebar" [class.sidebar-mobile]="isMobile">
            <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{exact: true}" (click)="onNavClick()">
              <mat-icon>dashboard</mat-icon> Dashboard
            </a>
            <a routerLink="/progress" routerLinkActive="active" (click)="onNavClick()">
              <mat-icon>trending_up</mat-icon> Progresso
            </a>
            <a routerLink="/sprints" routerLinkActive="active" (click)="onNavClick()">
              <mat-icon>flag</mat-icon> Sprints
            </a>
            <a routerLink="/tasks" routerLinkActive="active" (click)="onNavClick()">
              <mat-icon>task_alt</mat-icon> Tarefas
            </a>
            <a routerLink="/prompts" routerLinkActive="active" (click)="onNavClick()">
              <mat-icon>smart_toy</mat-icon> Prompts
            </a>
            <a routerLink="/calendar" routerLinkActive="active" (click)="onNavClick()">
              <mat-icon>calendar_month</mat-icon> Calendário
            </a>
            <a routerLink="/reports" routerLinkActive="active" (click)="onNavClick()">
              <mat-icon>assessment</mat-icon> Relatórios
            </a>
            <div class="divider"></div>
            <a routerLink="/stakeholder" target="_blank" (click)="onNavClick()">
              <mat-icon>visibility</mat-icon> Stakeholder
            </a>
          </nav>
        }
        @if (isMobile && sidebarOpen) {
          <div class="overlay" (click)="sidebarOpen = false"></div>
        }

        <main class="main-content">
          <router-outlet />
        </main>
      </div>
    </div>
  `,
  styles: [`
    .layout { display: flex; flex-direction: column; height: 100vh; }
    .header {
      background: var(--angola-black);
      color: white;
      position: sticky;
      top: 0;
      z-index: 100;
    }
    .logo {
      font-family: 'Playfair Display', serif;
      font-size: 20px;
      font-weight: 700;
      color: var(--angola-gold);
    }
    .spacer { flex: 1; }
    .user-info { margin-right: 8px; font-size: 14px; opacity: 0.8; }
    .content-area { display: flex; flex: 1; overflow: hidden; position: relative; }
    .sidebar {
      width: 220px;
      background: var(--surface);
      border-right: 1px solid var(--border-light);
      display: flex;
      flex-direction: column;
      padding: 8px;
      overflow-y: auto;
      flex-shrink: 0;
    }
    .sidebar-mobile {
      position: absolute;
      top: 0;
      left: 0;
      bottom: 0;
      z-index: 50;
      box-shadow: 4px 0 12px rgba(0,0,0,0.15);
    }
    .overlay {
      position: absolute;
      top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0,0,0,0.3);
      z-index: 40;
    }
    .sidebar a {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 10px 16px;
      text-decoration: none;
      color: var(--text-secondary);
      border-radius: 8px;
      font-size: 14px;
      transition: all 0.2s;
    }
    .sidebar a:hover { background: var(--surface-alt); color: var(--text-primary); }
    .sidebar a.active { background: var(--angola-red); color: white; }
    .sidebar a.active mat-icon { color: white; }
    .divider { height: 1px; background: var(--border-light); margin: 8px 16px; }
    .main-content { flex: 1; overflow-y: auto; padding: 24px; }

    @media (max-width: 768px) {
      .user-info { display: none; }
      .main-content { padding: 16px; }
    }
  `]
})
export class LayoutComponent implements OnInit, OnDestroy {
  isMobile = false;
  sidebarOpen = false;
  private bpSub!: Subscription;

  constructor(
    public auth: AuthService,
    private breakpointObserver: BreakpointObserver
  ) {}

  ngOnInit(): void {
    this.bpSub = this.breakpointObserver.observe(['(max-width: 768px)'])
      .subscribe(result => {
        this.isMobile = result.matches;
        if (!result.matches) {
          this.sidebarOpen = false;
        }
      });
  }

  ngOnDestroy(): void {
    this.bpSub?.unsubscribe();
  }

  onNavClick(): void {
    if (this.isMobile) {
      this.sidebarOpen = false;
    }
  }
}
