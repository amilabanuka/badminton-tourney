package nl.amila.badminton.manager.repository;

import nl.amila.badminton.manager.entity.LeagueGameDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeagueGameDayRepository extends JpaRepository<LeagueGameDay, Long> {
    Optional<LeagueGameDay> findByTournamentIdAndGameDate(Long tournamentId, LocalDate gameDate);
    List<LeagueGameDay> findByTournamentIdOrderByGameDateDesc(Long tournamentId);
    boolean existsByTournamentIdAndGameDate(Long tournamentId, LocalDate gameDate);

    /**
     * Eagerly fetch a game day together with its groups, group players, group matches,
     * and all four match-player references in a single query.
     */
    @Query("""
            SELECT DISTINCT d FROM LeagueGameDay d
            LEFT JOIN FETCH d.groups g
            LEFT JOIN FETCH g.players gp
            LEFT JOIN FETCH gp.tournamentPlayer tp
            LEFT JOIN FETCH tp.user
            LEFT JOIN FETCH g.matches m
            LEFT JOIN FETCH m.team1Player1 mp1
            LEFT JOIN FETCH mp1.tournamentPlayer mtp1
            LEFT JOIN FETCH mtp1.user
            LEFT JOIN FETCH m.team1Player2 mp2
            LEFT JOIN FETCH mp2.tournamentPlayer mtp2
            LEFT JOIN FETCH mtp2.user
            LEFT JOIN FETCH m.team2Player1 mp3
            LEFT JOIN FETCH mp3.tournamentPlayer mtp3
            LEFT JOIN FETCH mtp3.user
            LEFT JOIN FETCH m.team2Player2 mp4
            LEFT JOIN FETCH mp4.tournamentPlayer mtp4
            LEFT JOIN FETCH mtp4.user
            WHERE d.id = :id
            """)
    Optional<LeagueGameDay> findByIdWithAll(@Param("id") Long id);
}

