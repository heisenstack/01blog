import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ToastrService } from 'ngx-toastr';

@Injectable()
export class RateLimitInterceptor implements HttpInterceptor {
  private isShowingRateLimitError = false;

  constructor(private toastr: ToastrService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    console.log("Reeeeeeeeeeeeeeq is: " , req);
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        
        if (error.status === 429) {
          this.handleRateLimitError(error);
        }
        
        return throwError(() => error);
      })
    );
  }

  private handleRateLimitError(error: HttpErrorResponse): void {
    if (this.isShowingRateLimitError) {
      return;
    }

    this.isShowingRateLimitError = true;

    const message = error.error?.message || 
                   'You are performing actions too quickly. Please slow down and try again.';

    this.toastr.warning(message, 'Slow Down!', {
      timeOut: 5000,
      closeButton: true,
      progressBar: true
    });

    setTimeout(() => {  
      this.isShowingRateLimitError = false;
    }, 2000);

    // console.warn('Rate limit exceeded:', error);
  }
}