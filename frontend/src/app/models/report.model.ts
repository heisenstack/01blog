export interface Report {
  id: number;
  reason: string;
  details: string;
  createdAt: string; 
  reporterUsername: string;
  reportedPostId: number;
  reportedPostTitle: string;
}