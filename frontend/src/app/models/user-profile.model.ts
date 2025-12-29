
import { Post } from './post.model';
import { Page } from './page.model'; 

export interface UserProfile {
  id: number;
  name: string;
  username: string;
  posts: Page<Post>; 
  followerCount: number;
  followingCount: number;
  subscribed: boolean;
  enabled?: boolean;
}