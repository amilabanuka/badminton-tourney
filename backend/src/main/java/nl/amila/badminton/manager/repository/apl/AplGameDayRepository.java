package nl.amila.badminton.manager.repository.apl;

import nl.amila.badminton.manager.entity.apl.AplGameDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AplGameDayRepository extends JpaRepository<AplGameDay, Long> {
    Optional<AplGameDay> findByTournamentIdAndGameDate(Long tournamentId, LocalDate gameDate);
    List<AplGameDay> findByTournamentIdOrderByGameDateDesc(Long tournamentId);
    boolean existsByTournamentIdAndGameDate(Long tournamentId, LocalDate gameDate);

    @Query("""
            SELECT COUNT(gp) > 0 FROM AplGameDayGroupPlayer gp
            WHERE gp.group.gameDay.id = :gameDayId
              AND gp.tournamentPlayer.id = :playerId
            """)
    boolean isPlayerPresentInGameDay(@Param("gameDayId") Long gameDayId,
                                     @Param("playerId") Long playerId);

    @Query("""
            SELECT DISTINCT d FROM AplGameDay d
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
    Optional<AplGameDay> findByIdWithAll(@Param("id") Long id);
}
