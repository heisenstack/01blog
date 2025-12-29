import { UserSuggestion } from './user-suggestion.model';

export interface UserSuggestionResponse {
  content: UserSuggestion[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}