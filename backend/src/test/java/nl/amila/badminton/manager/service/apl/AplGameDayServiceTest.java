package nl.amila.badminton.manager.service.apl;

import nl.amila.badminton.manager.dto.apl.AplGameDayResponse;
import nl.amila.badminton.manager.entity.*;
import nl.amila.badminton.manager.entity.apl.*;
import nl.amila.badminton.manager.repository.TournamentPlayerRepository;
import nl.amila.badminton.manager.repository.TournamentRepository;
import nl.amila.badminton.manager.repository.UserRepository;
import nl.amila.badminton.manager.repository.apl.*;
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
class AplGameDayServiceTest {

    @Mock private TournamentRepository tournamentRepository;
    @Mock private TournamentPlayerRepository tournamentPlayerRepository;
    @Mock private AplGameDayRepository aplGameDayRepository;
    @Mock private AplGameDayGroupMatchRepository matchRepository;
    @Mock private UserRepository userRepository;
    @Mock private AplTournamentSettingsRepository aplSettingsRepository;
    @Mock private AplRankScoreHistoryRepository aplRankScoreHistoryRepository;

    private AplGameDayService service;

    private Tournament tournament;
    private User adminUser;
    private AplGameDay gameDay;
    private AplGameDayGroup group;
    private TournamentPlayer tp1, tp2, tp3, tp4;
    private AplGameDayGroupPlayer gp1, gp2, gp3, gp4;
    private AplGameDayGroupMatch match;

    @BeforeEach
    void setUp() {
        service = new AplGameDayService(
            tournamentRepository, tournamentPlayerRepository,
            aplGameDayRepository, matchRepository,
            userRepository, aplSettingsRepository, aplRankScoreHistoryRepository
        );

        adminUser = new User("admin", "admin@test.com", "pass", "Admin", "User");
        adminUser.setRole(Role.ADMIN);

        tournament = new Tournament("Test APL", 1L, true, TournamentType.APL);
        setId(tournament, 1L);

        gameDay = new AplGameDay(tournament, LocalDate.of(2025, 1, 10));
        setId(gameDay, 1L);
        gameDay.setStatus(AplGameDayStatus.ONGOING);

        group = new AplGameDayGroup(gameDay, 1);

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

        gp1 = new AplGameDayGroupPlayer(group, tp1);
        gp2 = new AplGameDayGroupPlayer(group, tp2);
        gp3 = new AplGameDayGroupPlayer(group, tp3);
        gp4 = new AplGameDayGroupPlayer(group, tp4);

        match = new AplGameDayGroupMatch(group, 1, gp1, gp2, gp3, gp4);
        match.setTeam1Score(21);
        match.setTeam2Score(15);
        setId(match, 10L);

        group.getPlayers().add(gp1);
        group.getPlayers().add(gp2);
        group.getPlayers().add(gp3);
        group.getPlayers().add(gp4);
        group.getMatches().add(match);
        gameDay.getGroups().add(group);
    }

    // ── finishGameDay: happy path ──────────────────────────────────────────────

    @Test
    void finishGameDay_success_marksCompleted() {
        stubHappyPath("10,8,5", null);

        AplGameDayResponse res = service.finishGameDay(1L, 1L, "admin");

        assertTrue(res.isSuccess());
        assertEquals(AplGameDayStatus.COMPLETED, gameDay.getStatus());
        verify(aplGameDayRepository, atLeastOnce()).save(gameDay);
    }

    @Test
    void finishGameDay_success_updatesRankScoresForParticipants() {
        stubHappyPath("10,8,5", null);

        service.finishGameDay(1L, 1L, "admin");

        // At minimum the 4 participating players get their ELO scores saved
        verify(tournamentPlayerRepository, atLeast(4)).save(any(TournamentPlayer.class));
    }

    // ── absence demerit ───────────────────────────────────────────────────────

    @Test
    void finishGameDay_absentPlayer_demeritDeductedFromRankScore() {
        // tp5 is absent (not in any group)
        User u5 = new User("p5", "p5@test.com", "p", "Eve", "E");
        setId(u5, 5L);
        TournamentPlayer tp5 = new TournamentPlayer(tournament, u5, new BigDecimal("50.00"));
        setId(tp5, 5L);

        stubHappyPath("10,8,5", null);
        // Return tp5 as additional tournament player (will be absent)
        when(tournamentPlayerRepository.findByTournamentId(1L))
            .thenReturn(List.of(tp1, tp2, tp3, tp4, tp5));
        // No previous completed game days → consecutive absences = 0 → after this day = 1 → index 0 = 10pts
        when(aplGameDayRepository.findByTournamentIdOrderByGameDateDesc(1L))
            .thenReturn(List.of(gameDay));

        service.finishGameDay(1L, 1L, "admin");

        assertEquals(new BigDecimal("40.00"), tp5.getRankScore()); // 50 - 10
    }

    @Test
    void finishGameDay_consecutiveAbsences_higherIndexDemeritUsed() {
        User u5 = new User("p5", "p5@test.com", "p", "Eve", "E");
        setId(u5, 5L);
        TournamentPlayer tp5 = new TournamentPlayer(tournament, u5, new BigDecimal("50.00"));
        setId(tp5, 5L);

        // A previous completed game day where tp5 was also absent
        AplGameDay prevDay = new AplGameDay(tournament, LocalDate.of(2025, 1, 3));
        setId(prevDay, 2L);
        prevDay.setStatus(AplGameDayStatus.COMPLETED);

        stubHappyPath("10,8,5", null);
        when(tournamentPlayerRepository.findByTournamentId(1L))
            .thenReturn(List.of(tp1, tp2, tp3, tp4, tp5));
        // History: current day first (skipped), then prevDay (completed, absent → count=1 before adding current)
        when(aplGameDayRepository.findByTournamentIdOrderByGameDateDesc(1L))
            .thenReturn(List.of(gameDay, prevDay));
        when(aplGameDayRepository.isPlayerPresentInGameDay(2L, 5L)).thenReturn(false);

        service.finishGameDay(1L, 1L, "admin");

        // consecutiveAbsences in prior history = 1, +1 for current day = 2 → index 1 = 8 pts deducted
        assertEquals(new BigDecimal("42.00"), tp5.getRankScore()); // 50 - 8
    }

    @Test
    void finishGameDay_consecutiveAbsencesMeetDeactivationCount_playerDisabled() {
        User u5 = new User("p5", "p5@test.com", "p", "Eve", "E");
        setId(u5, 5L);
        TournamentPlayer tp5 = new TournamentPlayer(tournament, u5, new BigDecimal("50.00"));
        setId(tp5, 5L);

        // deactivationCount = 1 → one consecutive absence disables the player
        stubHappyPath("10,8,5", 1);
        when(tournamentPlayerRepository.findByTournamentId(1L))
            .thenReturn(List.of(tp1, tp2, tp3, tp4, tp5));
        when(aplGameDayRepository.findByTournamentIdOrderByGameDateDesc(1L))
            .thenReturn(List.of(gameDay));

        service.finishGameDay(1L, 1L, "admin");

        assertEquals(PlayerStatus.DISABLED, tp5.getStatus());
    }

    @Test
    void finishGameDay_noAbsenteeDemeritConfig_skipsAbsenceProcessing() {
        User u5 = new User("p5", "p5@test.com", "p", "Eve", "E");
        setId(u5, 5L);
        TournamentPlayer tp5 = new TournamentPlayer(tournament, u5, new BigDecimal("50.00"));
        setId(tp5, 5L);

        // Both demerit fields null → absence processing must be skipped entirely
        stubHappyPath(null, null);

        service.finishGameDay(1L, 1L, "admin");

        // findByTournamentId should never be called when there is no config
        verify(tournamentPlayerRepository, never()).findByTournamentId(1L);
        assertEquals(new BigDecimal("50.00"), tp5.getRankScore());
        assertEquals(PlayerStatus.ENABLED, tp5.getStatus());
    }

    @Test
    void finishGameDay_alreadyDisabledPlayer_skippedInAbsenceProcessing() {
        User u5 = new User("p5", "p5@test.com", "p", "Eve", "E");
        setId(u5, 5L);
        TournamentPlayer tp5 = new TournamentPlayer(tournament, u5, new BigDecimal("50.00"));
        setId(tp5, 5L);
        tp5.setStatus(PlayerStatus.DISABLED);

        stubHappyPath("10,8,5", 1);
        when(tournamentPlayerRepository.findByTournamentId(1L))
            .thenReturn(List.of(tp1, tp2, tp3, tp4, tp5));

        service.finishGameDay(1L, 1L, "admin");

        // Score must not change; status stays DISABLED
        assertEquals(new BigDecimal("50.00"), tp5.getRankScore());
        assertEquals(PlayerStatus.DISABLED, tp5.getStatus());
    }

    // ── error paths ───────────────────────────────────────────────────────────

    @Test
    void finishGameDay_gameDayNotFound_returnsError() {
        when(aplGameDayRepository.findByIdWithAll(99L)).thenReturn(Optional.empty());

        AplGameDayResponse res = service.finishGameDay(1L, 99L, "admin");

        assertFalse(res.isSuccess());
    }

    @Test
    void finishGameDay_notOngoing_returnsError() {
        gameDay.setStatus(AplGameDayStatus.COMPLETED);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(aplGameDayRepository.findByIdWithAll(1L)).thenReturn(Optional.of(gameDay));

        AplGameDayResponse res = service.finishGameDay(1L, 1L, "admin");

        assertFalse(res.isSuccess());
        assertTrue(res.getMessage().contains("ONGOING"));
    }

    @Test
    void finishGameDay_missingScore_returnsError() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        match.setTeam1Score(null);
        when(aplGameDayRepository.findByIdWithAll(1L)).thenReturn(Optional.of(gameDay));

        AplGameDayResponse res = service.finishGameDay(1L, 1L, "admin");

        assertFalse(res.isSuccess());
        assertTrue(res.getMessage().contains("score"));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /**
     * Wire up the standard stubs required for a successful finishGameDay call.
     *
     * @param absenteeDemeritPoints comma-separated demerit list, or null
     * @param deactivationCount     threshold for auto-disable, or null
     */
    private void stubHappyPath(String absenteeDemeritPoints, Integer deactivationCount) {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(aplGameDayRepository.findByIdWithAll(1L)).thenReturn(Optional.of(gameDay));

        AplTournamentSettings settings = new AplTournamentSettings(
            tournament, RankingLogic.MODIFIED_ELO, new ModifiedEloConfig(32),
            absenteeDemeritPoints, deactivationCount);
        when(aplSettingsRepository.findByTournamentId(1L)).thenReturn(Optional.of(settings));

        // After ELO updates, each player is fetched by ID for delta application
        when(tournamentPlayerRepository.findById(1L)).thenReturn(Optional.of(tp1));
        when(tournamentPlayerRepository.findById(2L)).thenReturn(Optional.of(tp2));
        when(tournamentPlayerRepository.findById(3L)).thenReturn(Optional.of(tp3));
        when(tournamentPlayerRepository.findById(4L)).thenReturn(Optional.of(tp4));

        when(aplGameDayRepository.findByIdWithAll(1L)).thenReturn(Optional.of(gameDay));

        // Default: findByTournamentId returns only the 4 participants (no absent players).
        // Only stub when absence processing will actually run (config is non-null).
        if (absenteeDemeritPoints != null || deactivationCount != null) {
            when(tournamentPlayerRepository.findByTournamentId(1L))
                .thenReturn(List.of(tp1, tp2, tp3, tp4));
        }
    }

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
