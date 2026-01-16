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
  isDragging: boolean = false;
  maxFiles: number = 5;

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
    this.addFiles(event.target.files);
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.isDragging = false;
    if (event.dataTransfer?.files) {
      this.addFiles(event.dataTransfer.files);
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isDragging = true;
  }

  onDragLeave(event: DragEvent): void {
    this.isDragging = false;
  }

  addFiles(files: FileList): void {
    for (let i = 0; i < files.length && this.selectedFiles.length < this.maxFiles; i++) {
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

  clearAllFiles(): void {
    this.selectedFiles = [];
  }

  handleUploadClick(): void {
    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    fileInput?.click();
  }

  openFilePicker(): void {
    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    fileInput?.click();
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
        this.toastr.error(err.error.message, "Failed to update post");
        this.isSubmitting = false;
      }
    });
  }
}