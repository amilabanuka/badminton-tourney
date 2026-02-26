# CLAUDE.md — Badminton App

## General Rules

- Do NOT create documentation `.md` files unless explicitly requested
- Run builds after making changes to validate them
- Use the CVE Remediator agent when dealing with dependency vulnerabilities
- Always update or add tests as part of every plan — never skip this

---

## Backend (Spring Boot / Java)

### Entity Annotations — Always Use Jakarta

Always use `jakarta.persistence.*` on all entities. Never use Spring Data annotations.

```java
// ✅ Correct
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
```

```java
// ❌ Wrong
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
```

Rules:
- Always use `jakarta.persistence.*` wildcard import for entities
- Always add `@GeneratedValue(strategy = GenerationType.IDENTITY)` on `@Id` fields
- Never use `@MappedCollection` (Spring Data JDBC specific)
- Never use `org.springframework.data.annotation.Id`
- Never use `org.springframework.data.relational.core.mapping.Table`
- Never add redundant FK `Long` fields (e.g. `tournamentId`) when a `@ManyToOne` reference exists
- Always use `JpaRepository<T, ID>` in repositories, not `CrudRepository`
- This project uses **Spring Data JPA** (`spring-boot-starter-data-jpa`), NOT Spring Data JDBC

### Immutable Field Pattern

For fields that must be set at creation and never modified:

```java
@Setter(lombok.AccessLevel.NONE)          // suppresses setter for this field only
@Column(nullable = false, updatable = false)  // enforces immutability at DB level
private TournamentType type;
```

Set via constructor only — never expose a public setter.

### Build Validation

After editing Java source, validate the build compiles cleanly:

```bash
cd backend && mvn compile
```

- `BUILD SUCCESS` = code is valid
- `BUILD FAILURE` = real error, must fix

### IDE Warnings vs Real Errors

| Severity | Meaning | Action |
|----------|---------|--------|
| `ERROR` | Real compile error | Must fix |
| `WARNING(300)` | IDE hint only | Ignore — confirm with `mvnw.cmd compile` |

Common false-positive warnings (safe to ignore if Maven compiles):
- `"Enum 'X' is never used"`
- `"Constructor 'X(...)' is never used"`
- `"Method 'X' is never used"`
- `"Selector form-check-input is never used"`

Lombok-annotated classes often produce false IDE errors — always confirm with Maven.

### Database Migrations

`schema.sql` uses `CREATE TABLE IF NOT EXISTS` — it only applies to a fresh database. For an existing database, schema changes must be applied manually with `ALTER TABLE`.

Migration files live in: `backend/src/main/resources/migrations/`

Whenever `schema.sql` is updated with a new column or table:
1. Write a corresponding `ALTER TABLE` statement
2. Save it to `backend/src/main/resources/migrations/` with a descriptive name
3. Run it manually against the live database

### Backend Testing (Mandatory)

Adding/updating tests is a **required step** in every backend plan. Never skip it.

- Write tests before or alongside implementation (not after)
- Every new service method, controller endpoint, or entity change must have tests
- Tests live in `src/test/java/nl/amila/badminton/manager/`
- Service-layer tests: use `@ExtendWith(MockitoExtension.class)` + `@Mock` / `@InjectMocks`
- Run tests: `mvn test`

Test coverage checklist per service method:
- Happy-path (success) test
- Not-found / empty result test
- Invalid input / wrong role test
- Duplicate / already-exists test (where applicable)
- Access-denied / security test (where applicable)

---

## Frontend (Vue 3)

### Dependency Version Alignment

Keep these versions aligned to avoid build errors:
- `@vue/cli-service` and all `@vue/cli-plugin-*` must be the same major version (~5.0.0)
- `eslint` version must match `@vue/eslint-config-*` requirements

### vue.config.js Rules

`transpileDependencies` must be an array, never a boolean:

```javascript
transpileDependencies: [],    // ✅ Correct
transpileDependencies: true,  // ❌ Wrong — causes "transpileDependencies.map is not a function"
```

### Bootstrap 5 (Required)

This project uses Bootstrap 5. Keep it enabled.

`frontend/src/main.js` must always import:
```javascript
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
```

- Use Bootstrap classes (`container`, `row`, `col-*`, `btn`, `card`, `navbar`, etc.) for layout
- Keep custom CSS minimal — only add overrides when Bootstrap doesn't cover the need
- Do not remove Bootstrap imports unless replacing with an approved alternative

### Frontend Testing (Mandatory)

Adding/updating tests is a **required step** in every frontend plan. Never skip it.

- Write tests before or alongside implementation (not after)
- Every new Vue component, service call, or action must have corresponding tests
- Tests live alongside their subjects or in a `__tests__/` directory
- Run tests: `npm run test:unit`

Test coverage checklist per feature:
- Component renders without errors (smoke test)
- Expected UI elements present for each state (loading, success, error)
- API service calls are mocked and the correct endpoint is hit
- User interactions (clicks, form submissions) trigger the right behaviour
- Route guards / redirects work as expected (where applicable)

---

## Key Ports and URLs

- Backend: `http://localhost:8098`
- Frontend dev server: `http://localhost:8080` (proxies `/api/*` to backend)
- Frontend build output: `frontend/target/dist/`

## Common Commands

```bash
# Frontend
cd frontend
npm install
npm run serve       # dev server
npm run build       # production build
npm run lint        # linter
npm run test:unit   # unit tests

# Backend
cd backend
mvn clean install   # full build with tests
mvn test            # tests only
mvn spring-boot:run # run app
mvn compile         # compile-only validation
```
