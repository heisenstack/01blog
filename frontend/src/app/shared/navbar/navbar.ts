import { Component, ChangeDetectorRef, OnDestroy, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { Observable, Subscription } from 'rxjs';
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
export class Navbar implements OnInit, OnDestroy {
  isLoggedIn$: Observable<boolean>;
  username$: Observable<string | null>;
  currentTheme$: Observable<'light' | 'dark'>;
  isAdmin$: Observable<boolean>;
  currentUsername: string = '';

  isMobileMenuOpen = false;
  private subscriptions = new Subscription();

  constructor(
    private authService: AuthService,
    private router: Router,
    private toastr: ToastrService,
    private themeService: ThemeService,
    private cdr: ChangeDetectorRef
  ) {
    this.isLoggedIn$ = this.authService.isLoggedIn$;
    this.username$ = this.authService.currentUser$.pipe(
      map(user => user ? user.username : null)
    );
    this.currentTheme$ = this.themeService.currentTheme$;
    this.isAdmin$ = this.authService.currentUser$.pipe(
      map(user => user ? user.roles.includes('ROLE_ADMIN') : false)
    );
  }

  ngOnInit(): void {
    this.subscriptions.add(
      this.authService.isLoggedIn$.subscribe(isLoggedIn => {
        this.cdr.detectChanges();
      })
    );

    this.subscriptions.add(
      this.authService.currentUser$.subscribe(user => {
        this.currentUsername = user?.username || '';
        this.cdr.detectChanges();
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
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