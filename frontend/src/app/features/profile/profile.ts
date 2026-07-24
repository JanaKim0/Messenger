import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { ProfileService } from '../../core/services/profile.service';
import { Avatar } from '../../shared/avatar/avatar';

const MAX_PHOTO_BYTES = 1_000_000; // ~1 MB

@Component({
  selector: 'app-profile',
  imports: [ReactiveFormsModule, Avatar],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
})
export class Profile implements OnInit {
  private readonly profileService = inject(ProfileService);
  private readonly auth = inject(AuthService);

  readonly photo = signal<string | null>(null);
  readonly savedProfile = signal(false);
  readonly profileError = signal<string | null>(null);
  readonly savingProfile = signal(false);

  readonly savedPassword = signal(false);
  readonly passwordError = signal<string | null>(null);
  readonly savingPassword = signal(false);

  readonly profileForm = new FormGroup({
    firstName: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    lastName: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    phone: new FormControl('', { nonNullable: true }),
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
  });

  readonly passwordForm = new FormGroup({
    currentPassword: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    newPassword: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(6)],
    }),
  });

  ngOnInit(): void {
    this.profileService.get().subscribe((user) => {
      this.profileForm.patchValue({
        firstName: user.firstName,
        lastName: user.lastName,
        phone: user.phone ?? '',
        email: user.email,
      });
      this.photo.set(user.photo);
      this.auth.setCurrentUser(user);
    });
  }

  onPhotoSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }
    if (file.size > MAX_PHOTO_BYTES) {
      this.profileError.set('Please choose an image smaller than 1 MB.');
      return;
    }
    const reader = new FileReader();
    reader.onload = () => this.photo.set(reader.result as string);
    reader.readAsDataURL(file);
  }

  removePhoto(): void {
    this.photo.set(null);
  }

  saveProfile(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }
    this.savingProfile.set(true);
    this.profileError.set(null);
    this.savedProfile.set(false);

    const value = this.profileForm.getRawValue();
    this.profileService
      .update({
        firstName: value.firstName,
        lastName: value.lastName,
        phone: value.phone.trim() || null,
        email: value.email,
        photo: this.photo(),
      })
      .subscribe({
        next: (user) => {
          this.auth.setCurrentUser(user);
          this.savingProfile.set(false);
          this.savedProfile.set(true);
        },
        error: (err) => {
          this.profileError.set(err?.error?.message ?? 'Could not save your profile.');
          this.savingProfile.set(false);
        },
      });
  }

  changePassword(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }
    this.savingPassword.set(true);
    this.passwordError.set(null);
    this.savedPassword.set(false);

    this.profileService.changePassword(this.passwordForm.getRawValue()).subscribe({
      next: () => {
        this.savingPassword.set(false);
        this.savedPassword.set(true);
        this.passwordForm.reset();
      },
      error: (err) => {
        this.passwordError.set(err?.error?.message ?? 'Could not change your password.');
        this.savingPassword.set(false);
      },
    });
  }
}
