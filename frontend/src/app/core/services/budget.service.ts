import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { BudgetOverview, BudgetUpdate } from '../models/budget.model';

@Injectable({ providedIn: 'root' })
export class BudgetService {

  constructor(private api: ApiService) {}

  getBudgetOverview(): Observable<BudgetOverview> {
    return this.api.get<BudgetOverview>('/budget');
  }

  updateBudget(data: BudgetUpdate): Observable<BudgetOverview> {
    return this.api.put<BudgetOverview>('/budget', data);
  }
}
