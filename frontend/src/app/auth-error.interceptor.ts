import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse,
} from '@angular/common/http';
import { Observable, throwError, EMPTY } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { AuthService } from './services/auth.service';
import { ToastrService } from 'ngx-toastr';

@Injectable()
export class AuthErrorInterceptor implements HttpInterceptor {
  private isRedirecting = false;

  constructor(private authService: AuthService, private toastr: ToastrService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
   console.log('AuthErrorInterceptor'); 

    return next.handle(req).pipe(
      tap((event) => {
        console.log('AuthErrorInterceptor: ', event);
      }),

      catchError((error: HttpErrorResponse) => {
        if (this.isRedirecting) {
          return throwError(() => error);
        }

        if (error.status === 401 && this.authService.getToken()) {
          this.handleLogout('Your session has expired. Please log in again.', 'Session Expired');
          return EMPTY;
        }

        if (error.status === 403) {
          const errorStatus = error.error?.status || '';
          const errorMessage = error.error?.message || '';

          if (errorStatus === 'USER_BANNED' || errorStatus === 'USER_DELETED') {
            const isBanned = errorStatus === 'USER_BANNED';

            this.handleLogout(
              isBanned
                ? 'Your account has been banned. Please contact support.'
                : 'Your account has been deleted. Please contact support if you believe this is an error.',
              isBanned ? 'Account Banned' : 'Account Deleted'
            );
            return EMPTY;
          }
        }

        return throwError(() => error);
      })
    );
  }

  private handleLogout(message: string, title: string): void {
    if (this.isRedirecting) return;

    this.isRedirecting = true;

    this.toastr.clear();

    localStorage.removeItem('authToken');

    this.toastr.error(message, title, {
      timeOut: 8000,
      closeButton: true,
      disableTimeOut: false,
    });

    setTimeout(() => {
      window.location.href = '/login';
    }, 100);
  }
}
