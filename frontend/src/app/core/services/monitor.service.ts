import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { MonitorHealth } from '../models/monitor.model';

@Injectable({ providedIn: 'root' })
export class MonitorService {

  constructor(private api: ApiService) {}

  getHealth(): Observable<MonitorHealth> {
    return this.api.get<MonitorHealth>('/monitor/health');
  }
}
