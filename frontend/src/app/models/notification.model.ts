export interface Notification {
  id: number;
  message: string;
  read: boolean;
  type: 'NEW_FOLLOWER' | 'NEW_COMMENT' | 'NEW_LIKE' | 'NEW_POST';
  senderUsername: string;
  createdAt: string;
  postId: number;
}

export interface PagedNotifications {
  content: Notification[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number; 
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface NotificationCounts {
  totalCount: number;
  unreadCount: number;
  readCount: number;
}