import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Navbar } from './shared/navbar/navbar'; 
import { ThemeService } from './services/theme';
import { NotificationService } from './services/notification.service';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, Navbar],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App implements OnInit {
  // title = 'frontend';

  constructor(
    private themeService: ThemeService,
    private notificationService: NotificationService,
    private authService: AuthService
  ) {
    this.themeService.initTheme();
    this.authService.initializeAuth();
  }

  ngOnInit(): void {
    if (this.authService.isAuthenticated()) {
      console.log('User is logged in');
    }
  }
}