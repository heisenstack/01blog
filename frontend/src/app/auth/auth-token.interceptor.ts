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

    const authToken = this.authService.getToken();

    if (authToken) {
      const authReq = request.clone({
        setHeaders: {
          Authorization: `Bearer ${authToken}`
        }
      });
      
      
      return next.handle(authReq);
    }

    return next.handle(request);
  }
}