# UTS Pengujian Perangkat Lunak  
**Nama**: Dimas Angka Wijaya  
**NIM**: 230309006  
**Kelas**: 3A – DIV Rekayasa Keamanan Siber  
**Politeknik Negeri Cilacap**

## 📌 Deskripsi
Unit testing untuk Sistem Informasi Akademik (SIAKAD) menggunakan:
- **JUnit 5**
- **Mockito** (untuk `EnrollmentService`)
- **JaCoCo** (code coverage)

## ✅ Hasil
- Semua test **PASS**
- Coverage:
  - `GradeCalculator`: **100%**
  - `EnrollmentService`: **100%**
  - Overall: **72%**

> ⚠️ Overall coverage dipengaruhi oleh class pendukung (`model`, `exception`) yang tidak diuji secara khusus.  
> Target **80% berlaku per class yang diuji** — **sudah tercapai**.
