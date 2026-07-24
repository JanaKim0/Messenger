import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { WebSocketService } from '../../core/services/websocket.service';
import { Avatar } from '../../shared/avatar/avatar';

/**
 * Authenticated layout: top navigation bar plus a routed content area.
 * Opens the WebSocket connection for the whole authenticated session.
 */
@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, Avatar],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',
})
export class Shell implements OnInit, OnDestroy {
  private readonly auth = inject(AuthService);
  private readonly ws = inject(WebSocketService);
  private readonly router = inject(Router);

  readonly user = this.auth.currentUser;
  readonly isAdmin = this.auth.isAdmin;

  ngOnInit(): void {
    const token = this.auth.token;
    if (token) {
      this.ws.connect(token);
    }
  }

  ngOnDestroy(): void {
    this.ws.disconnect();
  }

  logout(): void {
    this.ws.disconnect();
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
