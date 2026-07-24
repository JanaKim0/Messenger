import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-chats',
  imports: [],
  templateUrl: './chats.html',
  styleUrl: './chats.scss',
})
export class Chats {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly user = this.auth.currentUser;
  readonly isAdmin = this.auth.isAdmin;

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
