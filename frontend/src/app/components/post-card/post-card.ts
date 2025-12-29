import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { Post } from '../../models/post.model';
import { PostService, PostLikeResponse } from '../../services/post.service';
import { AuthService } from '../../services/auth.service';
import { Observable } from 'rxjs';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-post-card',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './post-card.html',
  styleUrls: ['./post-card.scss']
})
export class PostCardComponent {
  @Input() post!: Post;
  @Output() postUpdated = new EventEmitter<Post>();
  isLoggedIn$: Observable<boolean>;

  constructor(
    private postService: PostService,
    private authService: AuthService,
    private router: Router,
    private toastr: ToastrService
  ) {
    this.isLoggedIn$ = this.authService.isLoggedIn$;
  }

  navigateToPost(): void {
    this.router.navigate(['/post', this.post.id]);
  }

  toggleLike(event: MouseEvent): void {
    event.stopPropagation();
    const currentPost = this.post;
    if (!currentPost) return;

    const action = currentPost.likedByCurrentUser
      ? this.postService.unlikePost(currentPost.id)
      : this.postService.likePost(currentPost.id);

    action.subscribe({
      next: (response: PostLikeResponse) => {
        const updatedPost: Post = {
          ...currentPost,
          likeCount: response.likeCount,
          likedByCurrentUser: response.likedByCurrentUser
        };
        this.post = updatedPost;
        // this.postUpdated.emit(updatedPost);
      },
      error: (err) => {
        this.handleError(err);
      }
    });
  }

  private handleError(error: any): void {
    // Handle rate limiting (429)
    if (error.status === 429) {
      return; // Rate limit interceptor already handled this
    }

    // Handle not found errors (404) - post was deleted
    if (error.status === 404) {
      const message = error.error?.message || 'This post no longer exists.';
      this.toastr.warning(message, 'Post Not Found');
      // Optionally refresh the page or remove the post from view
      return;
    }

    // Handle authentication errors (401)
    if (error.status === 401) {
      this.toastr.error('Please log in to like posts.', 'Authentication Required');
      return;
    }

    // Default error message
    const message = error.error?.message || 'Could not update like status.';
    this.toastr.error(message, 'Error');
  }
}