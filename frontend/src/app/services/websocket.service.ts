import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Client, StompSubscription, IMessage, IFrame } from '@stomp/stompjs';
// import SockJS from 'sockjs-client';
 import SockJS from 'sockjs-client'; 
import { Notification, NotificationCounts } from '../models/notification.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private client: Client | null = null;
  private connected$ = new BehaviorSubject<boolean>(false);
  
  // Subjects for real-time updates
  private newNotificationSubject = new BehaviorSubject<Notification | null>(null);
  private notificationCountsSubject = new BehaviorSubject<NotificationCounts | null>(null);
  private notificationReadSubject = new BehaviorSubject<number | null>(null);
  private notificationsDeletedSubject = new BehaviorSubject<number[] | null>(null);

  // Observables for components to subscribe
  public newNotification$ = this.newNotificationSubject.asObservable();
  public notificationCounts$ = this.notificationCountsSubject.asObservable();
  public notificationRead$ = this.notificationReadSubject.asObservable();
  public notificationsDeleted$ = this.notificationsDeletedSubject.asObservable();
  public connected = this.connected$.asObservable();

  private subscriptions: StompSubscription[] = [];

  constructor(private authService: AuthService) {}

  connect(): void {
    if (this.client && this.client.connected) {
      console.log('WebSocket already connected');
      return;
    }

    const token = this.authService.getToken();
    if (!token) {
      console.error('No authentication token found');
      return;
    }

    this.client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws') as any,
      connectHeaders: {
        Authorization: `Bearer ${token}`
      },
      debug: (str) => {
        console.log('STOMP Debug:', str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.client.onConnect = () => {
      console.log('WebSocket connected successfully');
      this.connected$.next(true);
      this.subscribeToNotifications();
    };

    this.client.onDisconnect = () => {
      console.log('WebSocket disconnected');
      this.connected$.next(false);
    };

    this.client.onStompError = (frame) => {
      console.error('WebSocket error:', frame);
      this.connected$.next(false);
    };

    this.client.activate();
  }


  private subscribeToNotifications(): void {
    if (!this.client) return;

    // Subscribe to new notifications
    const notificationSub = this.client.subscribe('/user/queue/notifications', (message) => {
      const notification: Notification = JSON.parse(message.body);
      console.log('Received new notification:', notification);
      this.newNotificationSubject.next(notification);
    });
    this.subscriptions.push(notificationSub);

    // Subscribe to notification counts updates
    const countsSub = this.client.subscribe('/user/queue/notification-counts', (message) => {
      const counts: NotificationCounts = JSON.parse(message.body);
      console.log('Received notification counts:', counts);
      this.notificationCountsSubject.next(counts);
    });
    this.subscriptions.push(countsSub);

    // Subscribe to notification read events
    const readSub = this.client.subscribe('/user/queue/notification-read', (message) => {
      const notificationId: number = JSON.parse(message.body);
      console.log('Notification marked as read:', notificationId);
      this.notificationReadSubject.next(notificationId);
    });
    this.subscriptions.push(readSub);

    // Subscribe to notification deleted events
    const deletedSub = this.client.subscribe('/user/queue/notifications-deleted', (message) => {
      const notificationIds: number[] = JSON.parse(message.body);
      console.log('Notifications deleted:', notificationIds);
      this.notificationsDeletedSubject.next(notificationIds);
    });
    this.subscriptions.push(deletedSub);
  }

  disconnect(): void {
    if (this.client) {
      // Unsubscribe from all subscriptions
      this.subscriptions.forEach(sub => sub.unsubscribe());
      this.subscriptions = [];

      this.client.deactivate();
      this.client = null;
      this.connected$.next(false);
      console.log('WebSocket disconnected');
    }
  }


  isConnected(): boolean {
    return this.client?.connected ?? false;
  }
}