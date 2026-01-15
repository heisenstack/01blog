import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PostService } from '../../services/post.service';
import { Post } from '../../models/post.model';
import { ToastrService } from 'ngx-toastr';


interface FilePreview {
  file: File;
  preview: string;
  type: 'image' | 'video';
  name: string;
}

@Component({
  selector: 'app-edit-post',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './edit-post.html',
  styleUrls: ['./edit-post.scss']
})
export class EditPostComponent implements OnInit {
  post: Post | null = null;
  errorMessage: string = '';
  isSubmitting: boolean = false;
  selectedFiles: FilePreview[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private postService: PostService,
    private toastr: ToastrService,
  ) {}

  ngOnInit(): void {
    const postId = this.route.snapshot.paramMap.get('id');
    if (postId) {
      this.postService.getPost(+postId).subscribe({
        next: (data: Post) => this.post = data,
        error: (err: any) => this.errorMessage = 'Failed to load post.'
      });
    }
  }

  onFileSelected(event: any): void {
    const files: FileList = event.target.files;
    if (!files) return;

    for (let i = 0; i < files.length; i++) {
      const file = files[i];
      const reader = new FileReader();
      
      reader.onload = (e: any) => {
        this.selectedFiles.push({
          file: file,
          preview: e.target.result,
          type: file.type.startsWith('image/') ? 'image' : 'video',
          name: file.name
        });
      };
      
      reader.readAsDataURL(file);
    }
  }

  removeFile(index: number): void {
    this.selectedFiles.splice(index, 1);
  }

  isImage(url: string): boolean {
    return /\.(jpg|jpeg|png|gif|webp)$/i.test(url);
  }

  isVideo(url: string): boolean {
    return /\.(mp4|webm|ogg|mov)$/i.test(url);
  }

  onSubmit(): void {
    if (!this.post) return;
    
    this.isSubmitting = true;
    const formData = new FormData();
    formData.append('title', this.post.title);
    formData.append('content', this.post.content);
    
    this.selectedFiles.forEach(filePreview => {
      formData.append('mediaFiles', filePreview.file);
    });

    this.postService.updatePost(this.post.id, formData).subscribe({
      next: () => this.router.navigate(['/post', this.post!.id]),
      error: (err: any) => {
        this.toastr.error(err.error.message, "Failed to create post");

        // this.errorMessage = 'Failed to update post.';
        this.isSubmitting = false;
      }
    });
  }
}