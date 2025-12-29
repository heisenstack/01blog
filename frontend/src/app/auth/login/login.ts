import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {
  model: any = {};
  isSubmitting: boolean = false;
  showPassword: boolean = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private toastr: ToastrService
  ) {}

  onSubmit() {
    if (this.isSubmitting) return;
    this.isSubmitting = true;

    this.authService.login(this.model).subscribe({
      next: (response) => {
        // console.log(response);
        this.toastr.success('Login successful!', 'Welcome Back!');
        setTimeout(() => {
          this.router.navigate(['/home']);
        }, 100); 
      },
      error: (error) => {
        // console.log(error);
        
        this.isSubmitting = false;
        this.handleLoginError(error);
      }
    });
  }

  private handleLoginError(error: any): void {
    if (error.status === 400 && error.error?.fieldErrors) {
      const fieldErrors = error.error.fieldErrors;
      
      if (fieldErrors.username) {
        this.toastr.error(fieldErrors.username, 'Validation Error');
      } else if (fieldErrors.password) {
        this.toastr.error(fieldErrors.password, 'Validation Error');
      } else {
        this.toastr.error('Please check your input.', 'Validation Error');
      }
      return;
    }

    // Handle authentication errors (401)
    if (error.status === 401) {
      const message = error.error?.message || 'Invalid username or password.';
      this.toastr.error(message, 'Login Failed');
      return;
    }

    // Handle server errors (500+)
    if (error.status >= 500) {
      this.toastr.error('Server error. Please try again later.', 'Error');
      return;
    }

    // Handle network errors
    if (error.status === 0) {
      this.toastr.error('Cannot connect to server. Please check your connection.', 'Connection Error');
      return;
    }

    // Default error message
    const message = error.error?.message || 'An unexpected error occurred.';
    this.toastr.error(message, 'Error');
  }
}