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
  styleUrls: ['./post-card.scss'],
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
          likedByCurrentUser: response.likedByCurrentUser,
        };
        this.post = updatedPost;
      },
      error: (error) => {
        this.toastr.error(error.error.message, "Error liking post");
      }
    });
  }
}
