export interface PostMedia {
  id: number;
  mediaUrl: string;
  mediaType: 'IMAGE' | 'VIDEO';
  displayOrder: number;
  createdAt: string;
}

export interface Post {
  id: number;
  title: string;
  content: string;
  authorUsername: string;
  authorId: number;
  createdAt: string;
  updatedAt?: string;
  likeCount: number;
  likedByCurrentUser: boolean;
  hidden?: boolean;
  
  mediaFiles?: PostMedia[];
  
  commentCount?: number;
  reportedCount: number;
}