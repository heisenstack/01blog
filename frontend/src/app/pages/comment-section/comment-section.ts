import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { CommentService } from '../../services/comment.service';
import { Comment } from '../../models/comment.model';
import { AuthService } from '../../services/auth.service';
import { Observable } from 'rxjs';
import { User } from '../../models/user.model';
import { ConfirmationModalComponent } from '../../components/confirmation-modal/confirmation-modal.component';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-comment-section',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, ConfirmationModalComponent],
  templateUrl: './comment-section.html',
  styleUrls: ['./comment-section.scss'],
})
export class CommentSectionComponent implements OnInit {
  @Input() postId!: number;
  comments: Comment[] = [];
  newCommentContent: string = '';

  isLoading = true;
  isSubmitting = false;
  isLastPage = false;

  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalComments = 0;
  isLoadingMore = false;

  isLoggedIn$: Observable<boolean>;
  currentUser: User | null = null;

  editingCommentId: number | null = null;
  editingCommentContent: string = '';
  isDeleteModalOpen = false;
  commentToDelete: number | null = null;

  constructor(
    private commentService: CommentService,
    private authService: AuthService,
    private toastr: ToastrService
  ) {
    this.isLoggedIn$ = this.authService.isLoggedIn$;
  }

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();

    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;
    });

    if (this.postId) {
      this.loadComments();
    }
  }

  loadComments(): void {
    this.isLoading = true;

    this.commentService.getComments(this.postId, this.currentPage, this.pageSize).subscribe({
      next: (response) => {

        this.comments = response.content || [];
        this.totalPages = response.totalPages || 0;
        this.totalComments = response.totalElements || 0;
        this.isLastPage = response.last || false;
        this.isLoading = false;
      },
      error: (err: any) => {
        this.toastr.error(err.error.message, "Error loading comments.")
        this.comments = [];
        this.isLoading = false;
      },
    });
  }

  loadMoreComments(): void {
    if (this.isLoadingMore || this.currentPage >= this.totalPages - 1) {
      return;
    }

    this.isLoadingMore = true;
    this.currentPage++;

    this.commentService.getComments(this.postId, this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        this.comments = [...this.comments, ...(response.content || [])];
        this.isLastPage = response.last || false;
        this.isLoadingMore = false;
      },
      error: (err: any) => {
        this.toastr.error(err.error.message, "Error loading more comments.")
        this.isLoadingMore = false;
      },
    });
  }

  onCommentSubmit(): void {
    if (!this.newCommentContent.trim() || this.isSubmitting) return;
    this.isSubmitting = true;

    this.commentService.addComment(this.postId, this.newCommentContent).subscribe({
      next: (response) => {
        this.toastr.success('Your comment has been posted.', 'Comment Added');
        this.newCommentContent = '';
        this.isSubmitting = false;
        this.currentPage = 0;
        this.loadComments();
      },
      error: (err: any) => {
        const errorMessage = err.error?.message || 'Failed to post comment. Please try again.';
        this.toastr.error(errorMessage, 'Error');
        this.isSubmitting = false;
      },
    });
  }

  onEditComment(comment: Comment): void {
    this.editingCommentId = comment.id;
    this.editingCommentContent = comment.content;
  }

  onCancelEdit(): void {
    this.editingCommentId = null;
    this.editingCommentContent = '';
  }

  onSaveEdit(commentId: number): void {
    if (!this.editingCommentContent.trim()) return;

    this.commentService.updateComment(commentId, this.editingCommentContent).subscribe({
      next: (updatedComment: Comment) => {
        const index = this.comments.findIndex((c) => c.id === commentId);
        if (index !== -1) {
          this.comments[index] = updatedComment;
        }
        this.toastr.success('Your comment has been updated.', 'Comment Edited');
        this.editingCommentId = null;
        this.editingCommentContent = '';
      },
      error: (err: any) => {
        const errorMessage = err.error?.message || 'Failed to update comment. Please try again.';
        this.toastr.error(errorMessage, 'Error');
      },
    });
  }

onDeleteComment(commentId: number): void {
  this.commentToDelete = commentId;
  this.isDeleteModalOpen = true;
}
handleDeleteConfirm(): void {
  if (this.commentToDelete === null) return;

  this.commentService.deleteComment(this.commentToDelete).subscribe({
    next: (resp) => {
      this.toastr.success('Your comment has been deleted.', 'Comment Deleted');
      this.loadComments();
      this.closeDeleteModal();
    },
    error: (err: any) => {
      const errorMessage = err.error?.message || 'Failed to delete comment. Please try again.';
      this.toastr.error(errorMessage, 'Error');
      this.closeDeleteModal();
    }
  });
}

closeDeleteModal(): void {
  this.isDeleteModalOpen = false;
  this.commentToDelete = null;
}
}
