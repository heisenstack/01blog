export interface UserAdminView {
  id: number;
  username: string;
  name: string;
  email: string;
  roles: string[];
  reportsSubmitted?: number;
   enabled?: boolean;
}