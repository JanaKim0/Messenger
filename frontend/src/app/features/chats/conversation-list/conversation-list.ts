import { Component, input, output } from '@angular/core';
import { Conversation } from '../../../core/models/chat.model';
import { Avatar } from '../../../shared/avatar/avatar';

@Component({
  selector: 'app-conversation-list',
  imports: [Avatar],
  templateUrl: './conversation-list.html',
  styleUrl: './conversation-list.scss',
})
export class ConversationList {
  readonly conversations = input.required<Conversation[]>();
  readonly selectedId = input<number | null>(null);
  readonly select = output<Conversation>();

  /** Short time label: HH:mm for today, otherwise a day/month date. */
  formatTime(iso: string): string {
    const date = new Date(iso);
    const now = new Date();
    const sameDay =
      date.getFullYear() === now.getFullYear() &&
      date.getMonth() === now.getMonth() &&
      date.getDate() === now.getDate();
    return sameDay
      ? date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      : date.toLocaleDateString([], { day: '2-digit', month: '2-digit' });
  }
}
