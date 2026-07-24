import { Component, computed, input } from '@angular/core';

/**
 * Circular avatar: shows the user's photo when available, otherwise their
 * initials on a soft-pink background.
 */
@Component({
  selector: 'app-avatar',
  imports: [],
  template: `
    @if (photo()) {
      <img class="avatar" [style.width.px]="size()" [style.height.px]="size()" [src]="photo()" alt="" />
    } @else {
      <span
        class="avatar avatar-initials"
        [style.width.px]="size()"
        [style.height.px]="size()"
        [style.font-size.px]="size() * 0.4"
        >{{ initials() }}</span
      >
    }
  `,
  styles: [
    `
      .avatar {
        border-radius: 50%;
        object-fit: cover;
        flex-shrink: 0;
        display: inline-flex;
        align-items: center;
        justify-content: center;
      }
      .avatar-initials {
        background: var(--pink-200);
        color: var(--teal-700);
        font-weight: 700;
        text-transform: uppercase;
      }
    `,
  ],
})
export class Avatar {
  readonly firstName = input<string>('');
  readonly lastName = input<string>('');
  readonly photo = input<string | null>(null);
  readonly size = input<number>(44);

  readonly initials = computed(() => {
    const f = this.firstName().charAt(0);
    const l = this.lastName().charAt(0);
    return (f + l) || '?';
  });
}
