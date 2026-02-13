export interface BudgetOverview {
  totalBudget: number;
  totalSpent: number;
  remaining: number;
  burnRatePerWeek: number;
  projectedTotal: number;
  projectedVariance: number;
  currency: string;
  hourlyRate: number;
  contingencyPercent: number;
  budgetUsedPercent: number;
  costPerSession: number;
  costPerHour: number;
  weeksElapsed: number;
  weeksRemaining: number;
  sprints: SprintBudget[];
}

export interface SprintBudget {
  sprintNumber: number;
  sprintName: string;
  color: string;
  status: string;
  plannedHours: number;
  actualHours: number;
  plannedCost: number;
  actualCost: number;
  variance: number;
}

export interface BudgetUpdate {
  hourlyRate: number;
  totalBudget: number;
  currency: string;
  contingencyPercent: number;
}
