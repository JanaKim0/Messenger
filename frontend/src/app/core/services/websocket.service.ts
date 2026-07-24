import { Injectable, signal } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import { Subject } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DeliveredReceipt, Message, ReadReceipt } from '../models/chat.model';

/**
 * Manages the STOMP-over-WebSocket connection and exposes real-time streams:
 * incoming messages, read receipts and delivery receipts.
 */
@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private client: Client | null = null;

  readonly connected = signal(false);

  readonly messages$ = new Subject<Message>();
  readonly reads$ = new Subject<ReadReceipt>();
  readonly deliveries$ = new Subject<DeliveredReceipt>();

  connect(token: string): void {
    if (this.client?.active) {
      return;
    }

    this.client = new Client({
      brokerURL: environment.wsUrl,
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 4000,
      onConnect: () => {
        this.connected.set(true);
        this.client?.subscribe('/user/queue/messages', (frame: IMessage) =>
          this.messages$.next(JSON.parse(frame.body) as Message),
        );
        this.client?.subscribe('/user/queue/read', (frame: IMessage) =>
          this.reads$.next(JSON.parse(frame.body) as ReadReceipt),
        );
        this.client?.subscribe('/user/queue/delivered', (frame: IMessage) =>
          this.deliveries$.next(JSON.parse(frame.body) as DeliveredReceipt),
        );
      },
      onDisconnect: () => this.connected.set(false),
      onWebSocketClose: () => this.connected.set(false),
    });

    this.client.activate();
  }

  disconnect(): void {
    this.client?.deactivate();
    this.client = null;
    this.connected.set(false);
  }
}
