# VolunteerHub

## 1. Project Objective

VolunteerHub provides an organizer dashboard and volunteer portal backed by a REST API for organizing events, registering volunteers, recording attendance, and tracking contributed hours.

## 2. Technologies

- Java 17
- Spring Boot 4
- Spring MVC REST controllers
- Spring Data JPA and Hibernate
- MySQL
- Jakarta Bean Validation
- Maven
- JUnit 5, Mockito, and Spring MVC Test

## 3. Features

- Create, read, update, delete, search, and list upcoming events
- Volunteer registration with email-format validation and unique-email checks
- Volunteer signups with duplicate-registration and event-capacity protection
- Attendance upsert for a signup and attendance listing by event
- Volunteer participation history and present-only total hours
- Database-backed dashboard summary
- Consistent JSON errors through `@RestControllerAdvice`
- Browser dashboard for organizers and an event registration portal for volunteers

## 4. Architecture

The application uses a simple layered structure:

- **Controller:** Maps HTTP requests and applies request DTO validation.
- **Service:** Enforces business rules, orchestrates repositories, and maps entities to response DTOs.
- **Repository:** Spring Data JPA queries against MySQL.
- **Entity:** JPA persistence model.
- **DTO:** Validated request and stable response shapes.
- **Exception advice:** Translates validation, domain, and unexpected errors to JSON.

## 5. Database Entities and Relationships

- `Event`: name, description, date, location, and volunteer capacity.
- `Volunteer`: name, unique email, and optional phone.
- `SignUp`: connects one volunteer to one event and stores the signup date.
- `AttendanceRecord`: attendance status and contribution hours for a signup.

Relationship overview:

- One `Event` has many `SignUp` records; each signup refers to one event (`Event` one-to-many, `SignUp` many-to-one).
- One `Volunteer` has many `SignUp` records; each signup refers to one volunteer (`Volunteer` one-to-many, `SignUp` many-to-one).
- A `SignUp` has zero or one `AttendanceRecord`; the attendance table has a unique foreign key to its signup.

## 6. API Endpoints

All endpoints are under `http://localhost:8080`. `GET` operations return `200 OK` unless the resource does not exist. Invalid request bodies return `400 Bad Request`.

| Method | Path | Purpose | Success status |
|---|---|---|---|
| `POST` | `/api/events` | Create an event | `201 Created` |
| `GET` | `/api/events` | List events; optional `?location=College` filter | `200 OK` |
| `GET` | `/api/events/upcoming` | List events today or later | `200 OK` |
| `GET` | `/api/events/{id}` | Get one event | `200 OK` |
| `PUT` | `/api/events/{id}` | Replace event fields | `200 OK` |
| `DELETE` | `/api/events/{id}` | Delete an event | `204 No Content` |
| `POST` | `/api/volunteers` | Create a volunteer | `201 Created` |
| `GET` | `/api/volunteers` | List volunteers | `200 OK` |
| `GET` | `/api/volunteers/{id}` | Get one volunteer | `200 OK` |
| `PUT` | `/api/volunteers/{id}` | Update a volunteer | `200 OK` |
| `DELETE` | `/api/volunteers/{id}` | Delete a volunteer | `204 No Content` |
| `GET` | `/api/volunteers/{id}/hours` | Get present-only total hours | `200 OK` |
| `GET` | `/api/volunteers/{id}/history` | Get participation history | `200 OK` |
| `POST` | `/api/signups` | Register a volunteer for an event | `201 Created` |
| `GET` | `/api/signups/{id}` | Get one signup | `200 OK` |
| `GET` | `/api/events/{eventId}/signups` | List registrations for an event | `200 OK` |
| `GET` | `/api/volunteers/{volunteerId}/signups` | List a volunteer's registrations | `200 OK` |
| `PUT` | `/api/attendance/{signupId}` | Create or update attendance | `200 OK` |
| `GET` | `/api/events/{eventId}/attendance` | List recorded attendance for an event | `200 OK` |
| `GET` | `/api/dashboard/summary` | Get database totals and volunteer hours | `200 OK` |

Missing resources return `404 Not Found`. Duplicate volunteers, duplicate signups, and full events return `409 Conflict`. Attendance rules and validation failures return `400 Bad Request`. Unsupported methods return `405`, unsupported content types `415`, and unexpected errors return a generic `500` response without stack traces.

## 7. Business Rules and Validation

- Event name and location cannot be blank; date is required; capacity must be positive.
- Volunteer name and email cannot be blank; email must be valid and unique (case-insensitive); phone, when supplied, must match the phone format and length.
- Signup requires existing event and volunteer records; duplicate registrations are rejected; signup count must be below event capacity.
- Attendance requires an existing signup. Hours must be zero or greater; positive hours are allowed only when `attended` is true. Attendance is created or updated for that signup.
- Total volunteer hours include only present attendance records with nonnegative hours.
- Service methods enforce domain rules; database constraints provide additional integrity protection.

## 8. MySQL Configuration

The MySQL Connector/J driver is included. The defaults in `src/main/resources/application.properties` use:

- URL: `jdbc:mysql://localhost:3306/volunteerhub?createDatabaseIfNotExist=true`
- Username: `root`
- Password: required environment variable `DB_PASSWORD`

Optionally set `DB_URL` and `DB_USERNAME` in the environment to use a different database or username. Set `DB_PASSWORD` in your local environment or IDE run configuration. Do not put database credentials in source code or commit them. Hibernate uses `ddl-auto=update` to create/update tables during development.

## 9. Run the Project

Prerequisites: JDK 17+, MySQL running, and a configured `DB_PASSWORD`.

From PowerShell at the project root:

```powershell
$securePassword = Read-Host "MySQL password" -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new("", $securePassword).Password
./mvnw.cmd clean compile
./mvnw.cmd spring-boot:run
```

The password is entered at a hidden prompt and is not stored in project files. The environment variable applies to this PowerShell session. The API listens on `http://localhost:8080` by default.

Open `http://localhost:8080/` for the combined organizer dashboard and volunteer portal. Choose **Volunteer portal** in the sidebar to browse events and register. The legacy `/volunteer.html` URL redirects to the same page and opens the volunteer portal.

### Manual Sample Data

`sample-data.sql` provides seven demo volunteers, six events, ten signups, and attendance records with 15 present hours in total. It uses demo-only email addresses and checks for existing matching records before inserting, so it can be run more than once without duplicating those records.

1. Start the app once with MySQL configured so Hibernate creates the tables.
2. In MySQL Workbench, open `sample-data.sql` from the project root and execute it.
3. Try `GET /api/volunteers/1/history` using the ID returned for `demo.avery@volunteerhub.local`; use that volunteer's actual ID, not necessarily `1`.
4. Avery's total should be 12 hours. The dashboard and event/volunteer list endpoints will include the demo rows.

## 10. Postman Testing Plan

### Postman Setup

Create a Postman environment variable `baseUrl` with value `http://localhost:8080`. Use `Content-Type: application/json` for JSON request bodies. Save returned IDs as `volunteerId`, `secondVolunteerId`, `eventId`, `fullEventId`, `signupId`, and `secondSignupId` as requests succeed. The examples below use IDs for readability; substitute the IDs returned by your database.

### Recommended Order: Core Workflow

1. **Create volunteers**
   - `POST {{baseUrl}}/api/volunteers`
   - Body: `{"name":"Avery Smith","email":"avery@example.com","phone":"+1 555-123-4567"}`
   - Expected: `201 Created`; JSON contains generated `id`, name, email, and phone. Repeat with `Jordan Lee` and `jordan@example.com` for capacity testing.

2. **Create events**
   - `POST {{baseUrl}}/api/events`
   - Body: `{"name":"College Garden Cleanup","description":"Prepare the garden","date":"2026-10-10","location":"College Campus","volunteerCapacity":2}`
   - Expected: `201 Created`; event response includes generated `id` and the submitted fields. Create a second event with capacity `1` for the full-event edge case.

3. **Get and search events**
   - `GET {{baseUrl}}/api/events`
   - Expected: `200 OK`; JSON array of event responses.
   - `GET {{baseUrl}}/api/events?location=College`
   - Expected: `200 OK`; only matching locations are returned (case-insensitive substring match).

4. **Get upcoming events**
   - `GET {{baseUrl}}/api/events/upcoming`
   - Expected: `200 OK`; events dated today or later, ordered by date.

5. **Register volunteers for events**
   - `POST {{baseUrl}}/api/signups`
   - Body: `{"eventId":{{eventId}},"volunteerId":{{volunteerId}}}`
   - Expected: `201 Created`; response contains signup `id`, event ID, volunteer ID, and signup date. Also register the first volunteer for the second event to create multi-event history.

6. **View event registrations**
   - `GET {{baseUrl}}/api/events/{{eventId}}/signups`
   - Expected: `200 OK`; array containing the signup created in Test 5. Also try `/api/volunteers/{{volunteerId}}/signups`.

7. **Mark the volunteer present**
   - `PUT {{baseUrl}}/api/attendance/{{signupId}}`
   - Body: `{"attended":true,"hoursContributed":0}`
   - Expected: `200 OK`; response contains attendance ID, signup ID, `attended: true`, and zero hours.

8. **Add contribution hours**
   - Repeat the PUT for the same signup with body `{"attended":true,"hoursContributed":4}`.
   - Expected: `200 OK`; same signup's attendance record is updated to 4 hours. Record 8 hours for the other event's signup to test the 12-hour total.

9. **Get volunteer total hours**
   - `GET {{baseUrl}}/api/volunteers/{{volunteerId}}/hours`
   - Expected: `200 OK`; with 4 and 8 attended hours, `totalHours` is `12`.

10. **Get volunteer history**
    - `GET {{baseUrl}}/api/volunteers/{{volunteerId}}/history`
    - Expected: `200 OK`; one item per signup, ordered by event date, with event name, date, location, attendance status, and hours.

11. **View attendance and dashboard**
    - `GET {{baseUrl}}/api/events/{{eventId}}/attendance` returns `200 OK` and its recorded attendance array.
    - `GET {{baseUrl}}/api/dashboard/summary` returns `200 OK` and totals calculated from current database rows.

12. **Verify remaining CRUD routes**
    - `GET {{baseUrl}}/api/events/{{eventId}}` and `GET {{baseUrl}}/api/volunteers/{{volunteerId}}` return `200 OK`.
   - `GET {{baseUrl}}/api/volunteers` returns `200 OK` and the volunteer array.
   - `PUT {{baseUrl}}/api/events/{{eventId}}` body: `{"name":"Updated Garden Cleanup","description":"Bring gloves","date":"2026-10-12","location":"College Campus","volunteerCapacity":3}`; expected `200 OK` with updated event.
   - `PUT {{baseUrl}}/api/volunteers/{{volunteerId}}` body: `{"name":"Avery Smith","email":"avery@example.com","phone":"+1 555-123-4567"}`; expected `200 OK` with updated volunteer.
    - `GET {{baseUrl}}/api/signups/{{signupId}}` returns `200 OK`.
   - `DELETE {{baseUrl}}/api/events/{{eventId}}` and `DELETE {{baseUrl}}/api/volunteers/{{volunteerId}}` return `204 No Content` for unreferenced test records. Use disposable data; foreign keys can prevent deleting records that still have signups.

### Edge-Case Requests

All error responses use `{"status":number,"message":"...","timestamp":"ISO-8601 timestamp"}`.

| Case | Request | Body | Expected status and response |
|---|---|---|---|
| Duplicate volunteer email | `POST /api/volunteers` | `{"name":"Avery Duplicate","email":"avery@example.com","phone":"+1 555-555-0100"}` | `409`; message `Volunteer with this email already exists.` |
| Invalid volunteer email | `POST /api/volunteers` | `{"name":"Avery","email":"not-an-email"}` | `400`; validation message identifies `email` |
| Invalid volunteer data | `POST /api/volunteers` | `{"name":" ","email":""}` | `400`; validation message identifies required fields |
| Non-existent volunteer | `GET /api/volunteers/999999` | None | `404`; message `Volunteer not found` |
| Non-existent event | `GET /api/events/999999` | None | `404`; message `Event not found` |
| Duplicate event registration | `POST /api/signups` | `{"eventId":{{eventId}},"volunteerId":{{volunteerId}}}` repeat same pair | `409`; message `Volunteer is already registered for this event.` |
| Event capacity reached | `POST /api/signups` | `{"eventId":{{fullEventId}},"volunteerId":{{volunteerId}}}` after capacity is reached | `409`; message `Event is already full. Volunteer cannot register.` |
| Signup for full event | `POST /api/signups` | `{"eventId":{{fullEventId}},"volunteerId":{{secondVolunteerId}}}` | `409`; same full-event message; signup is not saved |
| Invalid signup attendance | `PUT /api/attendance/999999` | `{"attended":true,"hoursContributed":1}` | `404`; message `Sign-up not found` |
| Negative contribution hours | `PUT /api/attendance/{{signupId}}` | `{"attended":true,"hoursContributed":-1}` | `400`; validation message identifies `hoursContributed` |
| Hours when absent | `PUT /api/attendance/{{signupId}}` | `{"attended":false,"hoursContributed":4}` | `400`; message `Contribution hours can only be recorded for volunteers marked as present.` |
| Invalid event data | `POST /api/events` | `{"name":"","date":null,"location":"","volunteerCapacity":0}` | `400`; validation message identifies invalid fields |
| Malformed JSON | `POST /api/signups` | `{invalid-json` | `400`; message `Invalid request data.` |

For other endpoints, use the endpoint table and core workflow above. Resource reads/updates/deletes for missing IDs return `404`; successful deletes return an empty `204` response. Unexpected server errors return `500` with `An unexpected error occurred.` and no stack trace.

## 11. Example API Requests and Responses

Create event (`POST /api/events`):

```json
{
  "name": "Tree Plantation Drive",
  "description": "Plant native trees in the community park.",
  "date": "2026-10-10",
  "location": "Community Park",
  "volunteerCapacity": 30
}
```

Attendance update (`PUT /api/attendance/1`):

```json
{
  "attended": true,
  "hoursContributed": 4
}
```

Volunteer hours (`GET /api/volunteers/1/hours`):

```json
{
  "volunteerId": 1,
  "volunteerName": "Srivel MV",
  "totalHours": 12
}
```

Dashboard (`GET /api/dashboard/summary`; values depend on database contents):

```json
{
  "totalEvents": 10,
  "totalVolunteers": 100,
  "totalSignups": 250,
  "totalAttendanceRecords": 200,
  "totalVolunteerHours": 850
}
```

## 12. Exception Handling

`@RestControllerAdvice` converts domain exceptions, Bean Validation failures, malformed JSON, type errors, database integrity conflicts, and unexpected exceptions to a consistent error DTO. Services throw domain exceptions; controllers remain focused on HTTP routing. Unexpected exception details are not returned to clients.

## 13. Viva Questions and Answers

**Why Spring Boot?** It provides auto-configuration, embedded server support, and a quick way to build REST services.

**Why MySQL?** It is a relational database suitable for connected entities such as events, volunteers, signups, and attendance.

**What is JPA?** JPA is the Java specification for mapping objects to relational data and performing persistence operations.

**What is Hibernate?** Hibernate is the JPA implementation used by this application to generate SQL and map entities.

**Why use `@OneToMany`?** It models one event or volunteer being associated with many signup records.

**Why use `@ManyToOne`?** Each signup belongs to one event and one volunteer, while each parent can have many signups.

**Why is `SignUp` a separate entity?** It represents the event-volunteer registration relationship and stores its own signup date; attendance also references it.

**Why put business logic in a service?** It keeps rules reusable and independent of HTTP request handling.

**How is event capacity checked?** The service counts current event signups and rejects another when the count is at least the configured capacity.

**How are duplicate signups prevented?** The service checks for the volunteer/event pair before saving; the database relation provides additional integrity protection.

**How are contribution hours calculated?** The service sums nonnegative hours only from attendance records marked present.

**How does `@RestControllerAdvice` work?** Spring applies its exception handlers to controller requests and serializes their returned error DTOs as JSON.

**Why use DTOs?** They validate incoming data and keep API contracts separate from persistence entities.

**Which HTTP status codes are used?** `200` for successful reads/updates, `201` for creation, `204` for deletion, `400` for invalid requests, `404` for missing resources, `409` for business/data conflicts, `405` for unsupported methods, `415` for unsupported media types, and `500` for unexpected errors.

## 14. Final Project Structure

Generated Maven output under `target/` is omitted.

```text
VolunteerHub/
|-- .gitattributes
|-- .gitignore
|-- HELP.md
|-- README.md
|-- mvnw
|-- mvnw.cmd
|-- pom.xml
|-- sample-data.sql
|-- .mvn/
|   `-- wrapper/
|       `-- maven-wrapper.properties
|-- src/
|   |-- main/
|   |   |-- java/com/example/demo/volenteerhub/
|   |   |   |-- DemoApplication.java
|   |   |   |-- controller/
|   |   |   |   |-- AttendanceController.java
|   |   |   |   |-- DashboardController.java
|   |   |   |   |-- EventController.java
|   |   |   |   |-- SignUpController.java
|   |   |   |   `-- VolunteerController.java
|   |   |   |-- dto/
|   |   |   |   |-- ApiErrorResponse.java
|   |   |   |   |-- AttendanceRequest.java
|   |   |   |   |-- AttendanceResponse.java
|   |   |   |   |-- CreateEventRequest.java
|   |   |   |   |-- DashboardSummaryResponse.java
|   |   |   |   |-- EventResponse.java
|   |   |   |   |-- SignUpRequest.java
|   |   |   |   |-- SignUpResponse.java
|   |   |   |   |-- UpdateEventRequest.java
|   |   |   |   |-- VolunteerHistoryResponse.java
|   |   |   |   |-- VolunteerHoursResponse.java
|   |   |   |   |-- VolunteerRequest.java
|   |   |   |   `-- VolunteerResponse.java
|   |   |   |-- entity/
|   |   |   |   |-- AttendanceRecord.java
|   |   |   |   |-- Event.java
|   |   |   |   |-- SignUp.java
|   |   |   |   `-- Volunteer.java
|   |   |   |-- exception/
|   |   |   |   |-- DuplicateSignupException.java
|   |   |   |   |-- DuplicateVolunteerException.java
|   |   |   |   |-- EventFullException.java
|   |   |   |   |-- GlobalExceptionHandler.java
|   |   |   |   |-- InvalidAttendanceException.java
|   |   |   |   `-- ResourceNotFoundException.java
|   |   |   |-- repository/
|   |   |   |   |-- AttendanceRecordRepository.java
|   |   |   |   |-- EventRepository.java
|   |   |   |   |-- SignUpRepository.java
|   |   |   |   `-- VolunteerRepository.java
|   |   |   `-- service/
|   |   |       |-- AttendanceService.java
|   |   |       |-- DashboardService.java
|   |   |       |-- EventService.java
|   |   |       |-- SignUpService.java
|   |   |       `-- VolunteerService.java
|   |   `-- resources/
|   |       |-- application.properties
|   |       `-- static/
|   |           |-- app.js
|   |           |-- index.html
|   |           |-- styles.css
|   |           `-- volunteer.html
|   `-- test/java/com/example/demo/volenteerhub/
|       |-- DemoApplicationTests.java
|       |-- controller/GlobalExceptionHandlerTest.java
|       `-- service/
|           |-- AttendanceServiceTest.java
|           |-- DashboardServiceTest.java
|           |-- EventSearchServiceTest.java
|           |-- SignUpServiceTest.java
|           |-- VolunteerHistoryServiceTest.java
|           `-- VolunteerServiceTest.java
```

## 15. Verification

Focused unit and MVC tests cover dashboard aggregation, location search, validation/error response mapping, signup duplicate/capacity rules, attendance rules, and volunteer hours/history. Run the project compile with:

```powershell
./mvnw.cmd clean compile
```
