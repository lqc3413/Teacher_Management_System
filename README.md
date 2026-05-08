# Teacher Management System

## Project Structure

- `backend/` - Spring Boot and Maven backend service.
- `frontend/` - Vue and Vite frontend app.
- `docs/` - Project documents, reports, templates, and exported materials.
- `uploads/` - Runtime uploaded files. Keep this outside frontend and backend source trees.

## Backend

Build the backend:

```powershell
mvn -f backend/pom.xml -DskipTests package
```

Run the backend in development:

```powershell
cd backend
mvn spring-boot:run
```

Run the packaged jar:

```powershell
backend\run_backend.bat
```

## Frontend

Run the frontend dev server:

```powershell
cd frontend
npm run dev
```

Build the frontend:

```powershell
cd frontend
npm run build
```

## Repository Notes

- SQL scripts live in `backend/sql/`.
- Helper scripts live in `backend/scripts/`.
- Generated build outputs such as `backend/target/` and `frontend/dist/` should not be committed.
- Temporary files such as `tmp_*`, `pdf_*`, and `*.log` should not be committed.
