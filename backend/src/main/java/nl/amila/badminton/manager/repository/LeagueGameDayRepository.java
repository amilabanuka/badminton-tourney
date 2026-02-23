package nl.amila.badminton.manager.repository;

import nl.amila.badminton.manager.entity.LeagueGameDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeagueGameDayRepository extends JpaRepository<LeagueGameDay, Long> {
    Optional<LeagueGameDay> findByTournamentIdAndGameDate(Long tournamentId, LocalDate gameDate);
    List<LeagueGameDay> findByTournamentIdOrderByGameDateDesc(Long tournamentId);
    boolean existsByTournamentIdAndGameDate(Long tournamentId, LocalDate gameDate);
}

