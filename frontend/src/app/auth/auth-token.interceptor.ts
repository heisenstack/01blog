import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthTokenInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    console.log("AuthTokenInterceptor");

    const authToken = this.authService.getToken();

    // console.log('AuthTokenInterceptor here:', next);

    if (authToken) {
      const authReq = request.clone({
        setHeaders: {
          Authorization: `Bearer ${authToken}`
        }
      });
      
      
      return next.handle(authReq).pipe(
        tap((event) => {
        console.log('AuthTokenInterceptor response: ', event);
      }),
      );
    }

    return next.handle(request);
  }
}