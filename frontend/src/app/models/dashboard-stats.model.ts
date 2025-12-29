export interface DashboardStats {
  totalUsers: number;
  totalPosts: number;
  activeReports: number;
  newUsersLast30Days: number;
  hiddenPosts: number;
  bannedUsers?: number;
}