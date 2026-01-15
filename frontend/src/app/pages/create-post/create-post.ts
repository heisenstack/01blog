import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PostService } from '../../services/post.service';
import { ToastrService } from 'ngx-toastr';


interface FilePreview {
  file: File;
  preview: string;
  type: 'image' | 'video';
  name: string;
}

@Component({
  selector: 'app-create-post',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './create-post.html',
  styleUrls: ['./create-post.scss']
})
export class CreatePostComponent {
  model = { title: '', content: '' };
  selectedFiles: FilePreview[] = [];
  isDragging = false;
  isSubmitting = false;
  maxFiles = 10;

    
  constructor(private postService: PostService, private router: Router, private toastr: ToastrService) {}

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
    this.isSubmitting = true;
    const formData = new FormData();
    formData.append('title', this.model.title);
    formData.append('content', this.model.content);
    
    this.selectedFiles.forEach(filePreview => {
      formData.append('mediaFiles', filePreview.file);
    });

    this.postService.createPost(formData).subscribe({
      next: (post: any) => this.router.navigate(['/post', post.id]),
      error: (err: any) => {
        // console.error('Error creating post:', err);
        this.toastr.error(err.error.message, "Failed to create post");
        this.isSubmitting = false;
      }
    });
  }
}