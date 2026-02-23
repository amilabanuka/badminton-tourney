package nl.amila.badminton.manager.repository;

import nl.amila.badminton.manager.entity.TournamentPlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentPlayerRepository extends JpaRepository<TournamentPlayer, Long> {
    Optional<TournamentPlayer> findByTournamentIdAndUserId(Long tournamentId, Long userId);
    List<TournamentPlayer> findByTournamentIdOrderByRankScoreDescUserIdAsc(Long tournamentId);
}

