import { Component } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { CommonModule } from '@angular/common';
import { ToastrService } from 'ngx-toastr';
import { ThemeService } from '../../services/theme';
import { NotificationBellComponent } from '../../components/notification-bell/notification-bell.component';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, NotificationBellComponent],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.scss']
})
export class Navbar {
  isLoggedIn$: Observable<boolean>;
  username$: Observable<string | null>;
  currentTheme$: Observable<'light' | 'dark'>;
  isAdmin$: Observable<boolean>;

  isMobileMenuOpen = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private toastr: ToastrService,
    private themeService: ThemeService
  ) {
    this.isLoggedIn$ = this.authService.isLoggedIn$;
    this.username$ = this.authService.currentUser$.pipe(
      map(user => user?.username ?? null)
    );
    this.currentTheme$ = this.themeService.currentTheme$;
    this.isAdmin$ = this.authService.currentUser$.pipe(
      map(user => user?.roles.includes('ROLE_ADMIN') ?? false)
    );
  }

  logout(): void {
    this.authService.logout();
    this.toastr.info('You have been successfully logged out.', 'Goodbye!');
    this.router.navigate(['/login']);
  }

  toggleTheme(): void {
    this.themeService.toggleTheme();
  }

  toggleMobileMenu(): void {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  closeMobileMenu(): void {
    this.isMobileMenuOpen = false;
  }
}