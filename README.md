# ☕ CafeBook — Cafe Seat Booking System

A full-stack cafe reservation system. Customers browse available tables by zone, book a seat instantly, and receive email confirmation — no phone calls needed. The cafe owner manages everything from a live admin dashboard.

---

## Features

**For Customers**
- Register, verify email, and sign in securely
- Browse available tables by date, time, and party size
- Choose a seating zone (Window, Patio, Lounge, Private Nook, and more)
- Book, view, and cancel reservations online
- Receive automatic email confirmation and status updates
- Change password or delete account from the dashboard

**For Admin**
- View all reservations in one dashboard
- Update booking status — Confirm, Complete, No-Show
- Add, enable/disable, or remove tables
- Every status change automatically emails the customer

---

## Project Structure

```
cafe-seat-booking/
├── docs/
│   └── index.html          ← GitHub Pages project site
├── frontend/
│   ├── index.html          ← Landing page
│   ├── auth.html           ← Login & Register
│   ├── book.html           ← Make a reservation
│   ├── my-bookings.html    ← Customer dashboard
│   ├── admin.html          ← Admin dashboard
│   ├── verify-email.html   ← Email verification
│   ├── reset-password.html ← Password reset
│   ├── css/style.css
│   └── js/api.js
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/restaurant/booking/
│       │   ├── config/         ← JWT filter, Security config
│       │   ├── controller/     ← REST API endpoints
│       │   ├── model/          ← JPA entities
│       │   ├── repository/     ← Data access layer
│       │   └── service/        ← Business logic
│       └── test/               ← Unit tests (JUnit 5 + Mockito)
├── Start CafeBook.bat      ← One-click launcher (Windows)
└── README.md
```

---

## Tech Stack

| Layer    | Technology                              |
|----------|-----------------------------------------|
| Backend  | Java 21, Spring Boot 3.2, Spring Security |
| Auth     | JWT (jjwt 0.11.5), BCrypt               |
| Database | H2 (local), PostgreSQL (production)     |
| Email    | JavaMailSender, Gmail SMTP              |
| Frontend | HTML5, CSS3, Vanilla JavaScript         |
| Tests    | JUnit 5, Mockito                        |

---

## Getting Started

### Prerequisites
- Java 21+
- Maven 3.8+

### Run locally

```bash
cd backend
mvn spring-boot:run
```

Then open your browser at `http://localhost:8080`

Or double-click **Start CafeBook.bat** on Windows.

### Default admin account
```
Email:    admin@cafebook.com
Password: admin123
```

---

## Email Setup (Optional)

Email is disabled by default. To enable, set these environment variables:

```properties
MAIL_ENABLED=true
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
APP_BASE_URL=http://localhost:8080
```

---

## Seating Zones

| Zone           | Tables  | Capacity | Best For                  |
|----------------|---------|----------|---------------------------|
| Window Side    | W1, W2  | 1–4      | Solo visits, couples      |
| Cosy Corner    | CC1, CC2| 1–2      | Quiet work, catch-ups     |
| Outdoor Patio  | OP1, OP2| 2–6      | Fresh air, casual groups  |
| Private Nook   | PN1, PN2| 2–6      | Meetings, private talks   |
| Main Floor     | MF1, MF2| 1–4      | Everyday visits           |
| Lounge         | L1, L2  | 2–8      | Groups, longer stays      |

---

## License

MIT License — free to use and modify.
