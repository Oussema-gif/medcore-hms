# MedCore HMS

> Hospital Management System for modern clinical, administrative, pharmacy, billing, and patient workflows.

MedCore HMS is a full-stack Hospital Management System designed as a production-oriented software project rather than a UI-only prototype. The project combines a secured Java/Spring backend with a modern React frontend and focuses on maintainable architecture, role-based access control, validation, clinical workflows, and a professional workspace-oriented interface.

## Project Overview

MedCore HMS provides a centralized platform for managing hospital operations across multiple user roles. The application is structured around real operational workflows such as patient registration, doctor management, appointments, medical records, prescriptions, pharmacy inventory, invoices, and reporting.

The project follows the principle that frontend permissions and navigation should reflect the same access boundaries enforced by the backend security layer. The documented role model includes Admin, Doctor, Receptionist, Pharmacist, and Patient personas. fileciteturn62file0L11-L22

## Main Capabilities

### Administration

- Dashboard and operational overview
- User and access management
- Doctors and departments
- Patients
- Appointments
- Billing and invoices
- Pharmacy inventory
- Reports
- Audit logs
- Settings

### Clinical workflows

- Patient clinical workspace
- Medical records
- Vitals
- Diagnoses
- Laboratory requests
- Prescriptions
- Clinical billing context
- Doctor availability and weekly scheduling

The frontend clinical workspace is organized into reusable modules and clinical tabs for overview, vitals, records, laboratory, prescriptions, and billing. fileciteturn63file9L941-L1017

### Doctor management

The Doctor module supports:

- Doctor directory and pagination
- Doctor profile/details
- Create doctor
- Edit doctor
- Doctor deactivation
- Doctor availability management
- Weekly availability windows
- Consultation duration configuration

Doctor creation and editing use validated form schemas, including positive identifiers, registration number validation, names, optional contact data, and consultation duration validation. fileciteturn63file0L38-L69 fileciteturn63file2L216-L251

### Appointment management

Appointment workflows include creation, check-in, start, completion, and cancellation. Appointment creation validates patient, doctor, department, future start/end timestamps, reason, and scheduling notes. The backend also contains doctor-conflict detection based on overlapping appointment windows. fileciteturn63file4L452-L487 fileciteturn63file4L529-L557

## Technology Stack

### Backend

- Java 21
- Spring Boot
- Spring Framework
- Spring Security
- Spring Data JPA
- JWT authentication
- Flyway database migrations
- MySQL connector
- MapStruct
- Springdoc OpenAPI / Swagger
- Lombok
- Spring Boot Actuator
- Spring AOP
- JUnit / Spring Boot Test
- Spring Security Test
- Testcontainers

The backend dependency set includes Flyway, MySQL, JWT, Springdoc OpenAPI, actuator/AOP support, and Testcontainers-based testing. fileciteturn63file5L568-L653

### Frontend

- React
- TypeScript
- Vite
- React Router
- TanStack Query
- React Hook Form
- Zod
- Axios
- Lucide React

The frontend uses TanStack Query for server state, React Hook Form plus Zod for form state and validation, and reusable feature-oriented components. fileciteturn63file0L25-L36

## Architecture

The project follows a feature-oriented full-stack structure with clear separation between presentation, API access, domain behavior, persistence, security, and infrastructure concerns.

### Frontend structure

A representative feature structure looks like:

```text
frontend/
└── src/
    ├── api/
    ├── components/
    ├── features/
    │   ├── doctors/
    │   ├── patients/
    │   ├── appointments/
    │   ├── medical-records/
    │   ├── vitals/
    │   ├── diagnoses/
    │   ├── laboratory/
    │   ├── prescriptions/
    │   └── invoices/
    ├── routes/
    ├── types/
    └── ...
```

### Backend structure

The backend is organized around domain modules and application services, with repositories, DTOs, validation, security, and exception handling separated from the web layer.

```text
backend/
└── src/
    ├── main/
    │   ├── java/com/medcore/hms/
    │   │   ├── auth/
    │   │   ├── appointment/
    │   │   ├── doctor/
    │   │   ├── patient/
    │   │   ├── medicalrecord/
    │   │   ├── prescription/
    │   │   ├── pharmacy/
    │   │   ├── billing/
    │   │   ├── security/
    │   │   ├── exception/
    │   │   └── ...
    │   └── resources/
    │       ├── application.yml
    │       └── db/migration/
    └── test/
```

## Security Model

Security is a first-class architectural concern in MedCore HMS.

The application is designed around role-based access control, JWT-based authentication, server-side authorization boundaries, validation, and consistent API error handling.

Frontend navigation is intentionally role-scoped so the interface does not advertise operations that the backend would reject. The project design explicitly mirrors backend `@PreAuthorize` boundaries at the UI level. fileciteturn62file0L21-L22

Destructive operations use explicit confirmation dialogs that identify the exact entity being modified. This pattern is intended to reduce accidental actions in clinical and financial workflows. fileciteturn62file0L24-L31

## API and Error Handling

The backend exposes REST-style APIs and uses DTO validation for incoming requests. Centralized exception handling is implemented with a `@RestControllerAdvice`-based global exception handler and dedicated API/business exception types. fileciteturn63file8L824-L925

Examples of handled application errors include:

- Bad requests
- Validation failures
- Forbidden operations
- Business-rule violations
- Conflicts
- Authentication/authorization failures
- Data integrity errors

## Database and Migrations

Database schema evolution is managed through Flyway migrations instead of relying on implicit runtime schema generation.

This approach provides:

- Versioned database changes
- Repeatable deployments
- Controlled schema evolution
- Environment consistency
- Easier rollback planning and release auditing

## Frontend State Management

TanStack Query is used for server-state fetching and cache invalidation. Doctor detail data, for example, is loaded through query keys scoped to the doctor identifier, while mutations invalidate the appropriate doctor/list queries after successful writes. The same pattern is used throughout the project for clinical resources. fileciteturn63file0L71-L127

## Validation

Zod schemas are used at the frontend boundary to prevent invalid data from being submitted to the backend.

Typical validation rules include:

- Positive numeric identifiers
- Required clinical fields
- Maximum string lengths
- Valid email formats
- Positive consultation duration
- Chronological time ranges
- Valid date ranges

Validation is also enforced on the backend through Jakarta Bean Validation annotations and domain-level business rules. For example, appointment request DTOs use `@NotNull`, `@Future`, `@NotBlank`, and `@Size`. fileciteturn63file4L452-L487

## User Roles

| Role | Primary Responsibilities |
|---|---|
| Admin | Users, doctors, departments, patients, appointments, billing, pharmacy, reports, audit logs, settings |
| Doctor | Appointments, patients, prescriptions, medical records |
| Receptionist | Appointments, patient registration/search, billing/invoices |
| Pharmacist | Prescription queue, pharmacy inventory, stock adjustments |
| Patient | Appointments, prescriptions, invoices, profile |

The role-based navigation model is explicitly defined in the project's UX specification. fileciteturn62file0L11-L22

## UX and Design System

MedCore HMS uses a workspace-first interface intended for daily operational use rather than a decorative dashboard.

Key design principles include:

- Clear visual hierarchy
- Professional information density
- Reusable cards and modules
- Responsive layouts
- Loading, empty, and error states
- Keyboard-oriented navigation through a command palette
- Contextual warnings instead of unnecessary interruptions
- Destructive confirmation dialogs
- Non-striped, scan-friendly tables

The design specification emphasizes a command palette as a real productivity feature and defines workspace cards as the main dashboard navigation layer. fileciteturn62file1L61-L67

## Responsive Design

The UI is designed to degrade gracefully across desktop, laptop, tablet, and smaller screens. The project specifications define different navigation and workspace behaviors by breakpoint, with particular attention to front-desk and clinical tablet usage. fileciteturn62file3L113-L132

## Quality and Testing

The backend includes Spring Boot testing, Spring Security testing, JUnit Jupiter, and Testcontainers with MySQL support. fileciteturn63file5L633-L653

Recommended release checks:

```bash
# Frontend
npm run lint
npm run build

# Backend
./mvnw clean verify
```

The expected release gate is a clean type-check/build, successful tests, valid database migrations, and a successful startup against the configured environment.

## Local Development

### Prerequisites

- Java 21
- Node.js 20+
- npm or pnpm
- MySQL 8+
- Maven wrapper supplied by the backend project

### Start the backend

```bash
cd backend
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

### Start the frontend

```bash
cd frontend
npm install
npm run dev
```

The exact environment variables and database credentials should be taken from the project's local configuration rather than committed credentials.

## Production Configuration

Before deployment, configure at minimum:

- Database connection
- JWT signing secret / key material
- CORS origins
- Application profile
- Logging level
- SMTP/email integration where required
- File/object storage configuration where applicable
- External service credentials

Secrets must be supplied through environment variables or a secrets-management system and must never be committed to Git.

## Documentation and API Discovery

The backend includes Springdoc OpenAPI support, making the REST API suitable for interactive API documentation and contract inspection. fileciteturn63file5L614-L617

For a running backend, use the project's configured Swagger/OpenAPI endpoint to inspect available endpoints and request/response models.

## Repository Guidelines

Recommended Git workflow:

```text
main
 └── feature/*
     ├── fix/*
     └── chore/*
```

Keep commits focused and descriptive. Avoid committing:

- Secrets
- Local database dumps
- Build output
- IDE metadata
- `.env` files containing credentials
- Generated temporary files

## Project Status

**Status:** Completed application build / portfolio-ready project baseline.

The current project includes a broad set of administrative, clinical, scheduling, pharmacy, billing, and patient-management capabilities, together with production-oriented security, validation, API structure, and a responsive application shell.

## Known Scope Notes

The project sources document a number of planned or extended operational capabilities at the system-design level. README claims are intentionally limited to capabilities supported by the available implementation and project sources.

Where deployment-specific integrations, environment values, or infrastructure details are not present in the available sources, they should be configured according to the target environment rather than inferred from this README.

## License

Add the project's chosen license here, for example:

```text
MIT License
Copyright (c) 2026 Oussema RAHMOUNI
```

Replace the example above with the final legal license selected for the repository.

## Author

**Oussema RAHMOUNI**

MedCore HMS — Hospital Management System
