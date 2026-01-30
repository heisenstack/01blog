export interface DashboardStats {
  totalUsers: number;
  totalPosts: number;
  hiddenPosts: number;
  activeReports: number;
  newUsersLast30Days: number;
  bannedUsers: number;
  mostReportedUser?: TopUser;
  mostReporterUser?: TopUser;
}

export interface TopUser {
  userId: number;
  username: string;
  count: number;
}