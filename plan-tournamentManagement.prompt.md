# Plan: Implement Tournament Management with Role-Based Access

Add tournament creation and management where ADMIN users can create tournaments with basic info, then separately assign TOURNY_ADMIN users as tournament admins and PLAYER users as participants. Tournament entity stores name, owner (TOURNY_ADMIN user), and enabled status.

## Steps

1. Create `Tournament` entity in entity package (`E:\learn\badminton-app\backend\src\main\java\nl\amila\badminton\manager\entity`) with fields: `id`, `name`, `ownerId` (FK to users), `enabled`, `createdAt`, `updatedAt`

2. Add database schema in `schema.sql` (`E:\learn\badminton-app\backend\src\main\resources\schema.sql`) for `tournament` table and junction tables `tournament_admins` (tournament_id, user_id) and `tournament_players` (tournament_id, user_id)

3. Create `TournamentRepository` in repository package (`E:\learn\badminton-app\backend\src\main\java\nl\amila\badminton\manager\repository`) extending `CrudRepository<Tournament, Long>` with custom queries for finding by owner

4. Create DTOs in dto package (`E:\learn\badminton-app\backend\src\main\java\nl\amila\badminton\manager\dto`): `CreateTournamentRequest`, `TournamentResponse`, `AddTournamentAdminRequest`, `AddTournamentPlayerRequest`

5. Implement `TournamentService` in service package (`E:\learn\badminton-app\backend\src\main\java\nl\amila\badminton\manager\service`) with methods: `createTournament()` (validates tournament name is unique globally), `addTournamentAdmin()` (validates user has TOURNY_ADMIN role), `addTournamentPlayer()` (validates user has PLAYER role), `removeTournamentAdmin()`, `removeTournamentPlayer()`, `getTournaments()`, `getTournamentById()`

6. Create `TournamentController` in controller package (`E:\learn\badminton-app\backend\src\main\java\nl\amila\badminton\manager\controller`) with endpoints:
   - `POST /api/tournaments` - Create tournament (ADMIN only)
   - `POST /api/tournaments/{id}/admins` - Add tournament admin (ADMIN only)
   - `POST /api/tournaments/{id}/players` - Add tournament player (ADMIN or TOURNY_ADMIN)
   - `DELETE /api/tournaments/{id}/admins/{userId}` - Remove tournament admin (ADMIN only)
   - `DELETE /api/tournaments/{id}/players/{userId}` - Remove tournament player (ADMIN or TOURNY_ADMIN)
   - `GET /api/tournaments` - List all tournaments (authenticated users)
   - `GET /api/tournaments/{id}` - Get tournament details (authenticated users)

7. Update `SecurityConfig` to enforce role-based access control:
   - `/api/tournaments` POST - ADMIN only
   - `/api/tournaments/admins/available` GET - ADMIN only
   - `/api/tournaments/{id}/admins/**` - ADMIN only
   - `/api/tournaments/{id}/players/**` - ADMIN or TOURNY_ADMIN
   - `/api/tournaments/**` GET - authenticated users

8. Add `findByRole()` method to `UserRepository` to query users by role

## Further Considerations

1. **Owner assignment**: Owner is specified at tournament creation by providing ownerId (must be a user with TOURNY_ADMIN role)
2. **Validation**: Tournament names must be unique globally (enforced via unique constraint in database and service layer validation)



