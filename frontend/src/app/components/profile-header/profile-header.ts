import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-profile-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './profile-header.html',
  styleUrls: ['./profile-header.scss']
})
export class ProfileHeaderComponent {
  @Input() username: string = '';
  @Input() name: string = '';

  @Input() postCount: number = 0;
  @Input() followerCount: number = 0;
  @Input() followingCount: number = 0;
  
  formatNumber(num: number): string {
    if (num >= 1000000) {
      return (num / 1000000).toFixed(1) + 'M';
    }
    if (num >= 1000) {
      return (num / 1000).toFixed(1) + 'K';
    }
    return num.toString();
  }
}