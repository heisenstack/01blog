import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { NotificationService } from '../../services/notification.service';
import { PostService } from '../../services/post.service';
import { Notification } from '../../models/notification.model';
import { Subscription } from 'rxjs';
import { CommonModule } from '@angular/common';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification-bell.component.html',
  styleUrls: ['./notification-bell.component.scss']
})
export class NotificationBellComponent implements OnInit, OnDestroy {
  notifications: Notification[] = [];
  isDropdownOpen = false;
  unreadCount: number = 0;
  private countsSubscription?: Subscription;

  constructor(
    private notificationService: NotificationService,
    private postService: PostService,
    private router: Router,
    private toastr: ToastrService
  ) { }

  ngOnInit(): void {
    this.setupNotificationCountsSubscription();
  }

  ngOnDestroy(): void {
    if (this.countsSubscription) {
      this.countsSubscription.unsubscribe();
    }
  }


  setupNotificationCountsSubscription(): void {
    this.countsSubscription = this.notificationService.counts$.subscribe(counts => {
      this.unreadCount = counts.unreadCount;
    });
    
    this.notificationService.refreshNotificationCounts();
  }

  loadUnreadNotifications(): void {
    this.notificationService.getUnreadNotifications().subscribe({
      next: (notifications) => {
        this.notifications = notifications.slice(0, 10);
      },
      error: (error) => {
        this.toastr.error(error.error.message, "Error loading notifications.")
      }
    });
  }

  toggleDropdown(): void {
    this.isDropdownOpen = !this.isDropdownOpen;
    
    if (this.isDropdownOpen) {
      this.loadUnreadNotifications();
    }
  }


  markAsRead(notification: Notification, event: MouseEvent): void {
    event.stopPropagation();
    
    if (notification.postId) {
      this.postService.getPost(notification.postId).subscribe({
        next: () => {
          this.markAndNavigate(notification, `/post/${notification.postId}`);
        },
        error: () => {
          this.markNotificationAsRead(notification);
          this.toastr.warning(
            'This post is no longer available.',
            'Post Unavailable',
            { timeOut: 3000, progressBar: true }
          );
          this.isDropdownOpen = false;
        }
      });
    } else {
      this.markAndNavigate(notification, this.getNavigationPath(notification));
    }
  }


  private markAndNavigate(notification: Notification, path: string): void {
    this.notificationService.markAsRead(notification.id).subscribe({
      next: () => {
        this.notifications = this.notifications.filter(n => n.id !== notification.id);
        this.isDropdownOpen = false;
        this.router.navigate([path]);
      },
      error: (error) => {
        this.toastr.error(error.error.message, "Error reading notification.")
        this.notifications = this.notifications.filter(n => n.id !== notification.id);
        this.isDropdownOpen = false;
        this.router.navigate([path]);
      }
    });
  }


  private markNotificationAsRead(notification: Notification): void {
    this.notificationService.markAsRead(notification.id).subscribe({
      next: () => {
        this.notifications = this.notifications.filter(n => n.id !== notification.id);
      },
      error: () => {
        this.notifications = this.notifications.filter(n => n.id !== notification.id);
      }
    });
  }


  private getNavigationPath(notification: Notification): string {
    switch (notification.type) {
      case 'NEW_FOLLOWER':
        return `/profile/${notification.senderUsername}`;
      case 'NEW_COMMENT':
      case 'NEW_LIKE':
      case 'NEW_POST':
        if (notification.postId) {
          return `/post/${notification.postId}`;
        }
        return '/';
      default:
        return '/';
    }
  }


  markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.notifications = [];
        this.toastr.success('All notifications marked as read');
      },
      error: () => {
        this.toastr.error('Failed to mark all as read');
      }
    });
  }


  getNotificationMessage(message: string): string {
    const parts = message.split(' ');
    if (parts.length > 1) {
      return parts.slice(1).join(' ');
    }
    return message;
  }
}