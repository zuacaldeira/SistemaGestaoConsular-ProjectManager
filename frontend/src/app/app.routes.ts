import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'stakeholder',
    loadComponent: () => import('./features/stakeholder/stakeholder.component').then(m => m.StakeholderComponent)
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./shared/components/layout.component').then(m => m.LayoutComponent),
    children: [
      { path: '', loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent) },
      { path: 'progress', loadComponent: () => import('./features/progress/progress.component').then(m => m.ProgressComponent) },
      { path: 'sprints', loadComponent: () => import('./features/sprints/sprint-list.component').then(m => m.SprintListComponent) },
      { path: 'sprints/:id', loadComponent: () => import('./features/sprints/sprint-detail.component').then(m => m.SprintDetailComponent) },
      { path: 'tasks', loadComponent: () => import('./features/tasks/task-list.component').then(m => m.TaskListComponent) },
      { path: 'tasks/:id', loadComponent: () => import('./features/tasks/task-detail.component').then(m => m.TaskDetailComponent) },
      { path: 'prompts', loadComponent: () => import('./features/prompts/prompt-generator.component').then(m => m.PromptGeneratorComponent) },
      { path: 'calendar', loadComponent: () => import('./features/calendar/calendar.component').then(m => m.CalendarComponent) },
      { path: 'monitor', loadComponent: () => import('./features/monitor/monitor.component').then(m => m.MonitorComponent) },
      { path: 'budget', loadComponent: () => import('./features/budget/budget.component').then(m => m.BudgetComponent) },
      { path: 'reports', loadComponent: () => import('./features/reports/reports.component').then(m => m.ReportsComponent) },
    ]
  },
  { path: '**', redirectTo: '' }
];
