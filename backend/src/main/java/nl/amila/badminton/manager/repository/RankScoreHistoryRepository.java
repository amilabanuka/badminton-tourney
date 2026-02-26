package nl.amila.badminton.manager.repository;

import nl.amila.badminton.manager.entity.RankScoreHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RankScoreHistoryRepository extends JpaRepository<RankScoreHistory, Long> {
    List<RankScoreHistory> findByTournamentPlayerIdOrderByChangedAtDesc(Long tournamentPlayerId);
}

