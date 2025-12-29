import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from './services/auth.service';
import { map, take } from 'rxjs/operators';


@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): Observable<boolean> {
  return this.authService.currentUser$.pipe(
    take(1),
    map(user => {
      const isAdmin = !!user && user.roles.includes('ROLE_ADMIN');
      if (!isAdmin) {
        this.router.navigate(['/']);
      }
      return isAdmin;
    })
  );
}
}