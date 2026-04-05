package nl.amila.badminton.manager.service.league;

import nl.amila.badminton.manager.dto.league.GameDayResponse;
import nl.amila.badminton.manager.dto.league.PlayerHistoryResponse;
import nl.amila.badminton.manager.entity.*;
import nl.amila.badminton.manager.entity.league.GameDayStatus;
import nl.amila.badminton.manager.entity.league.LeagueGameDay;
import nl.amila.badminton.manager.entity.league.LeagueGameDayGroup;
import nl.amila.badminton.manager.entity.league.LeagueGameDayGroupMatch;
import nl.amila.badminton.manager.entity.league.LeagueGameDayGroupPlayer;
import nl.amila.badminton.manager.entity.league.LeagueTournamentSettings;
import nl.amila.badminton.manager.entity.league.RankScoreHistory;
import nl.amila.badminton.manager.repository.*;
import nl.amila.badminton.manager.repository.league.LeagueGameDayGroupMatchRepository;
import nl.amila.badminton.manager.repository.league.LeagueGameDayRepository;
import nl.amila.badminton.manager.repository.league.LeagueTournamentSettingsRepository;
import nl.amila.badminton.manager.repository.league.RankScoreHistoryRepository;
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
            tournament, RankingLogic.MODIFIED_ELO, new ModifiedEloConfig(k));
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

    // ── getPlayerHistory ──────────────────────────────────────────────────────

    @Test
    void getPlayerHistory_success_returnsGroupedByGameDay() {
        gameDay.setStatus(GameDayStatus.COMPLETED);

        RankScoreHistory h1 = new RankScoreHistory(tp1, match,
            new BigDecimal("100.00"), new BigDecimal("115.00"));
        when(tournamentPlayerRepository.findById(1L)).thenReturn(Optional.of(tp1));
        when(rankScoreHistoryRepository.findByTournamentPlayerIdOrderByChangedAtDesc(1L))
            .thenReturn(List.of(h1));

        PlayerHistoryResponse res = service.getPlayerHistory(1L, 1L);

        assertTrue(res.isSuccess());
        assertEquals("Alice A", res.getPlayerName());
        assertEquals(1, res.getGameDays().size());
        PlayerHistoryResponse.GameDayHistoryDto day = res.getGameDays().get(0);
        assertEquals("2025-01-10", day.getGameDate());
        assertEquals(1, day.getMatches().size());
        PlayerHistoryResponse.MatchHistoryDto m = day.getMatches().get(0);
        assertEquals(new BigDecimal("100.00"), m.getPreviousScore());
        assertEquals(new BigDecimal("115.00"), m.getNewScore());
        assertEquals(new BigDecimal("15.00"), m.getScoreDelta());
        assertTrue(m.isPlayerOnTeam1());
    }

    @Test
    void getPlayerHistory_playerOnTeam2_flagsCorrectly() {
        gameDay.setStatus(GameDayStatus.COMPLETED);

        // tp3 is on team2 in the match fixture
        RankScoreHistory h = new RankScoreHistory(tp3, match,
            new BigDecimal("60.00"), new BigDecimal("45.00"));
        when(tournamentPlayerRepository.findById(3L)).thenReturn(Optional.of(tp3));
        when(rankScoreHistoryRepository.findByTournamentPlayerIdOrderByChangedAtDesc(3L))
            .thenReturn(List.of(h));

        PlayerHistoryResponse res = service.getPlayerHistory(1L, 3L);

        assertTrue(res.isSuccess());
        assertFalse(res.getGameDays().get(0).getMatches().get(0).isPlayerOnTeam1());
    }

    @Test
    void getPlayerHistory_emptyHistory_returnsEmptyList() {
        when(tournamentPlayerRepository.findById(1L)).thenReturn(Optional.of(tp1));
        when(rankScoreHistoryRepository.findByTournamentPlayerIdOrderByChangedAtDesc(1L))
            .thenReturn(Collections.emptyList());

        PlayerHistoryResponse res = service.getPlayerHistory(1L, 1L);

        assertTrue(res.isSuccess());
        assertTrue(res.getGameDays().isEmpty());
    }

    @Test
    void getPlayerHistory_playerNotFound_returnsError() {
        when(tournamentPlayerRepository.findById(99L)).thenReturn(Optional.empty());

        PlayerHistoryResponse res = service.getPlayerHistory(1L, 99L);

        assertFalse(res.isSuccess());
        assertEquals("Player not found in this tournament", res.getMessage());
    }

    @Test
    void getPlayerHistory_playerBelongsToDifferentTournament_returnsError() {
        Tournament otherTournament = new Tournament("Other", 1L, true, TournamentType.LEAGUE);
        setId(otherTournament, 99L);
        TournamentPlayer otherTp = new TournamentPlayer(otherTournament, tp1.getUser());
        setId(otherTp, 5L);

        when(tournamentPlayerRepository.findById(5L)).thenReturn(Optional.of(otherTp));

        // Request history for tournament 1 but player belongs to tournament 99
        PlayerHistoryResponse res = service.getPlayerHistory(1L, 5L);

        assertFalse(res.isSuccess());
        assertEquals("Player not found in this tournament", res.getMessage());
    }
}



