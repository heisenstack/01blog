import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PostMedia } from '../../models/post.model';

@Component({
  selector: 'app-media-carousel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './media-carousel.component.html',
  styleUrls: ['./media-carousel.component.scss']
})
export class MediaCarouselComponent implements OnInit {
  @Input() mediaFiles: PostMedia[] = [];
  
  currentIndex = 0;
  isAutoPlay = true;
  autoPlayInterval: any;

  ngOnInit(): void {
    if (this.mediaFiles.length > 1) {
      this.startAutoPlay();
    }
  }

  ngOnDestroy(): void {
    if (this.autoPlayInterval) {
      clearInterval(this.autoPlayInterval);
    }
  }

  nextSlide(): void {
    this.currentIndex = (this.currentIndex + 1) % this.mediaFiles.length;
    this.resetAutoPlay();
  }

  previousSlide(): void {
    this.currentIndex = (this.currentIndex - 1 + this.mediaFiles.length) % this.mediaFiles.length;
    this.resetAutoPlay();
  }

  goToSlide(index: number): void {
    this.currentIndex = index;
    this.resetAutoPlay();
  }

  startAutoPlay(): void {
    this.autoPlayInterval = setInterval(() => {
      if (this.isAutoPlay) {
        this.nextSlide();
      }
    }, 5000); // 5 seconds
  }

  resetAutoPlay(): void {
    if (this.autoPlayInterval) {
      clearInterval(this.autoPlayInterval);
    }
    this.startAutoPlay();
  }

  toggleAutoPlay(): void {
    this.isAutoPlay = !this.isAutoPlay;
  }

  get currentMedia(): PostMedia {
    return this.mediaFiles[this.currentIndex];
  }

  get totalSlides(): number {
    return this.mediaFiles.length;
  }
}