import { Component, ElementRef, inject, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs';
import { ChatService } from '../../../core/services/chat.service';
import { UserSummary } from '../../../core/models/user.model';
import { Avatar } from '../../../shared/avatar/avatar';

/**
 * Search box that looks up approved users as you type and emits the selected
 * user's id so a conversation can be opened.
 */
@Component({
  selector: 'app-user-search',
  imports: [ReactiveFormsModule, Avatar],
  templateUrl: './user-search.html',
  styleUrl: './user-search.scss',
})
export class UserSearch {
  private readonly chat = inject(ChatService);
  private readonly host = inject(ElementRef<HTMLElement>);

  readonly open = output<number>();

  readonly control = new FormControl('', { nonNullable: true });
  readonly results = signal<UserSummary[]>([]);
  readonly searching = signal(false);

  constructor() {
    this.control.valueChanges
      .pipe(
        debounceTime(250),
        distinctUntilChanged(),
        switchMap((term) => {
          const q = term.trim();
          if (q.length === 0) {
            this.results.set([]);
            return [];
          }
          this.searching.set(true);
          return this.chat.searchUsers(q);
        }),
        takeUntilDestroyed(),
      )
      .subscribe({
        next: (users) => {
          this.results.set(users);
          this.searching.set(false);
        },
        error: () => this.searching.set(false),
      });
  }

  choose(user: UserSummary): void {
    this.open.emit(user.id);
    this.control.setValue('');
    this.results.set([]);
  }
}
