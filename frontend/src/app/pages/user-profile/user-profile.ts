import { Component, OnInit, OnDestroy, ElementRef, HostListener } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService, UserReportRequest } from '../../services/user.service';
import { UserProfile } from '../../models/user-profile.model';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
import { ProfileHeaderComponent } from '../../components/profile-header/profile-header';
import { PostCardComponent } from '../../components/post-card/post-card';
import { Post } from '../../models/post.model';
import { ToastrService } from 'ngx-toastr';
import { Subscription } from 'rxjs';
import { ReportModalComponent, ReportPayload } from '../../components/report-modal/report-modal';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.html',
  styleUrls: ['./user-profile.scss'],
  standalone: true,
  imports: [CommonModule, ProfileHeaderComponent, PostCardComponent, ReportModalComponent],
})
export class UserProfileComponent implements OnInit, OnDestroy {
  userProfile: UserProfile | null = null;
  username: string = '';
  name: string = '';
  isOwnProfile: boolean = false;
  isLoggedIn: boolean = false;
  isLoading: boolean = true;
  isActionsMenuOpen = false;
  isReportModalOpen = false;

  currentPage = 0;
  pageSize = 10;
  isLoadingMore = false;
  posts: Post[] = [];
  isSubscribing = false;

  private routeSub: Subscription | undefined;

  constructor(
    private route: ActivatedRoute,
    private userService: UserService,
    private authService: AuthService,
    private router: Router,
    private toastr: ToastrService,
    private elementRef: ElementRef
  ) {}

  ngOnInit(): void {
    this.isLoggedIn = this.authService.isAuthenticated();

    this.routeSub = this.route.params.subscribe((params) => {
      this.username = params['username'];
      this.resetState();
      this.loadUserProfile();
      this.checkIfOwnProfile();
    });
  }

  ngOnDestroy(): void {
    if (this.routeSub) {
      this.routeSub.unsubscribe();
    }
  }

  resetState(): void {
    this.userProfile = null;
    this.posts = [];
    this.currentPage = 0;
    this.isLoading = true;
    this.isActionsMenuOpen = false;
  }

  loadUserProfile(): void {
    this.isLoading = true;
    this.userService.getUserProfile(this.username, this.currentPage, this.pageSize).subscribe({
      next: (profile) => {
        console.log(profile);

        this.userProfile = profile;
        this.posts = profile.posts?.content || [];
        this.isLoading = false;
      },
      error: (err) => {
        // console.error('Error loading user profile', err);
        this.isLoading = false;
        this.toastr.error('Could not load user profile.', 'Error');
        this.router.navigate(['/']);
      },
    });
  }

  loadMorePosts(): void {
    if (!this.userProfile || this.isLoadingMore || this.userProfile.posts.last) {
      return;
    }
    this.isLoadingMore = true;
    this.currentPage++;
    this.userService.getUserProfile(this.username, this.currentPage, this.pageSize).subscribe({
      next: (profile) => {
        console.log('Profile: ', profile);

        this.posts.push(...profile.posts.content);
        this.userProfile!.posts = profile.posts;
        this.isLoadingMore = false;
      },
      error: (err) => {
        // console.error('Error loading more posts', err);
        this.isLoadingMore = false;
      },
    });
  }

  checkIfOwnProfile(): void {
    const currentUser = this.authService.getCurrentUser();
    this.isOwnProfile = currentUser ? currentUser.username === this.username : false;
  }

  toggleSubscription(): void {
    if (!this.userProfile || this.isSubscribing) return;
    this.isSubscribing = true;
    if (!this.userProfile) return;
    const isSubscribed = this.userProfile.subscribed;
    const action = isSubscribed
      ? this.userService.unsubscribe(this.userProfile.username)
      : this.userService.subscribe(this.userProfile.username);
    action.subscribe({
      next: () => {
        if (this.userProfile) {
          const message = isSubscribed
            ? `You have successfully unsubscribed from ${this.userProfile.username}`
            : `You have successfully subscribed to ${this.userProfile.username}`;
          this.toastr.success(message, 'Subscription');

          this.userProfile.subscribed = !isSubscribed;
          isSubscribed ? this.userProfile.followerCount-- : this.userProfile.followerCount++;
        }
        this.isSubscribing = false;
      },
      error: (err) => {
        this.toastr.error('Something went wrong. Please try again.', 'Error');
        this.isSubscribing = false;
        // console.error('Subscription error:', err);
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

  handleUserReportSubmit(payload: ReportPayload): void {
    if (!this.userProfile) return;

    const reportData: UserReportRequest = {
      reason: payload.reason,
      details: payload.details,
    };

    this.userService.reportUser(this.userProfile.username, reportData).subscribe({
      next: (response) => {
        console.log(response);

        this.toastr.success(
          'User has been reported. Our moderation team will review it.',
          'Report Submitted'
        );
        this.isReportModalOpen = false;
      },
      error: (err) => {
        console.log('Failed to report user:', err);
        const errorMessage = err.error?.message || 'An unknown error occurred. Please try again.';
        this.toastr.error(errorMessage, 'Error');
      },
    });
  }
}
