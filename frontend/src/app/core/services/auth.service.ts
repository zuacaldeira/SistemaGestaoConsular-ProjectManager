import { Injectable, OnDestroy } from '@angular/core';
import { ApiService } from './api.service';
import { BehaviorSubject, Observable, tap, catchError, of, switchMap } from 'rxjs';
import { Router } from '@angular/router';

interface AuthResponse {
  token: string;
  refreshToken: string;
  role: string;
  expiresIn: number;
}

@Injectable({ providedIn: 'root' })
export class AuthService implements OnDestroy {
  private tokenKey = 'sgcd_pm_token';
  private refreshTokenKey = 'sgcd_pm_refresh_token';
  private roleKey = 'sgcd_pm_role';
  private isAuthenticated$ = new BehaviorSubject<boolean>(this.hasValidToken());
  private refreshTimer: ReturnType<typeof setTimeout> | null = null;
  private refreshInProgress: Observable<AuthResponse> | null = null;

  constructor(private api: ApiService, private router: Router) {
    this.scheduleTokenRefresh();
  }

  ngOnDestroy(): void {
    this.clearRefreshTimer();
  }

  login(username: string, password: string): Observable<AuthResponse> {
    return this.api.post<AuthResponse>('/auth/login', { username, password }).pipe(
      tap(res => {
        this.storeTokens(res);
        this.isAuthenticated$.next(true);
        this.scheduleTokenRefresh();
      })
    );
  }

  logout(): void {
    const refreshToken = this.getRefreshToken();
    const token = this.getToken();

    // Fire-and-forget server-side logout
    if (refreshToken || token) {
      this.api.post('/auth/logout', refreshToken ? { refreshToken } : {}).pipe(
        catchError(() => of(null))
      ).subscribe();
    }

    this.clearStorage();
    this.clearRefreshTimer();
    this.isAuthenticated$.next(false);
    this.router.navigate(['/login']);
  }

  refreshTokens(): Observable<AuthResponse> {
    if (this.refreshInProgress) {
      return this.refreshInProgress;
    }

    const refreshToken = this.getRefreshToken();
    if (!refreshToken) {
      this.logout();
      return of(null as any);
    }

    this.refreshInProgress = this.api.post<AuthResponse>('/auth/refresh', { refreshToken }).pipe(
      tap(res => {
        this.storeTokens(res);
        this.isAuthenticated$.next(true);
        this.scheduleTokenRefresh();
        this.refreshInProgress = null;
      }),
      catchError(err => {
        this.refreshInProgress = null;
        this.logout();
        throw err;
      })
    );

    return this.refreshInProgress;
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.refreshTokenKey);
  }

  getRole(): string | null {
    return localStorage.getItem(this.roleKey);
  }

  isLoggedIn(): boolean {
    return this.hasValidToken();
  }

  isTokenExpired(): boolean {
    const expiry = this.getTokenExpiry();
    if (!expiry) return true;
    return Date.now() / 1000 >= expiry;
  }

  isTokenExpiringSoon(marginMs = 300000): boolean {
    const expiry = this.getTokenExpiry();
    if (!expiry) return true;
    return Date.now() / 1000 >= expiry - marginMs / 1000;
  }

  get authenticated(): Observable<boolean> {
    return this.isAuthenticated$.asObservable();
  }

  private storeTokens(res: AuthResponse): void {
    localStorage.setItem(this.tokenKey, res.token);
    localStorage.setItem(this.refreshTokenKey, res.refreshToken);
    localStorage.setItem(this.roleKey, res.role);
  }

  private clearStorage(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.refreshTokenKey);
    localStorage.removeItem(this.roleKey);
  }

  private hasValidToken(): boolean {
    const token = localStorage.getItem(this.tokenKey);
    if (!token) return false;
    return !this.isTokenExpired();
  }

  private getTokenExpiry(): number | null {
    const token = this.getToken();
    if (!token) return null;
    try {
      const payload = token.split('.')[1];
      const decoded = JSON.parse(atob(payload));
      return decoded.exp || null;
    } catch {
      return null;
    }
  }

  private scheduleTokenRefresh(): void {
    this.clearRefreshTimer();
    const expiry = this.getTokenExpiry();
    if (!expiry) return;

    // Refresh 5 minutes before expiry
    const refreshAt = (expiry - 300) * 1000 - Date.now();
    if (refreshAt <= 0) {
      // Token is already expiring soon, try refresh now if we have a refresh token
      if (this.getRefreshToken()) {
        this.refreshTokens().subscribe();
      }
      return;
    }

    this.refreshTimer = setTimeout(() => {
      this.refreshTokens().subscribe();
    }, refreshAt);
  }

  private clearRefreshTimer(): void {
    if (this.refreshTimer) {
      clearTimeout(this.refreshTimer);
      this.refreshTimer = null;
    }
  }
}
