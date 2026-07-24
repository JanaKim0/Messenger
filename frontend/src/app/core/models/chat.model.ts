import { UserSummary } from './user.model';

export type MessageStatus = 'SENT' | 'DELIVERED' | 'READ';

export interface Conversation {
  id: number;
  otherUser: UserSummary;
  lastMessage: string | null;
  lastMessageAt: string;
  unreadCount: number;
}

export interface Message {
  id: number;
  conversationId: number;
  senderId: number;
  senderUsername: string;
  content: string;
  status: MessageStatus;
  createdAt: string;
}

/** Pushed over WebSocket when the recipient reads a conversation. */
export interface ReadReceipt {
  conversationId: number;
  readerId: number;
}

/** Pushed over WebSocket when a message transitions to DELIVERED. */
export interface DeliveredReceipt {
  conversationId: number;
  recipientId: number;
}
