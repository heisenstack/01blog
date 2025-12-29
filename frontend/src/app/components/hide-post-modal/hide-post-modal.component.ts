import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-hide-post-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './hide-post-modal.component.html',
  styleUrls: ['./hide-post-modal.component.scss']
})
export class HidePostModalComponent {
  @Input() isOpen = false;
  @Input() postTitle = '';
  @Input() isSubmitting = false;
  @Output() onConfirm = new EventEmitter<void>();
  @Output() onCancel = new EventEmitter<void>();

  confirm(): void {
    this.onConfirm.emit();
  }

  cancel(): void {
    this.onCancel.emit();
  }

  onBackdropClick(event: MouseEvent): void {
    if (event.target === event.currentTarget) {
      this.cancel();
    }
  }
}