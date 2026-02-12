import { HttpInterceptorFn, HttpRequest, HttpHandlerFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { catchError, switchMap, throwError } from 'rxjs';

const AUTH_URLS = ['/auth/login', '/auth/refresh', '/auth/logout'];

function isAuthRequest(url: string): boolean {
  return AUTH_URLS.some(path => url.includes(path));
}

function cloneWithToken(req: HttpRequest<unknown>, token: string): HttpRequest<unknown> {
  return req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
}

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  // Don't attach tokens to auth endpoints
  if (isAuthRequest(req.url)) {
    return next(req);
  }

  const token = authService.getToken();

  // No token at all — pass through
  if (!token) {
    return next(req);
  }

  // Token is expired — attempt refresh before sending
  if (authService.isTokenExpired()) {
    if (authService.getRefreshToken()) {
      return authService.refreshTokens().pipe(
        switchMap(res => {
          return next(cloneWithToken(req, res.token));
        }),
        catchError(err => {
          return throwError(() => err);
        })
      );
    }
    // No refresh token — logout
    authService.logout();
    return throwError(() => new Error('Token expired'));
  }

  // Token expiring soon — trigger background refresh (non-blocking)
  if (authService.isTokenExpiringSoon()) {
    authService.refreshTokens().subscribe();
  }

  // Attach valid token and handle 401
  return next(cloneWithToken(req, token)).pipe(
    catchError(err => {
      if (err.status === 401 && authService.getRefreshToken()) {
        return authService.refreshTokens().pipe(
          switchMap(res => next(cloneWithToken(req, res.token))),
          catchError(refreshErr => {
            authService.logout();
            return throwError(() => refreshErr);
          })
        );
      }
      if (err.status === 401) {
        authService.logout();
      }
      return throwError(() => err);
    })
  );
};
