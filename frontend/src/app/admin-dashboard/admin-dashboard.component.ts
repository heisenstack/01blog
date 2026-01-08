import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AdminService } from '../admin.service';
import { PostService } from '../services/post.service';
import { DashboardStats } from '../models/dashboard-stats.model';
import { Report } from '../models/report.model';
import { UserAdminView } from '../models/user-admin-view.model';
import { UserReport } from '../models/user-report.model';
import { HiddenPost } from '../models/HiddenPost.model';
import { ConfirmationModalComponent } from '../components/confirmation-modal/confirmation-modal.component';
import { HidePostModalComponent } from '../components/hide-post-modal/hide-post-modal.component';
import { ToastrService } from 'ngx-toastr';
import { Post } from '../models/post.model';


@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ConfirmationModalComponent,
    HidePostModalComponent
  ]
})


export class AdminDashboardComponent implements OnInit {
  activeTab: 'dashboard' | 'reports' | 'reportedUsers' | 'users' | 'hiddenPosts' | 'posts' | 'bannedUsers' = 'dashboard' ;
  dashboardStats?: DashboardStats;
  isStatsLoading = true;
  statsError: string | null = null;

// banned users properties:
  bannedUsers: UserAdminView[] = [];
isBannedUsersLoading = false;
bannedUsersError: string | null = null;
bannedUsersCurrentPage = 0;
bannedUsersPageSize = 10;
bannedUsersTotalPages = 0;
bannedUsersTotalElements = 0;
isLoadingMoreBannedUsers = false;
  // Reports pagination
  reports: Report[] = [];
  isReportsLoading = false;
  reportsError: string | null = null;
  reportsCurrentPage = 0;
  reportsPageSize = 10;
  reportsTotalPages = 0;
  reportsTotalElements = 0;
  isLoadingMoreReports = false;

  // Posts pagination
  posts: Post[] = [];
  isPostsLoading = false;
  postsError: string | null = null;
  postsCurrentPage = 0;
  postsPageSize = 10;
  postsTotalPages = 0;
  postsTotalElements = 0;
  isLoadingMorePosts = false;

  // Reported Users pagination
  reportedUsers: UserReport[] = [];
  isReportedUsersLoading = false;
  reportedUsersError: string | null = null;
  reportedUsersCurrentPage = 0;
  reportedUsersPageSize = 10;
  reportedUsersTotalPages = 0;
  reportedUsersTotalElements = 0;
  isLoadingMoreReportedUsers = false;

  // Users pagination
  users: UserAdminView[] = [];
  isUsersLoading = false;
  usersError: string | null = null;
  usersCurrentPage = 0;
  usersPageSize = 10;
  usersTotalPages = 0;
  usersTotalElements = 0;
  isLoadingMoreUsers = false;

  // Hidden Posts pagination
  hiddenPosts: HiddenPost[] = [];
  isHiddenPostsLoading = false;
  hiddenPostsError: string | null = null;
  hiddenPostsCurrentPage = 0;
  hiddenPostsPageSize = 10;
  hiddenPostsTotalPages = 0;
  hiddenPostsTotalElements = 0;
  isLoadingMoreHiddenPosts = false;

  // Hide Post Modal
  isHidePostModalOpen = false;
  isHidingPost = false;
  hidePostTitle = '';
  private postIdToHide: number | null = null;

  // Confirmation Modal
  isModalOpen = false;
  modalTitle = '';
  modalMessage = '';
  private confirmAction: (() => void) | null = null;

  constructor(private adminService: AdminService, private toastr: ToastrService, private postService: PostService) { }

  ngOnInit(): void {
    this.loadDashboardStats();
  }

  selectTab(tab: 'dashboard' | 'reports' | 'reportedUsers' | 'users' | 'posts' | 'hiddenPosts' | 'bannedUsers'): void {
    this.activeTab = tab;
    if (tab === 'dashboard' && !this.dashboardStats) this.loadDashboardStats();
    if (tab === 'reports' && this.reports.length === 0) this.loadReports();
    if (tab === 'reportedUsers' && this.reportedUsers.length === 0) this.loadReportedUsers();
    if (tab === 'users' && this.users.length === 0) this.loadUsers();
    if (tab === 'hiddenPosts' && this.hiddenPosts.length === 0) this.loadHiddenPosts();
    if (tab === 'posts' && this.posts.length === 0) this.loadPosts();
     if (tab === 'bannedUsers' && this.bannedUsers.length === 0) this.loadBannedUsers();
  }

  // ===== DASHBOARD STATS =====
  loadDashboardStats(): void {
    this.isStatsLoading = true;
    this.statsError = null;
    this.adminService.getDashboardStats().subscribe({
      next: data => { this.dashboardStats = data; this.isStatsLoading = false; 

        console.log("Stats: ", data);
      },
      
      error: () => { this.statsError = 'Failed to load dashboard statistics.'; this.isStatsLoading = false; }
    });
  }

  // ===== POSTS =====
  loadPosts(): void {
    this.isPostsLoading = true;
    this.postsError = null;
    this.postService.getPostsforAdmin(this.postsCurrentPage, this.postsPageSize).subscribe({
      next: response => {
        console.log([response]);
        
        this.posts = response.content || [];
        // console.log([this.posts]);
        this.postsTotalPages = response.totalPages || 0;
        this.postsTotalElements = response.totalElements || 0;
        this.isPostsLoading = false;
      },
      error: () => {
        this.postsError = 'Failed to load posts.';
        this.posts = [];
        this.isPostsLoading = false;
      }
    });
  }

  loadMorePosts(): void {
    if (this.isLoadingMorePosts || this.postsCurrentPage >= this.postsTotalPages - 1) {
      return;
    }
    this.isLoadingMorePosts = true;
    this.postsCurrentPage++;
    this.postService.getPosts(this.postsCurrentPage, this.postsPageSize).subscribe({
      next: response => {
        this.posts = [...this.posts, ...(response.content || [])];
        this.isLoadingMorePosts = false;
      },
      error: () => {
        this.toastr.error('Failed to load more posts.');
        this.isLoadingMorePosts = false;
      }
    });
  }

  // ===== REPORTS =====
  loadReports(): void {
    this.isReportsLoading = true;
    this.reportsError = null;
    this.adminService.getReportedPosts(this.reportsCurrentPage, this.reportsPageSize).subscribe({
      next: response => {
        console.log("Reported Posts: ", response);
        
        this.reports = response.content || [];
        this.reportsTotalPages = response.totalPages || 0;
        this.reportsTotalElements = response.totalElements || 0;
        this.isReportsLoading = false;
      },
      error: () => {
        this.reportsError = 'Failed to load reported posts.';
        this.reports = [];
        this.isReportsLoading = false;
      }
    });
  }

  loadMoreReports(): void {
    if (this.isLoadingMoreReports || this.reportsCurrentPage >= this.reportsTotalPages - 1) {
      return;
    }
    this.isLoadingMoreReports = true;
    this.reportsCurrentPage++;
    this.adminService.getReportedPosts(this.reportsCurrentPage, this.reportsPageSize).subscribe({
      next: response => {
        this.reports = [...this.reports, ...(response.content || [])];
        this.isLoadingMoreReports = false;
      },
      error: () => {
        this.toastr.error('Failed to load more reports.');
        this.isLoadingMoreReports = false;
      }
    });
  }

  // ===== REPORTED USERS =====
  loadReportedUsers(): void {
    this.isReportedUsersLoading = true;
    this.reportedUsersError = null;
    this.adminService.getReportedUsers(this.reportedUsersCurrentPage, this.reportedUsersPageSize).subscribe({
      next: response => {
        console.log("Reported Users: ", response);
        
        this.reportedUsers = response.content || [];
        this.reportedUsersTotalPages = response.totalPages || 0;
        this.reportedUsersTotalElements = response.totalElements || 0;
        this.isReportedUsersLoading = false;
      },
      error: () => {
        this.reportedUsersError = 'Failed to load reported accounts.';
        this.reportedUsers = [];
        this.isReportedUsersLoading = false;
      }
    });
  }

  loadMoreReportedUsers(): void {
    if (this.isLoadingMoreReportedUsers || this.reportedUsersCurrentPage >= this.reportedUsersTotalPages - 1) {
      return;
    }
    this.isLoadingMoreReportedUsers = true;
    this.reportedUsersCurrentPage++;
    this.adminService.getReportedUsers(this.reportedUsersCurrentPage, this.reportedUsersPageSize).subscribe({
      next: response => {
        this.reportedUsers = [...this.reportedUsers, ...(response.content || [])];
        this.isLoadingMoreReportedUsers = false;
      },
      error: () => {
        this.toastr.error('Failed to load more reported users.');
        this.isLoadingMoreReportedUsers = false;
      }
    });
  }

  // ===== USERS =====
  loadUsers(): void {
    this.isUsersLoading = true;
    this.usersError = null;
    this.adminService.getAllUsers(this.usersCurrentPage, this.usersPageSize).subscribe({
      next: response => {
        console.log(response);
        
        this.users = response.content || [];
        this.usersTotalPages = response.totalPages || 0;
        this.usersTotalElements = response.totalElements || 0;
        this.isUsersLoading = false;
        console.log(this.users);
      },
      error: () => {
        this.usersError = 'Failed to load users.';
        this.users = [];
        this.isUsersLoading = false;
      }
    });
  }

  loadMoreUsers(): void {
    if (this.isLoadingMoreUsers || this.usersCurrentPage >= this.usersTotalPages - 1) {
      return;
    }
    this.isLoadingMoreUsers = true;
    this.usersCurrentPage++;
    this.adminService.getAllUsers(this.usersCurrentPage, this.usersPageSize).subscribe({
      next: response => {
        this.users = [...this.users, ...(response.content || [])];
        this.isLoadingMoreUsers = false;
      },
      error: () => {
        this.toastr.error('Failed to load more users.');
        this.isLoadingMoreUsers = false;
      }
    });
  }

  // ===== HIDDEN POSTS =====
  loadHiddenPosts(): void {
    this.isHiddenPostsLoading = true;
    this.hiddenPostsError = null;
    this.adminService.getHiddenPosts(this.hiddenPostsCurrentPage, this.hiddenPostsPageSize).subscribe({
      next: response => {
        this.hiddenPosts = response.content || [];
        this.hiddenPostsTotalPages = response.totalPages || 0;
        this.hiddenPostsTotalElements = response.totalElements || 0;
        this.isHiddenPostsLoading = false;
      },
      error: () => {
        this.hiddenPostsError = 'Failed to load hidden posts.';
        this.hiddenPosts = [];
        this.isHiddenPostsLoading = false;
      }
    });
  }

  loadMoreHiddenPosts(): void {
    if (this.isLoadingMoreHiddenPosts || this.hiddenPostsCurrentPage >= this.hiddenPostsTotalPages - 1) {
      return;
    }
    this.isLoadingMoreHiddenPosts = true;
    this.hiddenPostsCurrentPage++;
    this.adminService.getHiddenPosts(this.hiddenPostsCurrentPage, this.hiddenPostsPageSize).subscribe({
      next: response => {
        this.hiddenPosts = [...this.hiddenPosts, ...(response.content || [])];
        this.isLoadingMoreHiddenPosts = false;
      },
      error: () => {
        this.toastr.error('Failed to load more hidden posts.');
        this.isLoadingMoreHiddenPosts = false;
      }
    });
  }

  onUnhidePost(postId: number): void {
    this.adminService.unhidePost(postId).subscribe({
      next: () => {
        this.toastr.success('Post has been unhidden.', 'Success');
        
        this.hiddenPosts = this.hiddenPosts.filter(p => p.id !== postId);
        this.hiddenPostsTotalElements--;
        
        const post = this.posts.find(p => p.id === postId);
        if (post) {
          post.hidden = false;
        }
        
        this.loadDashboardStats();
      },
      error: () => this.toastr.error('Failed to unhide post.', 'Error')
    });
  }

  // ===== HIDE POST MODAL =====
  onHidePost(postId: number, postTitle: string): void {
    this.postIdToHide = postId;
    this.hidePostTitle = postTitle;
    this.isHidePostModalOpen = true;
  }

  onHidePostConfirm(): void {
    if (!this.postIdToHide) return;

    this.isHidingPost = true;
    const postId = this.postIdToHide;

    this.adminService.hidePost(postId).subscribe({
      next: () => {
        this.toastr.success('Post has been hidden successfully.', 'Success');
        this.isHidingPost = false;
        this.isHidePostModalOpen = false;

        this.reports = this.reports.filter(r => r.reportedPostId !== postId);
        this.reportsTotalElements--;
        
        const post = this.posts.find(p => p.id === postId);
        if (post) {
          post.hidden = true;
        }

        this.loadDashboardStats();
      },
      error: (e) => {
        this.toastr.error(e.error.message, 'Failed to hide post.');
        this.isHidingPost = false;
      }
    });
  }

  onHidePostCancel(): void {
    this.isHidePostModalOpen = false;
    this.postIdToHide = null;
    this.hidePostTitle = '';
  }

  // ===== REPORT ACTIONS =====
  onDismissReport(reportId: number): void {
    this.adminService.dismissReport(reportId).subscribe({
      next: () => {
        this.toastr.success('Report dismissed.');
        this.reports = this.reports.filter(r => r.id !== reportId);
        this.reportsTotalElements--;
      },
      error: () => this.toastr.error('Failed to dismiss report.')
    });
  }

  onDismissUserReport(reportId: number): void {
    this.adminService.dismissUserReport(reportId).subscribe({
      next: () => {
        this.toastr.success('User report dismissed.');
        this.reportedUsers = this.reportedUsers.filter(r => r.id !== reportId);
        this.reportedUsersTotalElements--;
      },
      error: () => this.toastr.error('Failed to dismiss user report.')
    });
  }

  onDeletePost(postId: number): void {
    this.modalTitle = 'Confirm Post Deletion';
    this.modalMessage = 'Are you sure you want to permanently delete this post? This action cannot be undone.';
    this.confirmAction = () => this.executeDeletePost(postId);
    this.isModalOpen = true;
  }

  private executeDeletePost(postId: number): void {
    this.adminService.deletePost(postId).subscribe({
      next: () => {
        
        this.toastr.success('Post deleted successfully.');
        this.reports = this.reports.filter(r => r.reportedPostId !== postId);
        this.posts = this.posts.filter(p => p.id !== postId);
        this.hiddenPosts = this.hiddenPosts.filter(p => p.id !== postId);
        this.reportsTotalElements--;
        this.postsTotalElements--;
        this.hiddenPostsTotalElements--;
        this.loadDashboardStats();
      },
      error: (e) => {
        console.log(e);
        this.toastr.error(e.error.message,'Failed to delete post.')
      }
    });
  }

  onDeleteUser(userId: number, username: string): void {
    this.modalTitle = 'Confirm User Deletion';
    this.modalMessage = `Are you sure you want to delete the user "${username}"? All their data will be permanently removed.`;
    this.confirmAction = () => this.executeDeleteUser(userId);
    this.isModalOpen = true;
  }

  private executeDeleteUser(userId: number): void {
    this.adminService.deleteUser(userId).subscribe({
      next: () => {
        this.toastr.success('User deleted successfully.');
        this.users = this.users.filter(u => u.id !== userId);
        this.usersTotalElements--;
      },
      error: (e) => {
        this.toastr.error(e.error.message, 'Failed to delete user.')
      }
    });
  }

loadBannedUsers(): void {
  this.isBannedUsersLoading = true;
  this.bannedUsersError = null;
  this.adminService.getBannedUsers(this.bannedUsersCurrentPage, this.bannedUsersPageSize).subscribe({
    next: response => {
      console.log(response);
      
      this.bannedUsers = response.content || [];
      this.bannedUsersTotalPages = response.totalPages || 0;
      this.bannedUsersTotalElements = response.totalElements || 0;
      this.isBannedUsersLoading = false;
    },
    error: () => {
      this.bannedUsersError = 'Failed to load banned users.';
      this.bannedUsers = [];
      this.isBannedUsersLoading = false;
    }
  });
}

loadMoreBannedUsers(): void {
  if (this.isLoadingMoreBannedUsers || this.bannedUsersCurrentPage >= this.bannedUsersTotalPages - 1) {
    return;
  }
  this.isLoadingMoreBannedUsers = true;
  this.bannedUsersCurrentPage++;
  this.adminService.getBannedUsers(this.bannedUsersCurrentPage, this.bannedUsersPageSize).subscribe({
    next: response => {
      this.bannedUsers = [...this.bannedUsers, ...(response.content || [])];
      this.isLoadingMoreBannedUsers = false;
    },
    error: () => {
      this.toastr.error('Failed to load more banned users.');
      this.isLoadingMoreBannedUsers = false;
    }
  });
}

onBanUser(userId: number, username: string): void { 
  this.onBanUserAction(userId, username); 
}

onBanUserAction(userId: number, username: string): void {
  this.modalTitle = 'Confirm User Ban';
  this.modalMessage = `Are you sure you want to ban "${username}"? They will not be able to login or interact until unbanned.`;
  this.confirmAction = () => this.executeBanUser(userId);
  this.isModalOpen = true;
}

private executeBanUser(userId: number): void {
  this.adminService.banUser(userId).subscribe({
    next: () => {
      this.toastr.success('User has been banned successfully.');
      
      // Update user in the main users array
      const user = this.users.find(u => u.id === userId);
      if (user) {
        user.enabled = false;
      }
      
      // Update in reported users array
      const reportedUser = this.reportedUsers.find(r => r.reportedUserId === userId);
      if (reportedUser) {
        reportedUser.enabled = false;
      }
      
      this.loadDashboardStats();
    },
    error: (e) => {
      this.toastr.error(e.error.message || 'Failed to ban user.', 'Error');
    }
  });
}

onUnbanUser(userId: number, username: string): void {
  this.modalTitle = 'Confirm User Unban';
  this.modalMessage = `Are you sure you want to unban "${username}"? They will be able to login and interact again.`;
  this.confirmAction = () => this.executeUnbanUser(userId);
  this.isModalOpen = true;
}

private executeUnbanUser(userId: number): void {
  this.adminService.unbanUser(userId).subscribe({
    next: () => {
      this.toastr.success('User has been unbanned successfully.');
      
      // Remove from banned users array
      this.bannedUsers = this.bannedUsers.filter(u => u.id !== userId);
      this.bannedUsersTotalElements--;
      
      // Update in main users array if exists
      const user = this.users.find(u => u.id === userId);
      if (user) {
        user.enabled = true;
      }
      
      // Update in reported users array
      const reportedUser = this.reportedUsers.find(r => r.reportedUserId === userId);
      if (reportedUser) {
        reportedUser.enabled = true;
      }
      
      this.loadDashboardStats();
    },
    error: (e) => {
      this.toastr.error(e.error.message || 'Failed to unban user.', 'Error');
    }
  });
}


  handleConfirm(): void {
    if (this.confirmAction) {
      this.confirmAction();
    }
    this.closeModal();
  }

  closeModal(): void {
    this.isModalOpen = false;
    this.confirmAction = null;
  }
}