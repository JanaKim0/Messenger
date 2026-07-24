import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User } from '../models/user.model';

export interface UpdateProfilePayload {
  firstName: string;
  lastName: string;
  phone?: string | null;
  email: string;
  photo?: string | null;
}

export interface ChangePasswordPayload {
  currentPassword: string;
  newPassword: string;
}

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/profile`;

  get(): Observable<User> {
    return this.http.get<User>(this.baseUrl);
  }

  update(payload: UpdateProfilePayload): Observable<User> {
    return this.http.put<User>(this.baseUrl, payload);
  }

  changePassword(payload: ChangePasswordPayload): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/password`, payload);
  }
}
