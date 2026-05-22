# Agents Guidelines - Palmery

---

## Tujuan

- Menjaga kualitas kode tetap **_clean_, _readable_, dan _maintainable_**
- Menerapkan **_best practices_** dalam _software engineering_
- Memastikan **konsistensi struktur dan _style_**
- Mempermudah kolaborasi tim
- Menghindari technical debt

---

## General Principles

### 1. Clean Code

Ikuti prinsip berikut:

- Gunakan **nama variabel, fungsi, dan class yang deskriptif**
- Satu fungsi → **satu tanggung jawab**
- Hindari:
    - magic number
    - duplikasi kode
    - fungsi terlalu panjang
- Prioritaskan **_readability over cleverness_**

---

### 2. SOLID Principles

Pastikan implementasi mengikuti:

- Single Responsibility Principle
- Open/Closed Principle
- Liskov Substitution Principle
- Interface Segregation Principle
- Dependency Inversion Principle

---

### 3. Design & Architecture

- Gunakan **layered architecture**
- Pisahkan:
    - business logic
    - data access
    - interface / controller
- Gunakan **design patterns** jika relevan (Factory, Strategy, Observer, dll)

---

## Code Style & Formatting

- Ikuti formatter default bahasa pemrograman
- Gunakan:
    - Java → Google Java Style Guide
    - Python → PEP8
    - JavaScript → ESLint + Prettier

---

## Pull Request Rules

- Lolos semua test
- Tidak menurunkan _coverage_
- Sudah direview oleh QA
- Tidak mengandung:
  - dead code
  - debug print
  - hard-coded value

---