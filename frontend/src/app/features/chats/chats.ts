import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Conversation, Message } from '../../core/models/chat.model';
import { AuthService } from '../../core/services/auth.service';
import { ChatService } from '../../core/services/chat.service';
import { WebSocketService } from '../../core/services/websocket.service';
import { ChatView } from './chat-view/chat-view';
import { ConversationList } from './conversation-list/conversation-list';
import { UserSearch } from './user-search/user-search';

/**
 * The messenger screen: a sidebar (search + conversation list) and the active
 * chat. Real-time updates arrive through {@link WebSocketService}.
 */
@Component({
  selector: 'app-chats',
  imports: [UserSearch, ConversationList, ChatView],
  templateUrl: './chats.html',
  styleUrl: './chats.scss',
})
export class Chats implements OnInit {
  private readonly chat = inject(ChatService);
  private readonly ws = inject(WebSocketService);
  private readonly auth = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  readonly conversations = signal<Conversation[]>([]);
  readonly selectedId = signal<number | null>(null);
  readonly messages = signal<Message[]>([]);

  readonly selected = computed(
    () => this.conversations().find((c) => c.id === this.selectedId()) ?? null,
  );
  readonly currentUserId = computed(() => this.auth.currentUser()?.id ?? 0);

  constructor() {
    this.ws.messages$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((m) => this.onIncoming(m));
    this.ws.reads$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((r) => this.onRead(r.conversationId));
    this.ws.deliveries$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((d) => this.onDelivered(d.conversationId));
  }

  ngOnInit(): void {
    this.loadConversations();
  }

  // ---------------- data loading ----------------

  private loadConversations(): void {
    this.chat.listConversations().subscribe((list) => this.conversations.set(this.sorted(list)));
  }

  select(conversation: Conversation): void {
    this.selectedId.set(conversation.id);
    this.patch(conversation.id, { unreadCount: 0 });
    this.chat.history(conversation.id).subscribe((msgs) => this.messages.set(msgs));
  }

  openUser(userId: number): void {
    this.chat.openConversation(userId).subscribe((conversation) => {
      if (!this.conversations().some((c) => c.id === conversation.id)) {
        this.conversations.update((list) => this.sorted([conversation, ...list]));
      }
      this.select(conversation);
    });
  }

  sendMessage(text: string): void {
    const id = this.selectedId();
    if (id === null) {
      return;
    }
    this.chat.send(id, text).subscribe((message) => {
      this.messages.update((list) => [...list, message]);
      this.patch(id, { lastMessage: message.content, lastMessageAt: message.createdAt });
      this.conversations.update((list) => this.sorted(list));
    });
  }

  // ---------------- real-time handlers ----------------

  private onIncoming(message: Message): void {
    const openId = this.selectedId();
    if (message.conversationId === openId) {
      // Reload history so the incoming message is marked read on the server.
      this.chat.history(openId).subscribe((msgs) => this.messages.set(msgs));
      this.patch(openId, {
        lastMessage: message.content,
        lastMessageAt: message.createdAt,
        unreadCount: 0,
      });
      this.conversations.update((list) => this.sorted(list));
      return;
    }

    const known = this.conversations().find((c) => c.id === message.conversationId);
    if (known) {
      this.patch(message.conversationId, {
        lastMessage: message.content,
        lastMessageAt: message.createdAt,
        unreadCount: known.unreadCount + 1,
      });
      this.conversations.update((list) => this.sorted(list));
    } else {
      // First message of a brand-new conversation — refresh the whole list.
      this.loadConversations();
    }
  }

  private onRead(conversationId: number): void {
    if (this.selectedId() !== conversationId) {
      return;
    }
    this.messages.update((list) =>
      list.map((m) =>
        m.senderId === this.currentUserId() && m.status !== 'READ' ? { ...m, status: 'READ' } : m,
      ),
    );
  }

  private onDelivered(conversationId: number): void {
    if (this.selectedId() !== conversationId) {
      return;
    }
    this.messages.update((list) =>
      list.map((m) =>
        m.senderId === this.currentUserId() && m.status === 'SENT'
          ? { ...m, status: 'DELIVERED' }
          : m,
      ),
    );
  }

  // ---------------- helpers ----------------

  private patch(id: number, changes: Partial<Conversation>): void {
    this.conversations.update((list) =>
      list.map((c) => (c.id === id ? { ...c, ...changes } : c)),
    );
  }

  private sorted(list: Conversation[]): Conversation[] {
    return [...list].sort(
      (a, b) => new Date(b.lastMessageAt).getTime() - new Date(a.lastMessageAt).getTime(),
    );
  }
}
