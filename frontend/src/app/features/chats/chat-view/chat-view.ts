import {
  AfterViewInit,
  Component,
  ElementRef,
  effect,
  input,
  output,
  viewChild,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Conversation, Message } from '../../../core/models/chat.model';
import { Avatar } from '../../../shared/avatar/avatar';

@Component({
  selector: 'app-chat-view',
  imports: [Avatar, FormsModule],
  templateUrl: './chat-view.html',
  styleUrl: './chat-view.scss',
})
export class ChatView implements AfterViewInit {
  readonly conversation = input<Conversation | null>(null);
  readonly messages = input<Message[]>([]);
  readonly currentUserId = input.required<number>();
  readonly send = output<string>();

  private readonly scrollBox = viewChild<ElementRef<HTMLElement>>('scrollBox');

  draft = '';

  constructor() {
    // Scroll to the newest message whenever the list changes.
    effect(() => {
      this.messages();
      queueMicrotask(() => this.scrollToBottom());
    });
  }

  ngAfterViewInit(): void {
    this.scrollToBottom();
  }

  isMine(message: Message): boolean {
    return message.senderId === this.currentUserId();
  }

  submit(): void {
    const text = this.draft.trim();
    if (text.length === 0) {
      return;
    }
    this.send.emit(text);
    this.draft = '';
  }

  formatTime(iso: string): string {
    return new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  private scrollToBottom(): void {
    const el = this.scrollBox()?.nativeElement;
    if (el) {
      el.scrollTop = el.scrollHeight;
    }
  }
}
