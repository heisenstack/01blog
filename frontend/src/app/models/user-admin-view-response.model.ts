import { UserAdminView } from './user-admin-view.model';

export interface UserAdminViewResponse {
  content: UserAdminView[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}