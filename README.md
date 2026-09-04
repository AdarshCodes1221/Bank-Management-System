# Bank Management System

A desktop-based banking application built with **Java Swing, JDBC, and MySQL**. The project provides a graphical interface for managing basic banking operations, user authentication, account registration, deposits, withdrawals, fast cash, balance enquiries, PIN management, and mini statements.

> **Note:** This project is developed for educational and demonstration purposes and is not intended for production banking use.

---

## Features

- User account registration with a multi-step signup process
- User login and authentication
- SHA-256 PIN hashing
- Deposit money
- Withdraw money
- Fast Cash withdrawal
- Balance enquiry
- Mini statement
- Change PIN
- MySQL database integration using JDBC
- Prepared statements for database operations
- Input validation
- Email/OTP authentication components
- Java Swing graphical user interface
- Environment-based database configuration

---

## Tech Stack

| Technology | Usage |
|---|---|
| **Java** | Core application development |
| **Java Swing** | Graphical User Interface |
| **JDBC** | Database connectivity |
| **MySQL** | Database management |
| **IntelliJ IDEA** | Development environment |
| **Git & GitHub** | Version control |

---

## Application Workflow

```text
                    ┌──────────────┐
                    │    Login     │
                    └──────┬───────┘
                           │
              ┌────────────┴────────────┐
              │                         │
        Existing User              New User
              │                         │
              ▼                         ▼
       Authentication                Signup
              │                    ┌────┴────┐
              │                    │         │
              │                 Signup 2  Signup 3
              │                    │         │
              │                    └────┬────┘
              │                         │
              │                         ▼
              │                       Login
              │
              └──────────────┬──────────────
                             │
                             ▼
                    ┌──────────────────┐
                    │  Main Dashboard  │
                    └────────┬─────────┘
                             │
       ┌──────────┬──────────┼──────────┬─────────────┐
       ▼          ▼          ▼          ▼             ▼
    Deposit   Withdraw   Fast Cash   Balance     Mini Statement
                                                   
                             │
                             ▼
                         Change PIN
