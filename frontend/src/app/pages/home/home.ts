import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { PostService } from '../../services/post.service';
import { UserService } from '../../services/user.service';
import { Post } from '../../models/post.model';
import { UserSuggestion } from '../../models/user-suggestion.model';
import { AuthService } from '../../services/auth.service';
import { Observable } from 'rxjs';
import { PostCardComponent } from '../../components/post-card/post-card';
import { Page } from '../../models/page.model';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    PostCardComponent
  ],
  templateUrl: './home.html',
  styleUrls: ['./home.scss']
})
export class Home implements OnInit {
  
  posts: Post[] = [];
  isLoggedIn$: Observable<boolean>;
  isLoading: boolean = true;

  // Pagination state
  currentPage = 0;
  pageSize = 10; 
  totalPages = 0;
  isLoadingMore = false;

  // Suggested users
  suggestedUsers: UserSuggestion[] = [];
  isSuggestionsLoading = false;

  constructor(
    private postService: PostService,
    private userService: UserService,
    private authService: AuthService,
    private toastr: ToastrService
  ) {
    this.isLoggedIn$ = this.authService.isLoggedIn$;
  }

  ngOnInit(): void {
    this.loadPosts();
    
    if (this.authService.isAuthenticated()) {
      
      this.loadSuggestedUsers();
    } else {
      
    }
  }

  loadPosts(): void {
    this.isLoading = true;
    this.postService.getPosts(this.currentPage, this.pageSize).subscribe({
      next: (data: Page<Post>) => {
        // console.log("Daaaaaaaaaata: ", data);
        
        this.posts = data.content;
        this.totalPages = data.totalPages;
        this.isLoading = false;
      },
      error: (err: any) => {
        // console.error('Failed to fetch posts:', err);
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

    this.postService.getPosts(this.currentPage, this.pageSize).subscribe({
      next: (data: Page<Post>) => {
        this.posts = [...this.posts, ...data.content];
        this.isLoadingMore = false;
      },
      error: (err: any) => {
        // console.error('Failed to fetch more posts:', err);
        this.isLoadingMore = false;
      }
    });
  }

  loadSuggestedUsers(): void {
    this.isSuggestionsLoading = true;
    
    
    this.userService.getSuggestedUsers(0, 5).subscribe({
      next: (response) => {
        
        this.suggestedUsers = response.content || [];
        // console.log('Suggested users array:', this.suggestedUsers);
        
        this.isSuggestionsLoading = false;
      },
      error: (err) => {
        // console.error('Failed to load suggested users:', err);
        this.isSuggestionsLoading = false;
      }
    });
  }

  toggleFollow(user: UserSuggestion, event: Event): void {
    event.preventDefault();
    event.stopPropagation();

    const action = user.subscribed
      ? this.userService.unsubscribe(user.username)
      : this.userService.subscribe(user.username);

    action.subscribe({
      next: () => {
        user.subscribed = !user.subscribed;
        user.subscribed ? user.followerCount++ : user.followerCount--;
      },
      error: (error) => {
        // console.log("Home comp view: ", error);
        
        this.toastr.error(error.error.message, 'Error following user');
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