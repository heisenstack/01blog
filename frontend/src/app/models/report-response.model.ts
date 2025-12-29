import { Report } from './report.model';

export interface ReportResponse {
  content: Report[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}