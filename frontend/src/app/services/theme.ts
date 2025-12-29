import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  private currentTheme = new BehaviorSubject<'light' | 'dark'>('light');
  public currentTheme$ = this.currentTheme.asObservable();

  constructor() {}

  initTheme(): void {
    const startingTheme = (localStorage.getItem('theme') as 'light' | 'dark') || 'light';
    this.setTheme(startingTheme);
  }

  toggleTheme(): void {
    const newTheme = this.currentTheme.value === 'light' ? 'dark' : 'light';
    this.setTheme(newTheme);
  }

  private setTheme(theme: 'light' | 'dark'): void {
    localStorage.setItem('theme', theme);

    this.currentTheme.next(theme);

    if (theme === 'dark') {
      document.body.classList.add('dark-theme');
    } else {
      document.body.classList.remove('dark-theme');
    }
  }
}
