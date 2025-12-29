import { Comment } from './comment.model';

export interface CommentResponse {
  content: Comment[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}