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
  size: string;
}

@Component({
  selector: 'app-create-post',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './create-post.html',
  styleUrls: ['./create-post.scss'],
})
export class CreatePostComponent {
  model = { title: '', content: '' };
  selectedFiles: FilePreview[] = [];
  isDragging = false;
  isSubmitting = false;
  maxFiles = 5;

  private readonly MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
  private readonly MAX_VIDEO_SIZE = 100 * 1024 * 1024; // 100MB
  private readonly ALLOWED_IMAGE_TYPES = ['image/jpg','image/jpeg', 'image/png', 'image/gif', 'image/webp'];
  private readonly ALLOWED_VIDEO_TYPES = [
    'video/mp4',
    'video/webm',
    'video/quicktime',
    'video/x-msvideo',
  ];

  constructor(
    private postService: PostService,
    private router: Router,
    private toastr: ToastrService
  ) {}

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
    if (this.selectedFiles.length >= this.maxFiles) {
      this.toastr.warning(`Maximum ${this.maxFiles} files allowed`, 'Upload Limit');
      return;
    }

    const empty = this.maxFiles - this.selectedFiles.length;
    // empty = 5 - 3 = 2
    let addedCount = 0;
    let skippedCount = 0;
    const errors: string[] = [];

    for (let i = 0; i < files.length && addedCount < empty; i++) {
      const file = files[i];

      const validation = this.validateFile(file);
      if (!validation.valid) {
        skippedCount++;
        if (validation.error && !errors.includes(validation.error)) {
          errors.push(validation.error);
        }
        continue;
      }

      const reader = new FileReader();

      reader.onload = (e: any) => {
        console.log("Render: ", e);
        
        this.selectedFiles.push({
          file: file,
          preview: e.target.result,
          type: file.type.startsWith('image/') ? 'image' : 'video',
          name: file.name,
          size: this.formatFileSize(file.size),
        });
      };

      reader.readAsDataURL(file);
      addedCount++;
    }

    if (addedCount > 0) {
      this.toastr.success(
        `${addedCount} file${addedCount > 1 ? 's' : ''} added successfully`,
        'Upload Success'
      );
    }

    if (skippedCount > 0) {
      errors.forEach((error) => {
        this.toastr.error(error, 'Invalid File');
      });

      if (errors.length === 0) {
        this.toastr.warning(
          `${skippedCount} file${skippedCount > 1 ? 's were' : ' was'} skipped`,
          'Upload Warning'
        );
      }
    }

    if (files.length > empty && addedCount === empty) {
      this.toastr.info(
        `Only ${empty} more file${empty > 1 ? 's' : ''} could be added`,
        'Upload Limit'
      );
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
    console.log("FileInput:", fileInput);
    
    fileInput?.click();
  }

  openFilePicker(): void {
    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    console.log("FileInput open filepicker:", fileInput);

    fileInput?.click();
  }

  onSubmit(): void {
    this.isSubmitting = true;
    const formData = new FormData();
    formData.append('title', this.model.title);
    formData.append('content', this.model.content);

    this.selectedFiles.forEach((filePreview) => {
      formData.append('mediaFiles', filePreview.file);
    });

    this.postService.createPost(formData).subscribe({
      next: (post: any) => this.router.navigate(['/post', post.id]),
      error: (err: any) => {
        this.toastr.error(err.error.message, 'Failed to create post');
        this.isSubmitting = false;
      },
    });
  }
  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
  }

  validateFile(file: File): { valid: boolean; error?: string } {
    const isImage = file.type.startsWith('image/');
    const isVideo = file.type.startsWith('video/');

    if (isImage && !this.ALLOWED_IMAGE_TYPES.includes(file.type)) {
      return {
        valid: false,
        error: `Invalid image type. Allowed: JPG, PNG, GIF, WEBP`,
      };
    }

    if (isVideo && !this.ALLOWED_VIDEO_TYPES.includes(file.type)) {
      return {
        valid: false,
        error: `Invalid video type. Allowed: MP4, WEBM, MOV, AVI`,
      };
    }

    if (!isImage && !isVideo) {
      return {
        valid: false,
        error: `Only images and videos are allowed`,
      };
    }

    if (isImage && file.size > this.MAX_IMAGE_SIZE) {
      return {
        valid: false,
        error: `Image too large. Max size: ${this.formatFileSize(this.MAX_IMAGE_SIZE)}`,
      };
    }

    if (isVideo && file.size > this.MAX_VIDEO_SIZE) {
      return {
        valid: false,
        error: `Video too large. Max size: ${this.formatFileSize(this.MAX_VIDEO_SIZE)}`,
      };
    }

    return { valid: true };
  }
}
