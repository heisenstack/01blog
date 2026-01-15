import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, catchError, of, map } from 'rxjs';
import { Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode';
import { UserProfile } from '../models/user-profile.model';
import { User } from '../models/user.model';

export interface AuthResponse {
  accessToken: string;
}

export interface DecodedToken {
  sub: string; 
  userId: number;
  roles: string[];
  iat: number;
  exp: number;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private authApiUrl = 'http://localhost:8080/api/auth'; 
  private usersApiUrl = 'http://localhost:8080/api/users'; 
  private tokenKey = 'authToken';

  private loggedIn = new BehaviorSubject<boolean>(false); 
  private currentUserSubject = new BehaviorSubject<User | null>(null);

  public isLoggedIn$ = this.loggedIn.asObservable();
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {

  }


  initializeAuth(): void {
    this.checkTokenOnLoad();
  }

  private checkTokenOnLoad(): void {
    const token = this.getToken();
    
    if (!token) {
      this.clearAuthState();
      return;
    }

    try {
      const decodedToken: DecodedToken = jwtDecode(token);
      
      const currentTime = Date.now();
      const expirationTime = decodedToken.exp * 1000;
      
      if (expirationTime < currentTime) {
        console.log('Token expired');
        this.clearAuthState();
        return;
      }

      console.log('Token valid, setting user as authenticated');
      this.loggedIn.next(true);
      this.currentUserSubject.next({ 
        id: decodedToken.userId, 
        username: decodedToken.sub,
        roles: decodedToken.roles || []
      });

      this.verifyTokenInBackground();

    } catch (error) {
      console.error('Token decode error:', error);
      this.clearAuthState();
    }
  }


  private verifyTokenInBackground(): void {
    this.http.get(`${this.authApiUrl}/verify`).pipe(
      catchError(error => {
        console.log('error :', error.status);

        if (error.status === 401) {
          console.log('Token is invalid, logging out');
          this.logout();
        }
        return of(null);
      })
    ).subscribe();
  }


  public clearAuthState(): void {
    localStorage.removeItem(this.tokenKey);
    this.loggedIn.next(false);
    this.currentUserSubject.next(null);
  }

  register(user: any): Observable<any> {
    return this.http.post(`${this.authApiUrl}/signup`, user); 
  }

  login(credentials: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.authApiUrl}/signin`, credentials).pipe( 
      tap(response => {
        if (response?.accessToken) {
          this.setToken(response.accessToken);
          const decodedToken: DecodedToken = jwtDecode(response.accessToken);
          this.loggedIn.next(true);
          this.currentUserSubject.next({ 
            id: decodedToken.userId, 
            username: decodedToken.sub,
            roles: decodedToken.roles || []
          });
        }
      }),
      catchError(error => {
        console.error('Login error:', error);
        throw error;
      })
    );
  }

  logout(): void {
    this.clearAuthState();
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  private setToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
  }

  getUserProfile(username: string): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.usersApiUrl}/${username}`);
  }

  subscribe(username: string): Observable<void> {
    return this.http.post<void>(`${this.usersApiUrl}/${username}/subscribe`, {});
  }

  unsubscribe(username: string): Observable<void> {
    return this.http.delete<void>(`${this.usersApiUrl}/${username}/unsubscribe`);
  }

  isAuthenticated(): boolean {
    return this.loggedIn.value;
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }
}