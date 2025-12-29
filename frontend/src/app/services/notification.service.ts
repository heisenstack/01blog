import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { Notification, PagedNotifications, NotificationCounts } from '../models/notification.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private apiUrl = 'http://localhost:8080/api/notifications';
  
  private countsSubject = new BehaviorSubject<NotificationCounts>({
    totalCount: 0,
    unreadCount: 0,
    readCount: 0
  });
  public counts$ = this.countsSubject.asObservable();

  constructor(private http: HttpClient) { }


  getUnreadNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(this.apiUrl);
  }


  getNotificationsPaginated(filter: 'all' | 'read' | 'unread', page: number, size: number = 10): Observable<PagedNotifications> {
    return this.http.get<PagedNotifications>(`${this.apiUrl}/paginated`, {
      params: {
        filter,
        page: page.toString(),
        size: size.toString()
      }
    });
  }


  getNotificationCounts(): Observable<NotificationCounts> {
    return new Observable(observer => {
      this.http.get<NotificationCounts>(`${this.apiUrl}/counts`).subscribe({
        next: (counts: NotificationCounts) => {
          this.countsSubject.next(counts);
          observer.next(counts);
          observer.complete();
        },
        error: (error) => {
          observer.error(error);
        }
      });
    });
  }


  public refreshNotificationCounts(): void {
    this.http.get<NotificationCounts>(`${this.apiUrl}/counts`).subscribe({
      next: (counts: NotificationCounts) => {
        this.countsSubject.next(counts);
      },
      error: (error) => {
        // console.error('Failed to refresh notification counts:', error);
      }
    });
  }

  markAsRead(notificationId: number): Observable<NotificationCounts> {
    return new Observable(observer => {
      this.http.post<NotificationCounts>(`${this.apiUrl}/${notificationId}/read`, {}).subscribe({
        next: (counts: NotificationCounts) => {
          this.countsSubject.next(counts);
          observer.next(counts);
          observer.complete();
        },
        error: (error) => {
          observer.error(error);
        }
      });
    });
  }


  markAsUnread(notificationId: number): Observable<NotificationCounts> {
    return new Observable(observer => {
      this.http.post<NotificationCounts>(`${this.apiUrl}/${notificationId}/unread`, {}).subscribe({
        next: (counts: NotificationCounts) => {
          this.countsSubject.next(counts);
          observer.next(counts);
          observer.complete();
        },
        error: (error) => {
          observer.error(error);
        }
      });
    });
  }


  markAllAsRead(): Observable<NotificationCounts> {
    return new Observable(observer => {
      this.http.post<NotificationCounts>(`${this.apiUrl}/read-all`, {}).subscribe({
        next: (counts: NotificationCounts) => {
          this.countsSubject.next(counts);
          observer.next(counts);
          observer.complete();
        },
        error: (error) => {
          observer.error(error);
        }
      });
    });
  }


  deleteNotification(notificationId: number): Observable<NotificationCounts> {
    return new Observable(observer => {
      this.http.delete<NotificationCounts>(`${this.apiUrl}/${notificationId}`).subscribe({
        next: (counts: NotificationCounts) => {
          this.countsSubject.next(counts);
          observer.next(counts);
          observer.complete();
        },
        error: (error) => {
          observer.error(error);
        }
      });
    });
  }


  deleteNotifications(notificationIds: number[]): Observable<NotificationCounts> {
    return new Observable(observer => {
      this.http.request<NotificationCounts>('DELETE', this.apiUrl, {
        body: notificationIds
      }).subscribe({
        next: (counts: NotificationCounts) => {
          
          this.countsSubject.next(counts);
          observer.next(counts);
          observer.complete();
        },
        error: (error) => {
          console.log(error);
          observer.error(error);
        }
      });
    });
  }


  getCurrentCounts(): NotificationCounts {
    return this.countsSubject.value;
  }
}