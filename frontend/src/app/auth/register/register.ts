import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule
  ],
  templateUrl: './register.html',
  styleUrl: './register.scss'
})
export class Register {
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
    
    this.authService.register(this.model).subscribe({
      next: (response) => {
        console.log(response);
        
        this.toastr.success('You can now log in with your new account.', 'Registration Successful!');
        this.router.navigate(['/login']);
      },
      error: (error) => {
        console.log(error);
        
        this.isSubmitting = false;
        this.handleRegistrationError(error);
      }
    });
  }

  private handleRegistrationError(error: any): void {
    if (error.status === 400 && error.error?.fieldErrors) {
      const fieldErrors = error.error.fieldErrors;
      
      const firstError = Object.values(fieldErrors)[0] as string;
      this.toastr.error(firstError, 'Validation Error');
      return;
    }

    if (error.status === 409) {
      const message = error.error?.message || 'Username or email already exists.';
      this.toastr.error(message, 'Registration Failed');
      return;
    }

    if (error.status === 403) {
      const message = error.error?.message || 'Registration is not allowed at this time.';
      this.toastr.error(message, 'Registration Failed');
      return;
    }

    if (error.status >= 500) {
      this.toastr.error('Server error. Please try again later.', 'Error');
      return;
    }

    if (error.status === 0) {
      this.toastr.error('Cannot connect to server. Please check your connection.', 'Connection Error');
      return;
    }

    const message = error.error?.message || 'An unexpected error occurred during registration.';
    this.toastr.error(message, 'Registration Failed');
  }
}