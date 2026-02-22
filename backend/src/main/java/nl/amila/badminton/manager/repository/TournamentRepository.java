package nl.amila.badminton.manager.repository;

import nl.amila.badminton.manager.entity.Tournament;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentRepository extends CrudRepository<Tournament, Long> {
    Optional<Tournament> findByName(String name);

    boolean existsByName(String name);

    List<Tournament> findByOwnerId(Long ownerId);

    @Query("SELECT t.* FROM tournament t")
    List<Tournament> findAllTournaments();

    @Query("SELECT u.id FROM tournament_admins ta JOIN users u ON ta.user_id = u.id WHERE ta.tournament_id = :tournamentId")
    List<Long> findAdminIdsByTournamentId(@Param("tournamentId") Long tournamentId);

    @Query("SELECT u.id FROM tournament_players tp JOIN users u ON tp.user_id = u.id WHERE tp.tournament_id = :tournamentId")
    List<Long> findPlayerIdsByTournamentId(@Param("tournamentId") Long tournamentId);

    @Query("INSERT INTO tournament_admins (tournament_id, user_id) VALUES (:tournamentId, :userId)")
    void addAdmin(@Param("tournamentId") Long tournamentId, @Param("userId") Long userId);

    @Query("INSERT INTO tournament_players (tournament_id, user_id) VALUES (:tournamentId, :userId)")
    void addPlayer(@Param("tournamentId") Long tournamentId, @Param("userId") Long userId);

    @Query("DELETE FROM tournament_admins WHERE tournament_id = :tournamentId AND user_id = :userId")
    void removeAdmin(@Param("tournamentId") Long tournamentId, @Param("userId") Long userId);

    @Query("DELETE FROM tournament_players WHERE tournament_id = :tournamentId AND user_id = :userId")
    void removePlayer(@Param("tournamentId") Long tournamentId, @Param("userId") Long userId);

    @Query("SELECT COUNT(*) > 0 FROM tournament_admins WHERE tournament_id = :tournamentId AND user_id = :userId")
    boolean isAdmin(@Param("tournamentId") Long tournamentId, @Param("userId") Long userId);

    @Query("SELECT COUNT(*) > 0 FROM tournament_players WHERE tournament_id = :tournamentId AND user_id = :userId")
    boolean isPlayer(@Param("tournamentId") Long tournamentId, @Param("userId") Long userId);
}

