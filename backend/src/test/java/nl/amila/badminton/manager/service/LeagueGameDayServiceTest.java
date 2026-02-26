package nl.amila.badminton.manager.service;

import nl.amila.badminton.manager.dto.GameDayResponse;
import nl.amila.badminton.manager.entity.*;
import nl.amila.badminton.manager.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeagueGameDayServiceTest {

    @Mock private TournamentRepository tournamentRepository;
    @Mock private TournamentPlayerRepository tournamentPlayerRepository;
    @Mock private LeagueGameDayRepository leagueGameDayRepository;
    @Mock private LeagueGameDayGroupMatchRepository matchRepository;
    @Mock private UserRepository userRepository;
    @Mock private LeagueTournamentSettingsRepository leagueSettingsRepository;
    @Mock private RankScoreHistoryRepository rankScoreHistoryRepository;

    private LeagueGameDayService service;

    // Shared test fixtures
    private Tournament tournament;
    private User adminUser;
    private LeagueGameDay gameDay;
    private LeagueGameDayGroup group;
    private TournamentPlayer tp1, tp2, tp3, tp4;
    private LeagueGameDayGroupPlayer gp1, gp2, gp3, gp4;
    private LeagueGameDayGroupMatch match;

    @BeforeEach
    void setUp() {
        service = new LeagueGameDayService(
            tournamentRepository, tournamentPlayerRepository,
            leagueGameDayRepository, matchRepository,
            userRepository, leagueSettingsRepository, rankScoreHistoryRepository
        );

        adminUser = new User("admin", "admin@test.com", "pass", "Admin", "User");
        adminUser.setRole(Role.ADMIN);

        tournament = new Tournament("Test League", 1L, true, TournamentType.LEAGUE);
        setId(tournament, 1L);

        gameDay = new LeagueGameDay(tournament, LocalDate.of(2025, 1, 10));
        setId(gameDay, 1L);
        gameDay.setStatus(GameDayStatus.ONGOING);

        group = new LeagueGameDayGroup(gameDay, 1);

        // Four players with different rank scores
        User u1 = new User("p1", "p1@test.com", "p", "Alice", "A");
        User u2 = new User("p2", "p2@test.com", "p", "Bob", "B");
        User u3 = new User("p3", "p3@test.com", "p", "Carol", "C");
        User u4 = new User("p4", "p4@test.com", "p", "Dave", "D");
        setId(u1, 1L); setId(u2, 2L); setId(u3, 3L); setId(u4, 4L);

        tp1 = new TournamentPlayer(tournament, u1, new BigDecimal("100.00"));
        tp2 = new TournamentPlayer(tournament, u2, new BigDecimal("80.00"));
        tp3 = new TournamentPlayer(tournament, u3, new BigDecimal("60.00"));
        tp4 = new TournamentPlayer(tournament, u4, new BigDecimal("40.00"));
        setId(tp1, 1L); setId(tp2, 2L); setId(tp3, 3L); setId(tp4, 4L);

        gp1 = new LeagueGameDayGroupPlayer(group, tp1);
        gp2 = new LeagueGameDayGroupPlayer(group, tp2);
        gp3 = new LeagueGameDayGroupPlayer(group, tp3);
        gp4 = new LeagueGameDayGroupPlayer(group, tp4);

        // Match: tp1+tp2 vs tp3+tp4, team1 wins
        match = new LeagueGameDayGroupMatch(group, 1, gp1, gp2, gp3, gp4);
        match.setTeam1Score(21);
        match.setTeam2Score(15);
        setId(match, 10L);

        group.getMatches().add(match);
        gameDay.getGroups().add(group);
    }

    // ── finishGameDay: happy path ──────────────────────────────────────────────

    @Test
    void finishGameDay_success_marksCompleted() {
        stubHappyPath();

        GameDayResponse res = service.finishGameDay(1L, 1L, "admin");

        assertTrue(res.isSuccess());
        assertEquals(GameDayStatus.COMPLETED, gameDay.getStatus());
        verify(leagueGameDayRepository).save(gameDay);
    }

    @Test
    void finishGameDay_success_savesHistoryForAllFourPlayers() {
        stubHappyPath();

        service.finishGameDay(1L, 1L, "admin");

        // One history row per player per match = 4 rows
        verify(rankScoreHistoryRepository, times(4)).save(any(RankScoreHistory.class));
    }

    @Test
    void finishGameDay_success_updatesRankScores() {
        stubHappyPath();

        service.finishGameDay(1L, 1L, "admin");

        // All four players should have their rank scores updated and saved
        verify(tournamentPlayerRepository, times(4)).save(any(TournamentPlayer.class));
    }

    @Test
    void finishGameDay_team1WinsHigherScore_team1GainsPoints_team2LosesPoints() {
        // team1 strength X = (100+80)/2 = 90, team2 strength Y = (60+40)/2 = 50
        // T = 1 / (1 + 10^((50-90)/480)) = 1 / (1 + 10^(-40/480)) ≈ 0.556
        // team1 delta = K*T ≈ 32 * 0.556 ≈ +17.79  (wins)
        // team2 delta ≈ -17.79
        stubHappyPath();
        service.finishGameDay(1L, 1L, "admin");

        // Winning team players gain score
        assertTrue(tp1.getRankScore().compareTo(new BigDecimal("100.00")) > 0,
            "Winner tp1 should have higher rank score");
        assertTrue(tp2.getRankScore().compareTo(new BigDecimal("80.00")) > 0,
            "Winner tp2 should have higher rank score");
        // Losing team players lose score
        assertTrue(tp3.getRankScore().compareTo(new BigDecimal("60.00")) < 0,
            "Loser tp3 should have lower rank score");
        assertTrue(tp4.getRankScore().compareTo(new BigDecimal("40.00")) < 0,
            "Loser tp4 should have lower rank score");
    }

    @Test
    void finishGameDay_team1DeltaEqualsNegativeTeam2Delta() {
        stubHappyPath();
        service.finishGameDay(1L, 1L, "admin");

        BigDecimal team1Gain = tp1.getRankScore().subtract(new BigDecimal("100.00"));
        BigDecimal team2Loss = new BigDecimal("60.00").subtract(tp3.getRankScore());
        // both players in each team get the same delta
        assertEquals(0, team1Gain.compareTo(team2Loss),
            "Winner gain should equal loser loss in absolute value");
    }

    // ── finishGameDay: error cases ─────────────────────────────────────────────

    @Test
    void finishGameDay_gameDayNotFound_returnsError() {
        when(leagueGameDayRepository.findByIdWithAll(1L)).thenReturn(Optional.empty());

        GameDayResponse res = service.finishGameDay(1L, 1L, "admin");

        assertFalse(res.isSuccess());
        assertEquals("Game day not found", res.getMessage());
    }

    @Test
    void finishGameDay_notOngoing_returnsError() {
        gameDay.setStatus(GameDayStatus.COMPLETED);
        stubDayLookup();
        stubAdminUser();

        GameDayResponse res = service.finishGameDay(1L, 1L, "admin");

        assertFalse(res.isSuccess());
        assertTrue(res.getMessage().contains("ONGOING"));
    }

    @Test
    void finishGameDay_alreadyCompleted_blocked() {
        gameDay.setStatus(GameDayStatus.COMPLETED);
        stubDayLookup();
        stubAdminUser();

        GameDayResponse res = service.finishGameDay(1L, 1L, "admin");

        assertFalse(res.isSuccess());
        verify(rankScoreHistoryRepository, never()).save(any());
        verify(tournamentPlayerRepository, never()).save(any());
    }

    @Test
    void finishGameDay_missingScore_returnsError() {
        match.setTeam1Score(null); // missing score
        stubDayLookup();
        stubAdminUser();

        GameDayResponse res = service.finishGameDay(1L, 1L, "admin");

        assertFalse(res.isSuccess());
        assertTrue(res.getMessage().contains("missing a score"));
        verify(rankScoreHistoryRepository, never()).save(any());
    }

    @Test
    void finishGameDay_missingTeam2Score_returnsError() {
        match.setTeam2Score(null);
        stubDayLookup();
        stubAdminUser();

        GameDayResponse res = service.finishGameDay(1L, 1L, "admin");

        assertFalse(res.isSuccess());
        assertTrue(res.getMessage().contains("missing a score"));
    }

    @Test
    void finishGameDay_noLeagueSettings_returnsError() {
        stubDayLookup();
        stubAdminUser();
        when(leagueSettingsRepository.findByTournamentId(1L)).thenReturn(Optional.empty());

        GameDayResponse res = service.finishGameDay(1L, 1L, "admin");

        assertFalse(res.isSuccess());
        assertTrue(res.getMessage().contains("ELO settings not found"));
    }

    @Test
    void finishGameDay_accessDenied_returnsError() {
        stubDayLookup();
        // Use a TOURNY_ADMIN who is NOT an admin of the tournament
        User outsider = new User("outsider", "o@test.com", "p", "Out", "Sider");
        outsider.setRole(Role.TOURNY_ADMIN);
        setId(outsider, 99L);
        when(userRepository.findByUsername("outsider")).thenReturn(Optional.of(outsider));

        GameDayResponse res = service.finishGameDay(1L, 1L, "outsider");

        assertFalse(res.isSuccess());
        assertEquals("Access denied", res.getMessage());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void stubHappyPath() {
        stubDayLookup();
        stubAdminUser();
        stubEloSettings(32);
        when(tournamentPlayerRepository.findById(1L)).thenReturn(Optional.of(tp1));
        when(tournamentPlayerRepository.findById(2L)).thenReturn(Optional.of(tp2));
        when(tournamentPlayerRepository.findById(3L)).thenReturn(Optional.of(tp3));
        when(tournamentPlayerRepository.findById(4L)).thenReturn(Optional.of(tp4));
        when(rankScoreHistoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(tournamentPlayerRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(leagueGameDayRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    private void stubDayLookup() {
        when(leagueGameDayRepository.findByIdWithAll(1L)).thenReturn(Optional.of(gameDay));
    }

    private void stubAdminUser() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
    }

    private void stubEloSettings(int k) {
        LeagueTournamentSettings settings = new LeagueTournamentSettings(
            tournament, RankingLogic.MODIFIED_ELO, new ModifiedEloConfig(k, 5));
        when(leagueSettingsRepository.findByTournamentId(1L)).thenReturn(Optional.of(settings));
    }

    /** Reflectively set ID on entities since there's no public setter for id. */
    private static void setId(Object entity, Long id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Could not set id on " + entity.getClass().getSimpleName(), e);
        }
    }
}



