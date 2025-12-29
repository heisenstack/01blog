import { UserReport } from './user-report.model';

export interface UserReportResponse {
  content: UserReport[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}