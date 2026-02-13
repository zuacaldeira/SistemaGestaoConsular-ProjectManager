import { Task } from './task.model';
import { Sprint } from './sprint.model';

export interface Dashboard {
  projectProgress: number;
  totalSessions: number;
  completedSessions: number;
  totalHoursPlanned: number;
  totalHoursSpent: number;
  activeSprint: Sprint;
  todayTask: Task;
  recentTasks: Task[];
  sprintSummaries: SprintSummary[];
  upcomingBlockedDays: BlockedDay[];
  weekProgress: WeekProgress;
}

export interface SprintSummary {
  sprintNumber: number;
  name: string;
  progress: number;
  status: string;
  color: string;
}

export interface WeekProgress {
  weekTasks: number;
  weekCompleted: number;
  weekHoursPlanned: number;
  weekHoursSpent: number;
}

export interface BlockedDay {
  id: number;
  blockedDate: string;
  dayOfWeek: string;
  blockType: 'HOLIDAY' | 'SCC_EVENT';
  reason: string;
  hoursLost: number;
}

export interface StakeholderDashboard {
  projectName: string;
  client: string;
  overallProgress: number;
  totalSessions: number;
  completedSessions: number;
  totalHoursPlanned: number;
  totalHoursSpent: number;
  startDate: string;
  targetDate: string;
  daysRemaining: number;
  sprints: StakeholderSprint[];
  milestones: Milestone[];
  weeklyActivity: WeeklyActivity;
  budget?: BudgetSummary;
  lastUpdated: string;
}

export interface BudgetSummary {
  totalBudget: number;
  totalSpent: number;
  remaining: number;
  budgetUsedPercent: number;
  currency: string;
}

export interface StakeholderSprint {
  number: number;
  name: string;
  nameEn: string;
  progress: number;
  status: string;
  startDate: string;
  endDate: string;
  sessions: number;
  completedSessions: number;
  hours: number;
  hoursSpent: number;
  color: string;
  focus: string;
}

export interface Milestone {
  name: string;
  targetDate: string;
  status: string;
}

export interface WeeklyActivity {
  sessionsThisWeek: number;
  hoursThisWeek: number;
  tasksCompletedThisWeek: number;
}

export interface ProjectProgress {
  totalSessions: number;
  completedSessions: number;
  totalHoursPlanned: number;
  totalHoursSpent: number;
  overallProgress: number;
  daysRemaining: number;
  startDate: string;
  targetDate: string;
  totalPlanned: number;
  totalInProgress: number;
  totalCompleted: number;
  totalBlocked: number;
  totalSkipped: number;
  avgSessionsPerWeek: number;
  avgHoursPerWeek: number;
  weeksElapsed: number;
  weeksRemaining: number;
  sprints: SprintProgress[];
}

export interface SprintProgress {
  sprintNumber: number;
  name: string;
  status: string;
  color: string;
  startDate: string;
  endDate: string;
  totalSessions: number;
  completedSessions: number;
  totalHours: number;
  actualHours: number;
  progress: number;
  plannedTasks: number;
  inProgressTasks: number;
  completedTasks: number;
  blockedTasks: number;
  skippedTasks: number;
}

export interface CalendarData {
  year: number;
  month: number;
  days: CalendarDay[];
}

export interface CalendarDay {
  date: string;
  dayOfWeek: string;
  isBlocked: boolean;
  blockReason: string;
  task: Task;
  isWorkDay: boolean;
}
