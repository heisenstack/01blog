import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { NotificationService } from '../../services/notification.service';
import { PostService } from '../../services/post.service';
import { Notification, NotificationCounts } from '../../models/notification.model';
import { ToastrService } from 'ngx-toastr';
import { Subscription } from 'rxjs';

type TabType = 'all' | 'unread' | 'read';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.scss']
})
export class NotificationsComponent implements OnInit, OnDestroy {
  notifications: Notification[] = [];
  activeTab: TabType = 'all';
  isLoading = true;
  isLoadingMore = false;
  selectedNotifications = new Set<number>();
  selectAllChecked = false;
  
  // Pagination
  currentPage = 0;
  pageSize = 10;
  hasMorePages = false;
  
  // Counts
  counts: NotificationCounts = {
    totalCount: 0,
    unreadCount: 0,
    readCount: 0
  };

  private countsSubscription?: Subscription;

  constructor(
    private notificationService: NotificationService,
    private postService: PostService,
    private router: Router,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.loadCounts();
    this.loadNotifications();
    this.subscribeToCountsChanges();
  }

  ngOnDestroy(): void {
    if (this.countsSubscription) {
      this.countsSubscription.unsubscribe();
    }
  }


  subscribeToCountsChanges(): void {
    this.countsSubscription = this.notificationService.counts$.subscribe({
      next: (counts) => {
        console.log('Counts updated:', counts);
        this.counts = counts;
      }
    });
  }

  loadCounts(): void {
    this.notificationService.getNotificationCounts().subscribe({
      next: (counts) => {
        this.counts = counts;
        console.log('Initial counts loaded:', counts);
      },
      error: () => {
        // console.error('Failed to load notification counts');
      }
    });
  }

  loadNotifications(reset: boolean = true): void {
    if (reset) {
      this.currentPage = 0;
      this.notifications = [];
      this.isLoading = true;
      this.selectedNotifications.clear();
      this.selectAllChecked = false;
    } else {
      this.isLoadingMore = true;
    }

    this.notificationService.getNotificationsPaginated(this.activeTab, this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        console.log("Notifications: ", response);
        
        if (reset) {
          this.notifications = response.content;
        } else {
          this.notifications = [...this.notifications, ...response.content];
        }
        
        this.hasMorePages = !response.last;
        this.isLoading = false;
        this.isLoadingMore = false;
      },
      error: () => {
        this.toastr.error('Failed to load notifications');
        this.isLoading = false;
        this.isLoadingMore = false;
      }
    });
  }

  setActiveTab(tab: TabType): void {
    this.activeTab = tab;
    this.selectedNotifications.clear();
    this.selectAllChecked = false;
    this.loadNotifications(true);
  }

  loadMoreNotifications(): void {
    if (!this.isLoadingMore && this.hasMorePages) {
      this.currentPage++;
      this.loadNotifications(false);
    }
  }

  toggleNotificationRead(notification: Notification, event: MouseEvent): void {
    event.stopPropagation();
    
    // Only allow marking unread as read
    if (!notification.read) {
      this.notificationService.markAsRead(notification.id).subscribe({
        next: (counts) => {
          notification.read = true;
          this.toastr.success('Marked as read');
          console.log('Updated counts after mark as read:', counts);
          
          // If we're on unread tab, remove from list
          if (this.activeTab === 'unread') {
            this.notifications = this.notifications.filter(n => n.id !== notification.id);
          }
        },
        error: (error) => {
          this.toastr.error(error.error.message);
        }
      });
    }
  }

  handleNotificationClick(notification: Notification): void {
    // Mark as read if unread
    if (!notification.read) {
      this.notificationService.markAsRead(notification.id).subscribe({
        next: (counts) => {
          notification.read = true;
          console.log('Updated counts after click:', counts);
          
          // Remove from unread tab
          if (this.activeTab === 'unread') {
            this.notifications = this.notifications.filter(n => n.id !== notification.id);
          }
          
          this.navigateToDestination(notification);
        },
        error: () => {
          this.navigateToDestination(notification);
        }
      });
    } else {
      this.navigateToDestination(notification);
    }
  }

  private navigateToDestination(notification: Notification): void {
    if (notification.postId) {
      this.postService.getPost(notification.postId).subscribe({
        next: () => {
          this.router.navigate(['/post', notification.postId]);
        },
        error: () => {
          this.toastr.warning(
            'This post is no longer available.',
            'Post Unavailable',
            { timeOut: 3000, progressBar: true }
          );
        }
      });
    } else {
      switch (notification.type) {
        case 'NEW_FOLLOWER':
          this.router.navigate(['/profile', notification.senderUsername]);
          break;
        default:
          break;
      }
    }
  }

  toggleSelectNotification(notificationId: number): void {
    if (this.selectedNotifications.has(notificationId)) {
      this.selectedNotifications.delete(notificationId);
    } else {
      this.selectedNotifications.add(notificationId);
    }
    this.updateSelectAllState();
  }

  toggleSelectAll(): void {
    this.selectAllChecked = !this.selectAllChecked;
    
    if (this.selectAllChecked) {
      this.notifications.forEach(n => this.selectedNotifications.add(n.id));
    } else {
      this.selectedNotifications.clear();
    }
  }

  updateSelectAllState(): void {
    this.selectAllChecked = 
      this.notifications.length > 0 &&
      this.notifications.every(n => this.selectedNotifications.has(n.id));
  }

  markSelectedAsRead(): void {
    if (this.selectedNotifications.size === 0) {
      this.toastr.info('No notifications selected');
      return;
    }

    const unreadSelected = Array.from(this.selectedNotifications)
      .filter(id => {
        const notif = this.notifications.find(n => n.id === id);
        return notif && !notif.read;
      });

    if (unreadSelected.length === 0) {
      this.toastr.info('Selected notifications are already read');
      return;
    }

    // Mark each as read
    let completed = 0;
    const total = unreadSelected.length;
    
    unreadSelected.forEach(id => {
      this.notificationService.markAsRead(id).subscribe({
        next: (counts) => {
          const notif = this.notifications.find(n => n.id === id);
          if (notif) notif.read = true;
          completed++;
          
          if (completed === total) {
            this.selectedNotifications.clear();
            this.selectAllChecked = false;
            console.log('Final counts after bulk mark as read:', counts);
            
            // If on unread tab, remove marked items
            if (this.activeTab === 'unread') {
              this.notifications = this.notifications.filter(n => !unreadSelected.includes(n.id));
            }
            
            this.toastr.success(`${completed} notification${completed > 1 ? 's' : ''} marked as read`);
          }
        },
        error: () => {
          completed++;
          if (completed === total) {
            this.toastr.warning('Some notifications could not be marked as read');
          }
        }
      });
    });
  }

  deleteSelected(): void {
    if (this.selectedNotifications.size === 0) {
      this.toastr.info('No notifications selected');
      return;
    }

    const idsToDelete = Array.from(this.selectedNotifications);
    
    this.notificationService.deleteNotifications(idsToDelete).subscribe({
      next: (counts) => {
        this.notifications = this.notifications.filter(
          n => !this.selectedNotifications.has(n.id)
        );
        this.selectedNotifications.clear();
        this.selectAllChecked = false;
        console.log('Updated counts after delete:', counts);
        this.toastr.success(`${idsToDelete.length} notification${idsToDelete.length > 1 ? 's' : ''} deleted`);
      },
      error: () => {
        this.toastr.error('Failed to delete notifications');
      }
    });
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe({
      next: (counts) => {
        this.notifications.forEach(n => n.read = true);
        console.log('Updated counts after mark all as read:', counts);
        
        // If on unread tab, clear the list
        if (this.activeTab === 'unread') {
          this.notifications = [];
        }
        
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

  getNotificationIcon(type: string): string {
    switch (type) {
      case 'NEW_FOLLOWER':
        return 'fa-user-plus';
      case 'NEW_LIKE':
        return 'fa-heart';
      case 'NEW_COMMENT':
        return 'fa-comment';
      case 'NEW_POST':
        return 'fa-file-lines';
      default:
        return 'fa-bell';
    }
  }

  getUnreadCount(): number {
    return this.counts.unreadCount;
  }

  getReadCount(): number {
    return this.counts.readCount;
  }

  getTotalCount(): number {
    return this.counts.totalCount;
  }
}