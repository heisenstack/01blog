import { Component, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { PostService } from '../../services/post.service';
import { ToastrService } from 'ngx-toastr';

interface FilePreview {
  file: File;
  preview: string | ArrayBuffer | null;
  name: string;
  type: 'image' | 'video' | 'unknown';
}

@Component({
  selector: 'app-create-post',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './create-post.html',
  styleUrls: ['./create-post.scss']
})
export class CreatePostComponent {
  model: any = { title: '', content: '' };

  selectedFiles: FilePreview[] = [];
  maxFiles = 5;
  isDragging = false;

  isSubmitting = false;

  @ViewChild('fileInput') fileInput!: ElementRef;

  constructor(
    private postService: PostService,
    private router: Router,
    private toastr: ToastrService
  ) {}


  onFileSelected(event: any): void {
    const files = event.target.files;
    if (files) {
      this.handleFiles(Array.from(files));
    }
    event.target.value = '';
  }

  handleUploadClick(): void {
    if (this.selectedFiles.length === 0) {
      this.openFilePicker();
    }
  }

  openFilePicker(): void {
    if (this.fileInput) {
      this.fileInput.nativeElement.click();
    }
  }

  handleFiles(files: any[]): void {
    const totalFiles = this.selectedFiles.length + files.length;

    if (totalFiles > this.maxFiles) {
      this.toastr.warning(
        `You can only upload up to ${this.maxFiles} files.`,
        'Too Many Files'
      );
      return;
    }

    files.forEach(file => {
      if (!this.selectedFiles.some(f => f.file.name === file.name)) {
        this.processFile(file);
      }
    });
  }

  private processFile(file: File): void {
    let type: 'image' | 'video' | 'unknown' = 'unknown';

    if (file.type.startsWith('image/')) {
      type = 'image';
      this.generateImagePreview(file);
    } else if (file.type.startsWith('video/')) {
      type = 'video';
    } else {
      this.toastr.error('Only images and videos are supported', 'Invalid File Type');
      return;
    }

    this.selectedFiles.push({
      file,
      preview: null,
      name: file.name,
      type
    });
  }

  private generateImagePreview(file: File): void {
    const reader = new FileReader();
    reader.onload = () => {
      const filePreview = this.selectedFiles.find(f => f.file === file);
      if (filePreview) {
        filePreview.preview = reader.result;
      }
    };
    reader.readAsDataURL(file);
  }

  removeFile(index: number): void {
    this.selectedFiles.splice(index, 1);
    if (this.fileInput) {
      this.fileInput.nativeElement.value = '';
    }
  }

  clearAllFiles(): void {
    this.selectedFiles = [];
    if (this.fileInput) {
      this.fileInput.nativeElement.value = '';
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
    const files = event.dataTransfer?.files;
    if (files) {
      this.handleFiles(Array.from(files));
    }
  }

  onSubmit(): void {
    if (!this.model.title || !this.model.content) {
      this.toastr.error('Title and content are required.', 'Validation Error');
      return;
    }

    if (this.model.title.length > 255) {
      this.toastr.error('Title must not exceed 255 characters.', 'Title Too Long');
      return;
    }

    this.isSubmitting = true;

    const formData = new FormData();
    formData.append('title', this.model.title);
    formData.append('content', this.model.content);

    this.selectedFiles.forEach((filePreview) => {
      formData.append('mediaFiles', filePreview.file, filePreview.file.name);
    });

    this.postService.createPost(formData).subscribe({
      next: (response) => {
        this.toastr.success('Your new post is now live!', 'Post Created!');
        this.router.navigate(['/post', response.id]);
      },
      error: (error) => {
        // console.error('Failed to create post:', error);
        if (error.status === 400 && error.error) {
          this.toastr.error(error.error, 'Validation Error');
        } else if (error.status === 401) {
          this.toastr.error('You are not authorized to create posts.', 'Unauthorized');
        } else {
          this.toastr.error('There was an error creating your post.', 'Error');
        }
        this.isSubmitting = false;
      }
    });
  }
}