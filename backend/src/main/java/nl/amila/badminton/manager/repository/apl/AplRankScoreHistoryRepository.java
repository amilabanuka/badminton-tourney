package nl.amila.badminton.manager.repository.apl;

import nl.amila.badminton.manager.entity.apl.AplRankScoreHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AplRankScoreHistoryRepository extends JpaRepository<AplRankScoreHistory, Long> {
    List<AplRankScoreHistory> findByTournamentPlayerIdOrderByChangedAtDesc(Long tournamentPlayerId);
}
