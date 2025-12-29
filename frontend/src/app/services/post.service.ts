import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Post } from '../models/post.model';
import { Page } from '../models/page.model'; 

export interface PostLikeResponse {
  likeCount: number;
  likedByCurrentUser: boolean;
}

export interface ReportPayload {
  reason: string;
  details?: string;
}

@Injectable({
  providedIn: 'root'
})
export class PostService {
  private apiUrl = 'http://localhost:8080/api/posts';

  constructor(private http: HttpClient) { }

  getPosts(page: number, size: number): Observable<Page<Post>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<Post>>(this.apiUrl, { params });
  }
    getPostsforAdmin(page: number, size: number): Observable<Page<Post>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<Post>>("http://localhost:8080/api/admin/posts", { params });
  }

  getFeed(page: number, size: number): Observable<Page<Post>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<Post>>(`${this.apiUrl}/feed`, { params });
  }

  getPost(id: number): Observable<Post> {
    return this.http.get<Post>(`${this.apiUrl}/${id}`);
  }

  createPost(postData: FormData): Observable<Post> {
    return this.http.post<Post>(this.apiUrl, postData);
  }

  updatePost(id: number, postData: FormData): Observable<Post> {
    return this.http.put<Post>(`${this.apiUrl}/${id}`, postData);
  }

  deletePost(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  likePost(postId: number): Observable<PostLikeResponse> {
    return this.http.post<PostLikeResponse>(`${this.apiUrl}/${postId}/like`, {});
  }

  unlikePost(postId: number): Observable<PostLikeResponse> {
    return this.http.post<PostLikeResponse>(`${this.apiUrl}/${postId}/unlike`, {});
  }

  reportPost(postId: number, payload: ReportPayload): Observable<string> {
    return this.http.post(`${this.apiUrl}/${postId}/report`, payload, { responseType: 'text' });
  }
}