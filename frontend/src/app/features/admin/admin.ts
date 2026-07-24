import { Component, OnInit, inject, signal } from '@angular/core';
import { User } from '../../core/models/user.model';
import { AdminService } from '../../core/services/admin.service';
import { Avatar } from '../../shared/avatar/avatar';

@Component({
  selector: 'app-admin',
  imports: [Avatar],
  templateUrl: './admin.html',
  styleUrl: './admin.scss',
})
export class Admin implements OnInit {
  private readonly adminService = inject(AdminService);

  readonly pending = signal<User[]>([]);
  readonly loading = signal(true);
  readonly busyId = signal<number | null>(null);

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.adminService.pending().subscribe({
      next: (users) => {
        this.pending.set(users);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  approve(user: User): void {
    this.busyId.set(user.id);
    this.adminService.approve(user.id).subscribe({
      next: () => this.remove(user.id),
      error: () => this.busyId.set(null),
    });
  }

  reject(user: User): void {
    this.busyId.set(user.id);
    this.adminService.reject(user.id).subscribe({
      next: () => this.remove(user.id),
      error: () => this.busyId.set(null),
    });
  }

  private remove(id: number): void {
    this.pending.update((list) => list.filter((u) => u.id !== id));
    this.busyId.set(null);
  }
}
