import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface ReportPayload {
  reason: string;
  details: string;
}

@Component({
  selector: 'app-report-modal',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './report-modal.html',
  styleUrls: ['./report-modal.scss']
})
export class ReportModalComponent implements OnInit {
  @Input() isOpen = false;
  @Input() entityType: 'Post' | 'User' = 'Post';
  
  @Input() entityName!: string; 

  @Output() onClose = new EventEmitter<void>();
  @Output() onSubmit = new EventEmitter<ReportPayload>();

  reportReason = '';
  reportDetails = '';
  reportReasons = [
    { value: 'SPAM', display: 'Spam or misleading content' },
    { value: 'HARASSMENT', display: 'Harassment or hate speech' },
    { value: 'INAPPROPRIATE_CONTENT', display: 'Nudity or inappropriate content' },
    { value: 'INTELLECTUAL_PROPERTY', display: 'Intellectual property violation' },
    { value: 'SELF_HARM', display: 'Self-harm or suicidal content' },
    { value: 'OTHER', display: 'Other (please specify)' }
  ];

  constructor() { }


  ngOnInit(): void {
    if (this.entityName === undefined) {
    }
  }

  closeModal(): void {
    this.onClose.emit();
    this.resetForm();
  }

  submitReport(): void {
    if (!this.reportReason) {
      return;
    }
    this.onSubmit.emit({
      reason: this.reportReason,
      details: this.reportDetails
    });
  }

  private resetForm(): void {
    this.reportReason = '';
    this.reportDetails = '';
  }
}