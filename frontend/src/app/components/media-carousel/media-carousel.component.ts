import { Component, Input } from '@angular/core'; 
import { CommonModule } from '@angular/common';
import { PostMedia } from '../../models/post.model';

@Component({
  selector: 'app-media-carousel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './media-carousel.component.html',
  styleUrls: ['./media-carousel.component.scss']
})
export class MediaCarouselComponent {
  @Input() mediaFiles: PostMedia[] = [];
  
  currentIndex = 0;

  nextSlide(): void {
    this.currentIndex = (this.currentIndex + 1) % this.mediaFiles.length;
  }

  previousSlide(): void {
    this.currentIndex = (this.currentIndex - 1 + this.mediaFiles.length) % this.mediaFiles.length;
  }

  goToSlide(index: number): void {
    this.currentIndex = index;
  }

  get currentMedia(): PostMedia {
    return this.mediaFiles[this.currentIndex];
  }

  get totalSlides(): number {
    return this.mediaFiles.length;
  }
}