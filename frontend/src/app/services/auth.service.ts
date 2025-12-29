import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode';
import { UserProfile } from '../models/user-profile.model'; 

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

export interface User {
  id: number;
  username: string;
  roles: string[];
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private authApiUrl = 'http://localhost:8080/api/auth'; 
  private usersApiUrl = 'http://localhost:8080/api/users'; 
  private tokenKey = 'authToken';

  private loggedIn = new BehaviorSubject<boolean>(this.hasToken());
  private currentUserSubject = new BehaviorSubject<User | null>(null);

  public isLoggedIn$ = this.loggedIn.asObservable();
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {
    this.checkTokenOnLoad();
  }

  public isAuthenticated(): boolean {
    return this.loggedIn.value;
  }

  public getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  public hasRole(role: string): boolean {
    const user = this.getCurrentUser();
    return !!user && user.roles.includes(role);
  }

  private hasToken(): boolean {
    return !!localStorage.getItem(this.tokenKey);
  }

  private checkTokenOnLoad(): void {
    const token = this.getToken();
    if (token) {
      try {
        const decodedToken: DecodedToken = jwtDecode(token);
        if (decodedToken.exp * 1000 < Date.now()) {
          this.logout();
        } else {
          // console.log("Token: ", decodedToken);
          
          this.loggedIn.next(true);
          this.currentUserSubject.next({
            id: decodedToken.userId,
            username: decodedToken.sub,
            roles: decodedToken.roles || []
          });
        }
      } catch (error) {
        this.logout();
      }
    }
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
          
          
          const user = { 
            id: decodedToken.userId, 
            username: decodedToken.sub,
            roles: decodedToken.roles || []
          };
          
          this.currentUserSubject.next(user);
          
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    this.loggedIn.next(false);
    this.currentUserSubject.next(null);
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
}