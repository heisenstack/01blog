import { Component, OnInit, OnDestroy, ElementRef, HostListener } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { PostService, PostLikeResponse } from '../../services/post.service';
import { UserService, UserReportRequest } from '../../services/user.service';
import { Post } from '../../models/post.model';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
import { CommentSectionComponent } from '../comment-section/comment-section';
import { MediaCarouselComponent } from '../../components/media-carousel/media-carousel.component';
import { Observable } from 'rxjs';
import { ToastrService } from 'ngx-toastr';
import { FormsModule } from '@angular/forms';
import { ConfirmationModalComponent } from '../../components/confirmation-modal/confirmation-modal.component';
import { ReportModalComponent, ReportPayload } from '../../components/report-modal/report-modal';

@Component({
  selector: 'app-post-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    CommentSectionComponent,
    MediaCarouselComponent,
    FormsModule,
    ConfirmationModalComponent,
    ReportModalComponent,
  ],
  templateUrl: './post-detail.html',
  styleUrls: ['./post-detail.scss'],
})
export class PostDetailComponent implements OnInit, OnDestroy {
  post: Post | null = null;
  isLoggedIn$: Observable<boolean>;
  currentUsername: string | null = null;
  errorMessage: string | null = null;

  // Modal & Menu States
  isDeleteModalOpen = false;
  isPostReportModalOpen = false;
  isUserReportModalOpen = false;
  isActionsMenuOpen = false;

  // Properties for Delete Modal
  modalTitle = '';
  modalMessage = '';
  confirmButtonText = 'Confirm';
  confirmButtonClass = 'btn-danger';
  private confirmAction: (() => void) | null = null;

  constructor(
    private route: ActivatedRoute,
    private postService: PostService,
    private authService: AuthService,
    private userService: UserService,
    private router: Router,
    private toastr: ToastrService,
    private elementRef: ElementRef
  ) {
    this.isLoggedIn$ = this.authService.isLoggedIn$;
  }

  ngOnInit(): void {
    const postId = Number(this.route.snapshot.paramMap.get('id'));
    if (postId) {
      this.postService.getPost(postId).subscribe({
        next: (data: Post) => {
          console.log(data);
          
          this.post = data;
        },
        error: (err) => {
          this.handleError(err, 'loading post');
          this.errorMessage = 'Post not found, hidden or an error occurred.';
        },
      });
    }

    this.authService.currentUser$.subscribe((user) => {
      this.currentUsername = user ? user.username : null;
    });
  }

  ngOnDestroy(): void {}

  get isAuthor(): boolean {
    if (!this.post || !this.currentUsername) {
      return false;
    }
    return this.post.authorUsername === this.currentUsername;
  }

  toggleLike(): void {
    const currentPost = this.post;
    if (!currentPost) return;

    const action = currentPost.likedByCurrentUser
      ? this.postService.unlikePost(currentPost.id)
      : this.postService.likePost(currentPost.id);

    action.subscribe({
      next: (response: PostLikeResponse) => {
        console.log(response);

        this.post = {
          ...currentPost,
          likeCount: response.likeCount,
          likedByCurrentUser: response.likedByCurrentUser,
        };
      },
      error: (err) => {
        console.log(err);

        this.handleError(err, 'updating like');
      },
    });
  }

  onDeletePost(): void {
    this.modalTitle = 'Delete Post';
    this.modalMessage =
      'Are you sure you want to permanently delete this post? This action cannot be undone.';
    this.confirmButtonText = 'Yes, Delete Post';
    this.confirmButtonClass = 'btn-danger';
    this.confirmAction = () => this.executeDeletePost();
    this.isDeleteModalOpen = true;
  }

  private executeDeletePost(): void {
    if (!this.post) return;
    this.postService.deletePost(this.post.id).subscribe({
      next: (response) => {
        console.log(response);
        
        this.toastr.success('The post has been permanently deleted.', 'Post Deleted');
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.handleError(err, 'deleting post');
      },
    });
  }

  handleDeleteConfirm(): void {
    if (this.confirmAction) {
      this.confirmAction();
    }
    this.closeDeleteModal();
  }

  closeDeleteModal(): void {
    this.isDeleteModalOpen = false;
    this.confirmAction = null;
  }

  // Report Modal Handlers
  handlePostReportSubmit(payload: ReportPayload): void {
    if (!this.post) return;
    this.postService.reportPost(this.post.id, payload).subscribe({
      next: () => {
        this.toastr.success('Post has been reported for review.', 'Report Submitted');
        this.isPostReportModalOpen = false;
      },
      error: (err) => {
        this.handleError(err, 'reporting post');
      },
    });
  }

  handleUserReportSubmit(payload: ReportPayload): void {
    if (!this.post) return;
    const reportData: UserReportRequest = {
      reason: payload.reason,
      details: payload.details,
    };
    this.userService.reportUser(this.post.authorUsername, reportData).subscribe({
      next: () => {
        this.toastr.success(
          `User '${this.post?.authorUsername}' has been reported for review.`,
          'Report Submitted'
        );
        this.isUserReportModalOpen = false;
      },
      error: (err) => {
        this.handleError(err, 'reporting user');
      },
    });
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.isActionsMenuOpen = false;
    }
  }

  toggleActionsMenu(event: MouseEvent) {
    event.stopPropagation();
    this.isActionsMenuOpen = !this.isActionsMenuOpen;
  }

  private handleError(error: any, context: string): void {
    if (error.status === 429) {
      return; 
    }

    if (error.status === 404) {
      const message =
        error.error?.message ||
        `The ${context.includes('post') ? 'post' : 'resource'} was not found.`;
      this.toastr.error(message, 'Not Found');

      if (context.includes('loading post')) {
        setTimeout(() => this.router.navigate(['/']), 2000);
      }
      return;
    }

    // Handle authentication errors (401)
    if (error.status === 401) {
      this.toastr.error(`Please log in to perform this action.`, 'Authentication Required');
      return;
    }

    // Handle forbidden errors (403)
    if (error.status === 403) {
      const message = error.error?.message || 'You are not authorized to perform this action.';
      this.toastr.error(message, 'Forbidden');
      return;
    }

    // Handle conflict errors (409)
    if (error.status === 409) {
      const message = error.error?.message || 'This action conflicts with the current state.';
      this.toastr.warning(message, 'Conflict');
      return;
    }

    // Handle server errors (500+)
    if (error.status >= 500) {
      this.toastr.error('Server error. Please try again later.', 'Error');
      return;
    }

    // Handle network errors
    if (error.status === 0) {
      this.toastr.error(
        'Cannot connect to server. Please check your connection.',
        'Connection Error'
      );
      return;
    }

    // Default error message
    const message = error.error?.message || `Failed to ${context}.`;
    this.toastr.error(message, 'Error');
  }
}
