import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, Subscription } from 'rxjs';
import { Notification, PagedNotifications, NotificationCounts } from '../models/notification.model';
import { ToastrService } from 'ngx-toastr';
import { WebSocketService } from './websocket.service';

@Injectable({
  providedIn: 'root'
})
export class NotificationService implements OnDestroy {
  private apiUrl = 'http://localhost:8080/api/notifications';
  
  private countsSubject = new BehaviorSubject<NotificationCounts>({
    totalCount: 0,
    unreadCount: 0,
    readCount: 0
  });
  public counts$ = this.countsSubject.asObservable();

  private subscriptions: Subscription[] = [];

  constructor(
    private http: HttpClient, 
    private toastr: ToastrService,
    private webSocketService: WebSocketService
  ) {
    this.initializeWebSocketListeners();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }


  private initializeWebSocketListeners(): void {
    // Listen for new notifications
    const newNotifSub = this.webSocketService.newNotification$.subscribe(notification => {
      if (notification) {
        console.log('New notification received via WebSocket:', notification);
        // this.toastr.info(notification.message, 'New Notification');
      }
    });
    this.subscriptions.push(newNotifSub);

    // Listen for count updates
    const countsSub = this.webSocketService.notificationCounts$.subscribe(counts => {
      if (counts) {
        console.log('Notification counts updated via WebSocket:', counts);
        this.countsSubject.next(counts);
      }
    });
    this.subscriptions.push(countsSub);
  }

  connectWebSocket(): void {
    this.webSocketService.connect();
  }


  disconnectWebSocket(): void {
    this.webSocketService.disconnect();
  }


  getNewNotificationStream(): Observable<Notification | null> {
    return this.webSocketService.newNotification$;
  }


  getNotificationReadStream(): Observable<number | null> {
    return this.webSocketService.notificationRead$;
  }

  getNotificationsDeletedStream(): Observable<number[] | null> {
    return this.webSocketService.notificationsDeleted$;
  }

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
        this.toastr.error("Failed to refresh notification count");
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
          this.toastr.error("Failed to read notification.");
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
          this.toastr.error("Failed to delete notification.");
          observer.error(error);
        }
      });
    });
  }

  getCurrentCounts(): NotificationCounts {
    return this.countsSubject.value;
  }
}