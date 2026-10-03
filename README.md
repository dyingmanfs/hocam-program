# Hocam Program

An academic Android and PHP/MySQL project for managing students, lessons, scheduled lesson slots, attendance, and payment records.

## Stack and structure
- `Frontend/Java/`: Android activity source in Java.
- `Frontend/layout/`: Android XML layouts.
- `Backend/`: PHP endpoints using MySQL.

## Features represented in the source
Login, teacher panel, student creation/update/deletion, lesson and slot management, attendance updates, and paid/unpaid payment views.

## Setup requirements
This repository is a **source snapshot**, not a complete buildable Android Studio project. Gradle configuration, AndroidManifest.xml, complete resources, and a database schema/export are absent from the supplied files. Restore those files from the original project before attempting a full build.

Place the PHP endpoints under `hocamprogram_api` on a local PHP/MySQL server. Configure the server process environment with `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, and `DB_PASSWORD`. PHP reads these with `getenv()`; it does not automatically load a `.env` file. No database password is included.

The Android source currently targets `http://10.0.2.2/hocamprogram_api/`, the emulator's host-machine address. Update endpoint URLs for a physical device or another server. `PaymentActivity.java` references `get_paid_payments.php`, which is absent; `get_student_paid_payments.php` is present and compatibility needs checking.

## Scope
Academic prototype. Authentication, authorization, and transport security require further review before deployment. End-to-end behavior has not been validated from this incomplete snapshot.

## Attribution
The supplied project materials identify Fatih Sağlam and Furkan Sağlam as collaborators. This repository preserves the shared coursework source; individual contribution boundaries are not asserted.
