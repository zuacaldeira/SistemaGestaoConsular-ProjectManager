import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, MatCardModule, MatInputModule, MatButtonModule, MatFormFieldModule],
  template: `
    <div class="login-container">
      <mat-card class="login-card">
        <div class="login-header">
          <h1>SGCD-PM</h1>
          <p>Sistema de Gestão de Projecto</p>
        </div>

        <mat-card-content>
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Utilizador</mat-label>
            <input matInput [(ngModel)]="username" (keyup.enter)="login()">
          </mat-form-field>

          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Palavra-passe</mat-label>
            <input matInput type="password" [(ngModel)]="password" (keyup.enter)="login()">
          </mat-form-field>

          @if (error) {
            <p class="error">{{ error }}</p>
          }
        </mat-card-content>

        <mat-card-actions>
          <button mat-raised-button class="btn-primary full-width" (click)="login()" [disabled]="loading">
            {{ loading ? 'A entrar...' : 'Entrar' }}
          </button>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .login-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      background: var(--angola-black);
    }
    .login-card {
      width: 400px;
      padding: 32px;
    }
    .login-header {
      text-align: center;
      margin-bottom: 24px;
    }
    .login-header h1 {
      font-family: 'Playfair Display', serif;
      color: var(--angola-red);
      margin: 0;
    }
    .login-header p {
      color: var(--text-secondary);
      margin: 4px 0 0;
    }
    .full-width { width: 100%; }
    .error { color: var(--angola-red); font-size: 14px; text-align: center; }
    mat-card-actions { padding: 0 !important; }
  `]
})
export class LoginComponent {
  username = '';
  password = '';
  error = '';
  loading = false;

  constructor(private auth: AuthService, private router: Router) {}

  login(): void {
    if (!this.username || !this.password) return;
    this.loading = true;
    this.error = '';

    this.auth.login(this.username, this.password).subscribe({
      next: () => {
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.error = err.status === 429
          ? 'Demasiadas tentativas. Tente novamente em 5 minutos.'
          : 'Credenciais inválidas';
        this.loading = false;
      }
    });
  }
}
