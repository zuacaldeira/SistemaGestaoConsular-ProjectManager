import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { map, catchError, of } from 'rxjs';

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn() && !authService.isTokenExpired()) {
    return true;
  }

  // Token expired but refresh token exists — try refresh
  if (authService.getRefreshToken()) {
    return authService.refreshTokens().pipe(
      map(() => true),
      catchError(() => of(router.createUrlTree(['/login'])))
    );
  }

  return router.createUrlTree(['/login']);
};
