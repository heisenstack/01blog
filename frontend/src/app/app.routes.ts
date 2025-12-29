import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Login } from './auth/login/login';
import { Register } from './auth/register/register';
import { CreatePostComponent } from './pages/create-post/create-post';
import { PostDetailComponent } from './pages/post-detail/post-detail';
import { EditPostComponent } from './pages/edit-post/edit-post';
import { AuthGuard } from './auth/auth-guard';
import { UserProfileComponent } from './pages/user-profile/user-profile';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { AdminGuard } from './admin.guard';
import { FeedComponent } from './pages/feed/feed';
import { UserSuggestionsComponent } from './pages/user-suggestions/user-suggestions.component';
import { NotificationsComponent } from './pages/notifications/notifications.component';
import { NotFoundComponent } from './pages/not-found/not-found.component';

export const routes: Routes = [
  // --- PUBLIC ROUTES ---

  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: 'home', component: Home },
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'post/:id', component: PostDetailComponent },
  { path: 'profile/:username', component: UserProfileComponent },

  // --- PROTECTED ROUTES ---
  {
    path: 'admin',
    component: AdminDashboardComponent,
    canActivate: [AdminGuard],
  },
  { path: 'discover', component: UserSuggestionsComponent, canActivate: [AuthGuard] },

  {
    path: 'feed',
    component: FeedComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'create-post',
    component: CreatePostComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'edit-post/:id',
    component: EditPostComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'notifications',
    component: NotificationsComponent,
    canActivate: [AuthGuard],
  },
  { path: '**', component: NotFoundComponent },
];
