import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserProfile } from '../models/user-profile.model';
import { UserSuggestion } from '../models/user-suggestion.model';
import { UserSuggestionResponse } from '../models/user-suggestion-response.model';
export interface UserReportRequest {
  reason: string;
  details?: string; 
}
@Injectable({
  providedIn: 'root'
})
export class UserService {
  
  private apiUrl = 'http://localhost:8080/api/users';

  constructor(private http: HttpClient) {}

  getUserProfile(username: string, page: number, size: number): Observable<UserProfile> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
      
    return this.http.get<UserProfile>(`${this.apiUrl}/${username}`, { params });
  }

  subscribe(username: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${username}/follow`, {});
  }

  unsubscribe(username: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${username}/unfollow`);
  }
  reportUser(username: string, reportData: UserReportRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${username}/report`, reportData);
  }
  getSuggestedUsers(page: number = 0, size: number = 10): Observable<UserSuggestionResponse> {
  const params = new HttpParams()
    .set('page', page.toString())
    .set('size', size.toString());
  
  return this.http.get<UserSuggestionResponse>(`${this.apiUrl}/suggestions`, { params });
}

getFollowingUsers(page: number = 0, size: number = 10): Observable<UserSuggestionResponse> {
  const params = new HttpParams()
    .set('page', page.toString())
    .set('size', size.toString());
  
  return this.http.get<UserSuggestionResponse>(`${this.apiUrl}/following`, { params });
}
}