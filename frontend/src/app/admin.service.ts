import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DashboardStats } from './models/dashboard-stats.model';
import { Report } from './models/report.model';
import { UserAdminView } from './models/user-admin-view.model';
import { UserReport } from './models/user-report.model';
import { ReportResponse } from './models/report-response.model';
import { UserReportResponse } from './models/user-report-response.model';
import { UserAdminViewResponse } from './models/user-admin-view-response.model';
import { HidePostRequest } from './models/hide-post-request.model';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = 'http://localhost:8080/api/admin';

  constructor(private http: HttpClient) { }

  getDashboardStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.apiUrl}/stats`);
  }

  getReportedPosts(page: number = 0, size: number = 10): Observable<ReportResponse> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<ReportResponse>(`${this.apiUrl}/reports`, { params });
  }

  getReportedUsers(page: number = 0, size: number = 10): Observable<UserReportResponse> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<UserReportResponse>(`${this.apiUrl}/reports/users`, { params });
  }

  dismissUserReport(reportId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/reports/users/${reportId}`);
  }

  dismissReport(reportId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/reports/${reportId}`);
  }

  deletePost(postId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/posts/${postId}`);
  }

  getAllUsers(page: number = 0, size: number = 10): Observable<UserAdminViewResponse> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<UserAdminViewResponse>(`${this.apiUrl}/users`, { params });
  }

  deleteUser(userId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/users/${userId}`);
  }


hidePost(postId: number, reason?: string): Observable<any> {
  const payload: HidePostRequest = { postId, reason };
  return this.http.put(`${this.apiUrl}/posts/${postId}/hide`, payload);
}


unhidePost(postId: number): Observable<any> {
  return this.http.put(`${this.apiUrl}/posts/${postId}/unhide`, {});
}


getHiddenPosts(page: number = 0, size: number = 10): Observable<any> {
  const params = new HttpParams()
    .set('page', page.toString())
    .set('size', size.toString());
  
  return this.http.get<any>(`${this.apiUrl}/posts/hidden`, { params });
}


getHiddenPostsCount(): Observable<number> {
  return this.http.get<number>(`${this.apiUrl}/posts/hidden/count`);
}


banUser(userId: number): Observable<void> {
  return this.http.put<void>(`${this.apiUrl}/users/${userId}/ban`, {});
}

unbanUser(userId: number): Observable<void> {
  return this.http.put<void>(`${this.apiUrl}/users/${userId}/unban`, {});
}

getBannedUsers(page: number = 0, size: number = 10): Observable<UserAdminViewResponse> {
  const params = new HttpParams()
    .set('page', page.toString())
    .set('size', size.toString());
  
  return this.http.get<UserAdminViewResponse>(`${this.apiUrl}/users/banned`, { params });
}
}