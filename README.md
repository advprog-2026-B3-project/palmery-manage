# palmery-manage

Backend Spring Boot untuk modul **Manajemen Kebun**, **Hasil Panen**, dan **Pengiriman** MySawit. Berjalan di port **8081** secara default.

## Arsitektur lokal

| Layanan | Port | Peran |
|---------|------|--------|
| **palmery-fe** | 3001 | Aplikasi utama (pengiriman, payment UI, dll.) |
| **palmery-auth** (frontend) | 3000 | Login & registrasi |
| **palmery-auth** (backend) | 8080 | JWT, daftar pengguna |
| **palmery-manage** (repo ini) | 8081 | API kebun, panen, pengiriman |
| PostgreSQL (manage) | 5432 | DB `palmery_manage_dev` (dev) |

Alur autentikasi:

1. Pengguna membuka http://localhost:3001
2. Login → dialihkan ke http://localhost:3000/login?returnUrl=...
3. Setelah sukses → kembali ke http://localhost:3001/auth/callback?token=...
4. JWT disimpan; dashboard dibuka sesuai peran (`SUPIR`, `MANDOR`, `ADMIN`)

## Prasyarat

- Java 21+
- PostgreSQL (untuk profil `dev`)
- Node.js 22+ (untuk frontend)
- Repo terkait di folder saudara:
  - `../palmery-auth`
  - `../palmery-fe`

## Konfigurasi lingkungan

Salin dan samakan **JWT secret** serta **service client** dengan `palmery-auth`:

```bash
# palmery-manage — gunakan di application-dev.properties atau env
JWT_SECRET=replace-this-secret-with-at-least-32-characters
AUTH_API_BASE_URL=http://localhost:8080
AUTH_SERVICE_CLIENT_ID=palmery-internal-service
AUTH_SERVICE_CLIENT_SECRET=replace-with-service-client-secret
```

Di `palmery-auth/backend/.env` gunakan nilai yang sama untuk `AUTH_JWT_SECRET` dan `AUTH_SERVICE_CLIENT_SECRET`.

Profil aktif untuk pengembangan lokal:

```bash
export SPRING_PROFILES_ACTIVE=dev
```

Profil `dev` membuka semua endpoint tanpa JWT (nyaman untuk debug). Profil lain memvalidasi Bearer token dari palmery-auth.

## Menjalankan palmery-manage

```bash
cd palmery-manage

# Pastikan PostgreSQL berjalan dan DB ada (sesuaikan application-dev.properties)
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Health check: http://localhost:8081/actuator/health atau uji `GET http://localhost:8081/api/mandor/panen/siap-angkut`.
Metrik pengiriman tersedia di `/actuator/metrics` dengan prefix `palmery.pengiriman.*`.

## Menjalankan stack lengkap (terminal terpisah)

### 1. Database

```bash
# Contoh: buat database dev
createdb palmery_manage_dev
```

### 2. palmery-auth backend

```bash
cd ../palmery-auth/backend
cp .env.example .env   # edit JWT secret & DB
./gradlew bootRun
# → http://localhost:8080
```

### 3. palmery-auth frontend

```bash
cd ../palmery-auth/frontend
pnpm install   # atau npm install
pnpm dev
# → http://localhost:3000
```

### 4. palmery-manage (repo ini)

```bash
cd ../palmery-manage
./gradlew bootRun --args='--spring.profiles.active=dev'
# → http://localhost:8081
```

### 5. palmery-fe

```bash
cd ../palmery-fe
cp .env.sample .env.local
pnpm install
pnpm dev
# → http://localhost:3001
```

## Uji alur pengiriman + auth

1. **Registrasi Supir** di http://localhost:3000/register — pilih peran Supir Truk (`DRIVER` di backend).
2. **Admin** menugaskan Supir ke kebun (modul kebun): `POST /kebun/{kebunId}/supir` dengan `personnelId` = UUID pengguna dari JWT (`sub` setelah login).
3. **Mandor** ditugaskan ke kebun yang sama.
4. Login sebagai **Mandor** di palmery-fe → **Buat Pengiriman Baru** → dropdown Supir menampilkan **nama** dari palmery-auth.
5. Login sebagai **Supir** → update status pengiriman.

Tanpa penugasan kebun, daftar supir untuk mandor akan kosong meskipun akun sudah terdaftar.

## API pengiriman (ringkas)

| Peran | Endpoint |
|-------|----------|
| Mandor | `GET /api/mandor/drivers`, `GET /api/mandor/panen/siap-angkut`, `POST /api/mandor/pengiriman`, `GET /api/mandor/pengiriman/aktif`, approve/reject |
| Supir | `GET /api/supir/pengiriman/aktif`, `PATCH /api/supir/pengiriman/{id}/status`, riwayat |
| Admin | `GET /api/admin/pengiriman/pending`, approve / reject / partial-reject |

Event payroll: `PengirimanApprovedMandorEvent`, `PengirimanApprovedAdminEvent` (konsumsi modul pembayaran).

## Integrasi palmery-auth

- JWT claim `sub` = UUID akun di tabel `users`.
- JWT claim `role` = `SUPIR` | `MANDOR` | `ADMIN` | `BURUH` (dipetakan dari `DRIVER`, `SUPERVISOR`, dll.).
- `AuthUserClient` memanggil `POST /api/users/by-ids` dengan service token untuk nama supir di dropdown.

## Tests

```bash
./gradlew test
```

## Troubleshooting

| Masalah | Solusi |
|---------|--------|
| Dropdown supir kosong | Pastikan supir di-assign ke kebun yang sama dengan mandor (`plantation_assignments`). |
| Nama supir hanya ID | Cek `AUTH_INTEGRATION_ENABLED=true` dan kredensial service client sama dengan auth. |
| 401 dari palmery-fe | Login ulang; pastikan `JWT_SECRET` sama di auth & manage (profil non-dev). |
| Redirect login gagal | palmery-fe harus di port **3001**, auth di **3000**; cek `returnUrl` ke `/auth/callback`. |
