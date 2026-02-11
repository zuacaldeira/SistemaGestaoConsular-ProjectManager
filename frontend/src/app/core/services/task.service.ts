import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs';
import { Task, TaskNote, TaskExecution, Prompt } from '../models/task.model';

interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({ providedIn: 'root' })
export class TaskService {
  constructor(private api: ApiService) {}

  findAll(params?: Record<string, any>): Observable<Page<Task>> {
    return this.api.get<Page<Task>>('/tasks', params);
  }

  findById(id: number): Observable<Task> {
    return this.api.get<Task>(`/tasks/${id}`);
  }

  findToday(): Observable<Task> {
    return this.api.get<Task>('/tasks/today');
  }

  findNext(): Observable<Task> {
    return this.api.get<Task>('/tasks/next');
  }

  update(id: number, data: any): Observable<Task> {
    return this.api.patch<Task>(`/tasks/${id}`, data);
  }

  start(id: number): Observable<Task> {
    return this.api.post<Task>(`/tasks/${id}/start`);
  }

  complete(id: number, data?: any): Observable<Task> {
    return this.api.post<Task>(`/tasks/${id}/complete`, data);
  }

  block(id: number, reason: string): Observable<Task> {
    return this.api.post<Task>(`/tasks/${id}/block`, { reason });
  }

  skip(id: number): Observable<Task> {
    return this.api.post<Task>(`/tasks/${id}/skip`);
  }

  getPrompt(id: number): Observable<Prompt> {
    return this.api.get<Prompt>(`/tasks/${id}/prompt`);
  }

  addNote(id: number, note: Partial<TaskNote>): Observable<TaskNote> {
    return this.api.post<TaskNote>(`/tasks/${id}/notes`, note);
  }

  getExecutions(id: number): Observable<TaskExecution[]> {
    return this.api.get<TaskExecution[]>(`/tasks/${id}/executions`);
  }

  addExecution(id: number, data: Partial<TaskExecution>): Observable<TaskExecution> {
    return this.api.post<TaskExecution>(`/tasks/${id}/executions`, data);
  }
}
