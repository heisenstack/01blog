export interface HiddenPost {
  id: number;
  title: string;
  content: string;
  authorUsername: string;
  createdAt: string;
  hidden: boolean;
  hiddenAt?: string;
  hideReason?: string;
  mediaUrl?: string;
  mediaType?: 'IMAGE' | 'VIDEO';
}