import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { PostService } from '../../services/post.service';
import { Post } from '../../models/post.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-edit-post',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './edit-post.html',
  styleUrls: ['./edit-post.scss']
})
export class EditPostComponent implements OnInit {
  post: Post | null = null;
  errorMessage: string | null = null;
  isSubmitting = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private postService: PostService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      const postId = +id;
      this.postService.getPost(postId).subscribe({
        next: (data: Post) => {
          console.log(data);
          
          this.post = data;
        },
        error: (err: any) => {
          console.log(err);
          
          this.errorMessage = 'Could not load post for editing. You may not have permission or the post may not exist.';
          this.toastr.error('Could not load post data.', 'Error');
        }
      });
    }
  }

  onSubmit(): void {
    if (!this.post) {
      this.toastr.error('Post data is not available.', 'Error');
      return;
    }
    this.isSubmitting = true;


    const formData = new FormData();
    formData.append('title', this.post.title);
    formData.append('content', this.post.content);

    this.postService.updatePost(this.post.id, formData).subscribe({
      next: () => {
        this.toastr.success('Your changes have been saved.', 'Post Updated!');
        this.router.navigate(['/post', this.post?.id]);
      },
      error: (err) => {
        this.toastr.error(err.error, 'Update Failed');
        this.isSubmitting = false;
      }
    });
  }
}