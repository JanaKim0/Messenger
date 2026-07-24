import { provideHttpClient, withInterceptors } from '@angular/common/http';
import {
  ApplicationConfig,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
  inject,
} from '@angular/core';
import { provideRouter } from '@angular/router';
import { catchError, of } from 'rxjs';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { AuthService } from './core/services/auth.service';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    // If a token is already stored, restore the current user before the app renders.
    provideAppInitializer(() => {
      const auth = inject(AuthService);
      if (!auth.token) {
        return of(null);
      }
      return auth.loadCurrentUser().pipe(
        catchError(() => {
          auth.clearSession();
          return of(null);
        }),
      );
    }),
  ],
};
