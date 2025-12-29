export interface UserReport {
  id: number;
  reason: string;
  details: string;
  createdAt: string;
  reporterUsername: string;
  reportedUserId: number;
  reportedUsername: string;
  enabled: boolean;
}