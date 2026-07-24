import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Conversation, Message } from '../models/chat.model';
import { UserSummary } from '../models/user.model';

/**
 * REST access to users, conversations and messages.
 */
@Injectable({ providedIn: 'root' })
export class ChatService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  searchUsers(term: string): Observable<UserSummary[]> {
    return this.http.get<UserSummary[]>(`${this.api}/users/search`, { params: { q: term } });
  }

  listConversations(): Observable<Conversation[]> {
    return this.http.get<Conversation[]>(`${this.api}/conversations`);
  }

  openConversation(userId: number): Observable<Conversation> {
    return this.http.post<Conversation>(`${this.api}/conversations/with/${userId}`, {});
  }

  history(conversationId: number): Observable<Message[]> {
    return this.http.get<Message[]>(`${this.api}/conversations/${conversationId}/messages`);
  }

  send(conversationId: number, content: string): Observable<Message> {
    return this.http.post<Message>(`${this.api}/conversations/${conversationId}/messages`, {
      content,
    });
  }
}
