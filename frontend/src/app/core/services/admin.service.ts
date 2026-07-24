import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/admin`;

  pending(): Observable<User[]> {
    return this.http.get<User[]>(`${this.baseUrl}/users/pending`);
  }

  approve(userId: number): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/users/${userId}/approve`, {});
  }

  reject(userId: number): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/users/${userId}/reject`, {});
  }
}
