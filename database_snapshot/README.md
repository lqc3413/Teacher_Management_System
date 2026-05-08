# Database Snapshot

This folder contains a MySQL snapshot for the Teacher Management System.

## File

- `teacher_mgmt_snapshot_2026-05-08.sql`

The SQL file includes:

- `CREATE DATABASE teacher_mgmt`
- table definitions
- current data from the local development database

## Import

Open PowerShell or CMD in this folder, then run:

```powershell
mysql -uroot -p --default-character-set=utf8mb4 < teacher_mgmt_snapshot_2026-05-08.sql
```

Enter the local MySQL password when prompted.

After import, update the backend database username and password in:

```text
backend/src/main/resources/application.yml
```

Then start the backend normally.
