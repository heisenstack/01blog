import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { UserService } from '../../services/user.service';
import { UserSuggestion } from '../../models/user-suggestion.model';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-user-suggestions',
  templateUrl: './user-suggestions.component.html',
  styleUrls: ['./user-suggestions.component.scss'],
  standalone: true,
  imports: [CommonModule, RouterModule]
})
export class UserSuggestionsComponent implements OnInit {
  activeTab: 'suggestions' | 'following' = 'suggestions';

  // Suggestions
  suggestions: UserSuggestion[] = [];
  isSuggestionsLoading = false;
  suggestionsError: string | null = null;
  suggestionsCurrentPage = 0;
  suggestionsPageSize = 10;
  suggestionsTotalPages = 0;
  suggestionsTotalElements = 0;
  isLoadingMoreSuggestions = false;

  // Following
  following: UserSuggestion[] = [];
  isFollowingLoading = false;
  followingError: string | null = null;
  followingCurrentPage = 0;
  followingPageSize = 10;
  followingTotalPages = 0;
  followingTotalElements = 0;
  isLoadingMoreFollowing = false;

  constructor(
    private userService: UserService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    // Load suggestions on init (default tab)
    this.loadSuggestions();
  }

  selectTab(tab: 'suggestions' | 'following'): void {
    // Only fetch if switching to a different tab
    if (this.activeTab === tab) {
      return;
    }

    this.activeTab = tab;
    
    // Fetch data based on the selected tab
    if (tab === 'suggestions') {
      this.loadSuggestions();
    } else if (tab === 'following') {
      this.loadFollowing();
    }
  }

  loadSuggestions(): void {
    this.isSuggestionsLoading = true;
    this.suggestionsError = null;
    
    // Reset pagination
    this.suggestionsCurrentPage = 0;
    
    this.userService.getSuggestedUsers(this.suggestionsCurrentPage, this.suggestionsPageSize).subscribe({
      next: response => {
        console.log("This is response: " , response);
        
        this.suggestions = response.content || [];
        this.suggestionsTotalPages = response.totalPages || 0;
        this.suggestionsTotalElements = response.totalElements || 0;
        this.isSuggestionsLoading = false;
      },
      error: () => {
        this.suggestionsError = 'Failed to load suggested users.';
        this.suggestions = [];
        this.isSuggestionsLoading = false;
      }
    });
  }

  loadMoreSuggestions(): void {
    if (this.isLoadingMoreSuggestions || this.suggestionsCurrentPage >= this.suggestionsTotalPages - 1) {
      return;
    }

    this.isLoadingMoreSuggestions = true;
    this.suggestionsCurrentPage++;

    this.userService.getSuggestedUsers(this.suggestionsCurrentPage, this.suggestionsPageSize).subscribe({
      next: response => {
        console.log("This is load more response: " , response);

        this.suggestions = [...this.suggestions, ...(response.content || [])];
        this.isLoadingMoreSuggestions = false;
      },
      error: () => {
        this.toastr.error('Failed to load more suggestions.');
        this.isLoadingMoreSuggestions = false;
        this.suggestionsCurrentPage--;
      }
    });
  }

  loadFollowing(): void {
    this.isFollowingLoading = true;
    this.followingError = null;
    
    this.followingCurrentPage = 0;
    
    this.userService.getFollowingUsers(this.followingCurrentPage, this.followingPageSize).subscribe({
      next: response => {
        console.log(response);
        
        this.following = response.content || [];
        this.followingTotalPages = response.totalPages || 0;
        this.followingTotalElements = response.totalElements || 0;
        this.isFollowingLoading = false;
      },
      error: () => {
        this.followingError = 'Failed to load following users.';
        this.following = [];
        this.isFollowingLoading = false;
      }
    });
  }

  loadMoreFollowing(): void {
    if (this.isLoadingMoreFollowing || this.followingCurrentPage >= this.followingTotalPages - 1) {
      return;
    }

    this.isLoadingMoreFollowing = true;
    this.followingCurrentPage++;

    this.userService.getFollowingUsers(this.followingCurrentPage, this.followingPageSize).subscribe({
      next: response => {
        this.following = [...this.following, ...(response.content || [])];
        this.isLoadingMoreFollowing = false;
      },
      error: () => {
        this.toastr.error('Failed to load more following users.');
        this.isLoadingMoreFollowing = false;
        this.followingCurrentPage--;
      }
    });
  }

  toggleFollow(user: UserSuggestion): void {
    const action = user.subscribed
      ? this.userService.unsubscribe(user.username)
      : this.userService.subscribe(user.username);

    action.subscribe({
      next: () => {
        // Update the subscribed status
        user.subscribed = !user.subscribed;
        user.subscribed ? user.followerCount++ : user.followerCount--;
        
        // Remove from current list based on tab
        if (this.activeTab === 'suggestions' && user.subscribed) {
          // User was followed from suggestions tab - remove from suggestions
          this.suggestions = this.suggestions.filter(u => u.id !== user.id);
          this.suggestionsTotalElements--;
        } else if (this.activeTab === 'following' && !user.subscribed) {
          // User was unfollowed from following tab - remove from following
          this.following = this.following.filter(u => u.id !== user.id);
          this.followingTotalElements--;
        }
      },
      error: () => {
        this.toastr.error('Something went wrong. Please try again.');
      }
    });
  }
}