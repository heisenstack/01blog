import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { PostService } from '../../services/post.service';
import { Post } from '../../models/post.model';
import { PostCardComponent } from '../../components/post-card/post-card';
import { Page } from '../../models/page.model'; // <-- IMPORT Page

@Component({
  selector: 'app-feed',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    PostCardComponent
  ],
  templateUrl: './feed.html',
  styleUrls: ['./feed.scss']
})
export class FeedComponent implements OnInit {
  
  posts: Post[] = [];
  isLoading: boolean = true;
  error: string | null = null;

  // --- NEW PAGINATION STATE ---
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  isLoadingMore = false;
  // --------------------------

  constructor(private postService: PostService) {}

  ngOnInit(): void {
    this.loadFeed();
  }

  loadFeed(): void {
    this.isLoading = true;
    this.postService.getFeed(this.currentPage, this.pageSize).subscribe({
      next: (data: Page<Post>) => {
        
        this.posts = data.content;
        // console.log("Pooooost:", this.posts.length);
        this.totalPages = data.totalPages;
        this.isLoading = false;
      },
      error: (err: any) => {
        // console.error('Failed to fetch personalized feed:', err);
        this.error = 'Could not load your feed. Please try again later.';
        this.isLoading = false;
      }
    });
  }

  loadMorePosts(): void {
    if (this.isLoadingMore || this.currentPage >= this.totalPages - 1) {
      return;
    }

    this.isLoadingMore = true;
    this.currentPage++;

    this.postService.getFeed(this.currentPage, this.pageSize).subscribe({
      next: (data: Page<Post>) => {
        this.posts = [...this.posts, ...data.content];
        this.isLoadingMore = false;
      },
      error: (err: any) => {
        // console.error('Failed to fetch more of the feed:', err);
        this.isLoadingMore = false;
      }
    });
  }

  onPostUpdated(updatedPost: Post): void {
    const index = this.posts.findIndex(p => p.id === updatedPost.id);
    if (index !== -1) {
      const newPosts = [...this.posts];
      newPosts[index] = updatedPost;
      this.posts = newPosts;
    }
  }
}