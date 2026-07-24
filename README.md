# SitApp

A WhatsApp-inspired messenger built with **Angular** and **Spring Boot**.

Users can register, sign in, manage their profile, search for other users, and
have private one-to-one conversations with real-time message delivery. New
registrations are moderated by an administrator before an account becomes active.

> Status: **in development** — being rebuilt from scratch, stage by stage.

## Features

- ✔ User registration
- ✔ Login / Logout
- ✔ User approval by administrator
- ✔ User search
- ✔ Private conversations
- ✔ Sending text messages (real-time via WebSocket)
- ✔ Delivery status
- ✔ Conversation list with sorting by last message
- ✔ Unread messages
- ✔ Profile editing (name, surname, phone, email, photo)
- ✔ Password changing

## Tech Stack

**Frontend**
- Angular
- TypeScript

**Backend**
- Spring Boot
- Spring Security (JWT)
- Spring Data JPA
- WebSocket (STOMP)

**Database**
- PostgreSQL (production profile)
- H2 (embedded, for local development)

## Project Structure

```
Messenger/
├── backend/    # Spring Boot REST API + WebSocket
└── frontend/   # Angular single-page application
```

## Getting Started

_Setup instructions will be added as the backend and frontend are built._

## Design

Clean, minimal UI in a **white + soft pink** palette.

---

<!-- Acknowledgements block is added at the end of the project. -->
