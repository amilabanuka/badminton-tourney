package nl.amila.badminton.manager.service;

import nl.amila.badminton.manager.dto.*;
import nl.amila.badminton.manager.entity.LeagueTournamentSettings;
import nl.amila.badminton.manager.entity.ModifiedEloConfig;
import nl.amila.badminton.manager.entity.OneOffTournamentSettings;
import nl.amila.badminton.manager.entity.RankingLogic;
import nl.amila.badminton.manager.entity.Role;
import nl.amila.badminton.manager.entity.Tournament;
import nl.amila.badminton.manager.entity.TournamentAdmin;
import nl.amila.badminton.manager.entity.TournamentPlayer;
import nl.amila.badminton.manager.entity.TournamentType;
import nl.amila.badminton.manager.entity.User;
import nl.amila.badminton.manager.repository.LeagueTournamentSettingsRepository;
import nl.amila.badminton.manager.repository.OneOffTournamentSettingsRepository;
import nl.amila.badminton.manager.repository.TournamentPlayerRepository;
import nl.amila.badminton.manager.repository.TournamentRepository;
import nl.amila.badminton.manager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TournamentServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TournamentPlayerRepository tournamentPlayerRepository;

    @Mock
    private LeagueTournamentSettingsRepository leagueSettingsRepository;

    @Mock
    private OneOffTournamentSettingsRepository oneOffSettingsRepository;

    @InjectMocks
    private TournamentService tournamentService;

    private User adminUser;
    private User tournamentAdminUser;
    private User playerUser;
    private User playerUser2;
    private Tournament tournament;
    private Tournament leagueTournament;

    @BeforeEach
    void setUp() {
        adminUser = new User("admin", "admin@example.com", "password", "Admin", "User");
        adminUser.setId(1L);
        adminUser.setRole(Role.ADMIN);

        tournamentAdminUser = new User("tourny_admin", "tourny@example.com", "password", "Tourny", "Admin");
        tournamentAdminUser.setId(2L);
        tournamentAdminUser.setRole(Role.TOURNY_ADMIN);

        playerUser = new User("player", "player@example.com", "password", "Player", "User");
        playerUser.setId(3L);
        playerUser.setRole(Role.PLAYER);

        playerUser2 = new User("player2", "player2@example.com", "password", "Player", "Two");
        playerUser2.setId(4L);
        playerUser2.setRole(Role.PLAYER);

        tournament = new Tournament("Spring Championship", 2L, true, TournamentType.ONE_OFF);
        tournament.setId(1L);

        leagueTournament = new Tournament("Premier League", 2L, true, TournamentType.LEAGUE);
        leagueTournament.setId(2L);
    }

    // --- helpers ---

    private CreateTournamentRequest oneOffRequest(String name) {
        CreateTournamentRequest req = new CreateTournamentRequest();
        req.setName(name);
        req.setOwnerId(2L);
        req.setEnabled(true);
        req.setType(TournamentType.ONE_OFF);
        OneOffSettingsRequest os = new OneOffSettingsRequest();
        os.setNumberOfRounds(1);
        os.setMaxPoints(21);
        req.setOneOffSettings(os);
        return req;
    }

    private CreateTournamentRequest leagueRequest(String name) {
        CreateTournamentRequest req = new CreateTournamentRequest();
        req.setName(name);
        req.setOwnerId(2L);
        req.setEnabled(true);
        req.setType(TournamentType.LEAGUE);
        LeagueSettingsRequest ls = new LeagueSettingsRequest();
        ls.setRankingLogic(RankingLogic.MODIFIED_ELO);
        ls.setK(32);
        ls.setAbsenteeDemerit(5);
        req.setLeagueSettings(ls);
        return req;
    }

    // -------------------------------------------------------------------------
    // createTournament — existing tests (updated to use helper / setter pattern)
    // -------------------------------------------------------------------------

    @Test
    void testCreateTournament_Success() {
        CreateTournamentRequest request = oneOffRequest("Spring Championship");
        when(tournamentRepository.existsByName("Spring Championship")).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(tournamentAdminUser));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);
        when(oneOffSettingsRepository.save(any(OneOffTournamentSettings.class))).thenReturn(null);

        TournamentResponse response = tournamentService.createTournament(request);

        assertTrue(response.isSuccess());
        assertEquals("Tournament created successfully", response.getMessage());
        assertNotNull(response.getTournament());
        assertEquals("Spring Championship", response.getTournament().getName());
        verify(tournamentRepository, times(1)).save(any(Tournament.class));
        verify(oneOffSettingsRepository, times(1)).save(any(OneOffTournamentSettings.class));
    }

    @Test
    void testCreateTournament_NameAlreadyExists() {
        CreateTournamentRequest request = new CreateTournamentRequest();
        request.setName("Spring Championship");
        request.setOwnerId(2L);
        request.setType(TournamentType.ONE_OFF);
        when(tournamentRepository.existsByName("Spring Championship")).thenReturn(true);

        TournamentResponse response = tournamentService.createTournament(request);

        assertFalse(response.isSuccess());
        assertEquals("Tournament name already exists", response.getMessage());
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    void testCreateTournament_OwnerNotFound() {
        CreateTournamentRequest request = new CreateTournamentRequest();
        request.setName("Spring Championship");
        request.setOwnerId(999L);
        request.setType(TournamentType.ONE_OFF);
        when(tournamentRepository.existsByName("Spring Championship")).thenReturn(false);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        TournamentResponse response = tournamentService.createTournament(request);

        assertFalse(response.isSuccess());
        assertEquals("Owner user not found", response.getMessage());
    }

    @Test
    void testCreateTournament_OwnerNotTournyAdmin() {
        CreateTournamentRequest request = new CreateTournamentRequest();
        request.setName("Spring Championship");
        request.setOwnerId(3L);
        request.setType(TournamentType.ONE_OFF);
        when(tournamentRepository.existsByName("Spring Championship")).thenReturn(false);
        when(userRepository.findById(3L)).thenReturn(Optional.of(playerUser));

        TournamentResponse response = tournamentService.createTournament(request);

        assertFalse(response.isSuccess());
        assertEquals("Owner must have TOURNY_ADMIN role", response.getMessage());
    }

    // -------------------------------------------------------------------------
    // createTournament — LEAGUE settings validation
    // -------------------------------------------------------------------------

    @Test
    void createTournament_league_validEloSettings_savesLeagueSettingsAndReturnsSuccess() {
        CreateTournamentRequest request = leagueRequest("Premier League");
        when(tournamentRepository.existsByName("Premier League")).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(tournamentAdminUser));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(leagueTournament);
        when(leagueSettingsRepository.save(any(LeagueTournamentSettings.class))).thenReturn(null);

        TournamentResponse response = tournamentService.createTournament(request);

        assertTrue(response.isSuccess());
        verify(leagueSettingsRepository, times(1)).save(any(LeagueTournamentSettings.class));
        verify(oneOffSettingsRepository, never()).save(any());
    }

    @Test
    void createTournament_league_missingLeagueSettings_returnsError() {
        CreateTournamentRequest request = new CreateTournamentRequest();
        request.setName("Premier League");
        request.setOwnerId(2L);
        request.setType(TournamentType.LEAGUE);
        when(tournamentRepository.existsByName("Premier League")).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(tournamentAdminUser));

        TournamentResponse response = tournamentService.createTournament(request);

        assertFalse(response.isSuccess());
        assertEquals("League settings are required", response.getMessage());
        verify(tournamentRepository, never()).save(any());
    }

    @Test
    void createTournament_league_nullRankingLogic_returnsError() {
        CreateTournamentRequest request = new CreateTournamentRequest();
        request.setName("Premier League");
        request.setOwnerId(2L);
        request.setType(TournamentType.LEAGUE);
        LeagueSettingsRequest ls = new LeagueSettingsRequest();
        ls.setK(32);
        ls.setAbsenteeDemerit(5);
        // rankingLogic is null
        request.setLeagueSettings(ls);
        when(tournamentRepository.existsByName("Premier League")).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(tournamentAdminUser));

        TournamentResponse response = tournamentService.createTournament(request);

        assertFalse(response.isSuccess());
        assertEquals("Ranking logic is required", response.getMessage());
    }

    @Test
    void createTournament_league_zeroK_returnsError() {
        CreateTournamentRequest request = new CreateTournamentRequest();
        request.setName("Premier League");
        request.setOwnerId(2L);
        request.setType(TournamentType.LEAGUE);
        LeagueSettingsRequest ls = new LeagueSettingsRequest();
        ls.setRankingLogic(RankingLogic.MODIFIED_ELO);
        ls.setK(0);
        ls.setAbsenteeDemerit(5);
        request.setLeagueSettings(ls);
        when(tournamentRepository.existsByName("Premier League")).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(tournamentAdminUser));

        TournamentResponse response = tournamentService.createTournament(request);

        assertFalse(response.isSuccess());
        assertEquals("k must be a positive integer", response.getMessage());
    }

    @Test
    void createTournament_league_negativeAbsenteeDemerit_returnsError() {
        CreateTournamentRequest request = new CreateTournamentRequest();
        request.setName("Premier League");
        request.setOwnerId(2L);
        request.setType(TournamentType.LEAGUE);
        LeagueSettingsRequest ls = new LeagueSettingsRequest();
        ls.setRankingLogic(RankingLogic.MODIFIED_ELO);
        ls.setK(32);
        ls.setAbsenteeDemerit(-1);
        request.setLeagueSettings(ls);
        when(tournamentRepository.existsByName("Premier League")).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(tournamentAdminUser));

        TournamentResponse response = tournamentService.createTournament(request);

        assertFalse(response.isSuccess());
        assertEquals("Absentee demerit must be non-negative", response.getMessage());
    }

    // -------------------------------------------------------------------------
    // createTournament — ONE_OFF settings validation
    // -------------------------------------------------------------------------

    @Test
    void createTournament_oneOff_validSettings_savesOneOffSettingsAndReturnsSuccess() {
        CreateTournamentRequest request = oneOffRequest("Summer Open");
        when(tournamentRepository.existsByName("Summer Open")).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(tournamentAdminUser));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);
        when(oneOffSettingsRepository.save(any(OneOffTournamentSettings.class))).thenReturn(null);

        TournamentResponse response = tournamentService.createTournament(request);

        assertTrue(response.isSuccess());
        verify(oneOffSettingsRepository, times(1)).save(any(OneOffTournamentSettings.class));
        verify(leagueSettingsRepository, never()).save(any());
    }

    @Test
    void createTournament_oneOff_missingOneOffSettings_returnsError() {
        CreateTournamentRequest request = new CreateTournamentRequest();
        request.setName("Summer Open");
        request.setOwnerId(2L);
        request.setType(TournamentType.ONE_OFF);
        when(tournamentRepository.existsByName("Summer Open")).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(tournamentAdminUser));

        TournamentResponse response = tournamentService.createTournament(request);

        assertFalse(response.isSuccess());
        assertEquals("One-off settings are required", response.getMessage());
        verify(tournamentRepository, never()).save(any());
    }

    @Test
    void createTournament_oneOff_invalidMaxPoints_returnsError() {
        CreateTournamentRequest request = new CreateTournamentRequest();
        request.setName("Summer Open");
        request.setOwnerId(2L);
        request.setType(TournamentType.ONE_OFF);
        OneOffSettingsRequest os = new OneOffSettingsRequest();
        os.setNumberOfRounds(3);
        os.setMaxPoints(30); // invalid
        request.setOneOffSettings(os);
        when(tournamentRepository.existsByName("Summer Open")).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(tournamentAdminUser));

        TournamentResponse response = tournamentService.createTournament(request);

        assertFalse(response.isSuccess());
        assertEquals("Max points must be 15 or 21", response.getMessage());
    }

    @Test
    void createTournament_oneOff_zeroRounds_returnsError() {
        CreateTournamentRequest request = new CreateTournamentRequest();
        request.setName("Summer Open");
        request.setOwnerId(2L);
        request.setType(TournamentType.ONE_OFF);
        OneOffSettingsRequest os = new OneOffSettingsRequest();
        os.setNumberOfRounds(0);
        os.setMaxPoints(21);
        request.setOneOffSettings(os);
        when(tournamentRepository.existsByName("Summer Open")).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(tournamentAdminUser));

        TournamentResponse response = tournamentService.createTournament(request);

        assertFalse(response.isSuccess());
        assertEquals("Number of rounds must be positive", response.getMessage());
    }

    // -------------------------------------------------------------------------
    // getTournamentById — settings included in detail response
    // -------------------------------------------------------------------------

    @Test
    void getTournamentById_leagueTournament_includesEloSettingsInDto() {
        LeagueTournamentSettings ls = new LeagueTournamentSettings(
            leagueTournament, RankingLogic.MODIFIED_ELO, new ModifiedEloConfig(32, 5));
        leagueTournament.setLeagueSettings(ls);

        when(tournamentRepository.findById(2L)).thenReturn(Optional.of(leagueTournament));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        TournamentResponse response = tournamentService.getTournamentById(2L, "admin");

        assertTrue(response.isSuccess());
        TournamentResponse.TournamentSettingsDto settings = response.getTournament().getSettings();
        assertNotNull(settings);
        assertEquals(RankingLogic.MODIFIED_ELO, settings.getRankingLogic());
        assertEquals(32, settings.getK());
        assertEquals(5, settings.getAbsenteeDemerit());
        assertNull(settings.getNumberOfRounds());
        assertNull(settings.getMaxPoints());
    }

    @Test
    void getTournamentById_oneOffTournament_includesOneOffSettingsInDto() {
        OneOffTournamentSettings os = new OneOffTournamentSettings(tournament, 3, 21);
        tournament.setOneOffSettings(os);

        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        TournamentResponse response = tournamentService.getTournamentById(1L, "admin");

        assertTrue(response.isSuccess());
        TournamentResponse.TournamentSettingsDto settings = response.getTournament().getSettings();
        assertNotNull(settings);
        assertEquals(3, settings.getNumberOfRounds());
        assertEquals(21, settings.getMaxPoints());
        assertNull(settings.getRankingLogic());
        assertNull(settings.getK());
    }

    // -------------------------------------------------------------------------
    // updateTournamentSettings
    // -------------------------------------------------------------------------

    @Test
    void updateTournamentSettings_league_validRequest_updatesConfigAndReturnsDto() {
        LeagueTournamentSettings ls = new LeagueTournamentSettings(
            leagueTournament, RankingLogic.MODIFIED_ELO, new ModifiedEloConfig(32, 5));
        leagueTournament.setLeagueSettings(ls);

        UpdateTournamentSettingsRequest request = new UpdateTournamentSettingsRequest();
        request.setK(20);
        request.setAbsenteeDemerit(10);

        when(tournamentRepository.findById(2L)).thenReturn(Optional.of(leagueTournament));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(leagueSettingsRepository.findByTournamentId(2L)).thenReturn(Optional.of(ls));
        when(leagueSettingsRepository.save(any(LeagueTournamentSettings.class))).thenReturn(ls);
        when(tournamentRepository.findById(2L)).thenReturn(Optional.of(leagueTournament));

        TournamentResponse response = tournamentService.updateTournamentSettings(2L, request, "admin");

        assertTrue(response.isSuccess());
        assertEquals("Tournament settings updated successfully", response.getMessage());
        verify(leagueSettingsRepository, times(1)).save(any(LeagueTournamentSettings.class));
    }

    @Test
    void updateTournamentSettings_league_invalidK_returnsError() {
        LeagueTournamentSettings ls = new LeagueTournamentSettings(
            leagueTournament, RankingLogic.MODIFIED_ELO, new ModifiedEloConfig(32, 5));

        UpdateTournamentSettingsRequest request = new UpdateTournamentSettingsRequest();
        request.setK(0);
        request.setAbsenteeDemerit(5);

        when(tournamentRepository.findById(2L)).thenReturn(Optional.of(leagueTournament));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(leagueSettingsRepository.findByTournamentId(2L)).thenReturn(Optional.of(ls));

        TournamentResponse response = tournamentService.updateTournamentSettings(2L, request, "admin");

        assertFalse(response.isSuccess());
        assertEquals("k must be a positive integer", response.getMessage());
        verify(leagueSettingsRepository, never()).save(any());
    }

    @Test
    void updateTournamentSettings_oneOff_validRequest_updatesFieldsAndReturnsDto() {
        OneOffTournamentSettings os = new OneOffTournamentSettings(tournament, 1, 21);
        tournament.setOneOffSettings(os);

        UpdateTournamentSettingsRequest request = new UpdateTournamentSettingsRequest();
        request.setNumberOfRounds(5);
        request.setMaxPoints(15);

        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(oneOffSettingsRepository.findByTournamentId(1L)).thenReturn(Optional.of(os));
        when(oneOffSettingsRepository.save(any(OneOffTournamentSettings.class))).thenReturn(os);

        TournamentResponse response = tournamentService.updateTournamentSettings(1L, request, "admin");

        assertTrue(response.isSuccess());
        verify(oneOffSettingsRepository, times(1)).save(any(OneOffTournamentSettings.class));
    }

    @Test
    void updateTournamentSettings_oneOff_invalidMaxPoints_returnsError() {
        OneOffTournamentSettings os = new OneOffTournamentSettings(tournament, 1, 21);

        UpdateTournamentSettingsRequest request = new UpdateTournamentSettingsRequest();
        request.setNumberOfRounds(3);
        request.setMaxPoints(30);

        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(oneOffSettingsRepository.findByTournamentId(1L)).thenReturn(Optional.of(os));

        TournamentResponse response = tournamentService.updateTournamentSettings(1L, request, "admin");

        assertFalse(response.isSuccess());
        assertEquals("Max points must be 15 or 21", response.getMessage());
        verify(oneOffSettingsRepository, never()).save(any());
    }

    @Test
    void updateTournamentSettings_tournamentNotFound_returnsError() {
        when(tournamentRepository.findById(99L)).thenReturn(Optional.empty());

        TournamentResponse response = tournamentService.updateTournamentSettings(
            99L, new UpdateTournamentSettingsRequest(), "admin");

        assertFalse(response.isSuccess());
        assertEquals("Tournament not found", response.getMessage());
    }

    @Test
    void updateTournamentSettings_tourneyAdminNotOfThisTournament_throwsAccessDenied() {
        // tourny_admin is NOT in the tournament's admins list
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(userRepository.findByUsername("tourny_admin")).thenReturn(Optional.of(tournamentAdminUser));

        assertThrows(AccessDeniedException.class,
            () -> tournamentService.updateTournamentSettings(1L, new UpdateTournamentSettingsRequest(), "tourny_admin"));
    }

    // -------------------------------------------------------------------------
    // addTournamentAdmin
    // -------------------------------------------------------------------------

    @Test
    void testAddTournamentAdmin_Success() {
        AddTournamentAdminRequest request = new AddTournamentAdminRequest(2L);
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(userRepository.findById(2L)).thenReturn(Optional.of(tournamentAdminUser));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        TournamentResponse response = tournamentService.addTournamentAdmin(1L, request);

        assertTrue(response.isSuccess());
        assertEquals("Tournament admin added successfully", response.getMessage());
        verify(tournamentRepository, times(1)).save(any(Tournament.class));
    }

    @Test
    void testAddTournamentAdmin_UserNotTournyAdmin() {
        AddTournamentAdminRequest request = new AddTournamentAdminRequest(3L);
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(userRepository.findById(3L)).thenReturn(Optional.of(playerUser));

        TournamentResponse response = tournamentService.addTournamentAdmin(1L, request);

        assertFalse(response.isSuccess());
        assertEquals("User must have TOURNY_ADMIN role", response.getMessage());
    }

    @Test
    void testAddTournamentAdmin_AlreadyAdmin() {
        tournament.getAdmins().add(new TournamentAdmin(tournament, tournamentAdminUser));
        AddTournamentAdminRequest request = new AddTournamentAdminRequest(2L);
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(userRepository.findById(2L)).thenReturn(Optional.of(tournamentAdminUser));

        TournamentResponse response = tournamentService.addTournamentAdmin(1L, request);

        assertFalse(response.isSuccess());
        assertEquals("User is already a tournament admin", response.getMessage());
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    // -------------------------------------------------------------------------
    // addTournamentPlayer
    // -------------------------------------------------------------------------

    @Test
    void testAddTournamentPlayer_Success() {
        AddTournamentPlayerRequest request = new AddTournamentPlayerRequest(3L);
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(userRepository.findById(3L)).thenReturn(Optional.of(playerUser));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        TournamentResponse response = tournamentService.addTournamentPlayer(1L, request);

        assertTrue(response.isSuccess());
        assertEquals("Tournament player added successfully", response.getMessage());
        verify(tournamentRepository, times(1)).save(any(Tournament.class));
    }

    @Test
    void testAddTournamentPlayer_UserNotPlayer() {
        AddTournamentPlayerRequest request = new AddTournamentPlayerRequest(2L);
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(userRepository.findById(2L)).thenReturn(Optional.of(tournamentAdminUser));

        TournamentResponse response = tournamentService.addTournamentPlayer(1L, request);

        assertFalse(response.isSuccess());
        assertEquals("User must have PLAYER role", response.getMessage());
    }

    // -------------------------------------------------------------------------
    // removeTournamentAdmin
    // -------------------------------------------------------------------------

    @Test
    void testRemoveTournamentAdmin_Success() {
        tournament.getAdmins().add(new TournamentAdmin(tournament, tournamentAdminUser));
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        TournamentResponse response = tournamentService.removeTournamentAdmin(1L, 2L);

        assertTrue(response.isSuccess());
        assertEquals("Tournament admin removed successfully", response.getMessage());
        verify(tournamentRepository, times(1)).save(any(Tournament.class));
    }

    @Test
    void testRemoveTournamentAdmin_NotFound() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));

        TournamentResponse response = tournamentService.removeTournamentAdmin(1L, 99L);

        assertFalse(response.isSuccess());
        assertEquals("User is not a tournament admin", response.getMessage());
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    // -------------------------------------------------------------------------
    // removeTournamentPlayer — removal is not supported
    // -------------------------------------------------------------------------

    @Test
    void testRemoveTournamentPlayer_AlwaysReturnsFalse() {
        TournamentResponse response = tournamentService.removeTournamentPlayer(1L, 3L);

        assertFalse(response.isSuccess());
        assertEquals("Players cannot be removed from a tournament. Use disable instead.", response.getMessage());
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    void testRemoveTournamentPlayer_NonExistentPlayer_AlsoReturnsFalse() {
        TournamentResponse response = tournamentService.removeTournamentPlayer(1L, 99L);

        assertFalse(response.isSuccess());
        assertEquals("Players cannot be removed from a tournament. Use disable instead.", response.getMessage());
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    // -------------------------------------------------------------------------
    // getTournaments — role-scoped
    // -------------------------------------------------------------------------

    @Test
    void testGetTournaments_AsAdmin_ReturnsAll() {
        List<Tournament> tournaments = new ArrayList<>();
        tournaments.add(tournament);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(tournamentRepository.findAll()).thenReturn(tournaments);

        TournamentResponse response = tournamentService.getTournaments("admin");

        assertTrue(response.isSuccess());
        assertEquals("Tournaments retrieved successfully", response.getMessage());
        assertNotNull(response.getTournaments());
        assertEquals(1, response.getTournaments().size());
        verify(tournamentRepository, times(1)).findAll();
        verify(tournamentRepository, never()).findByAdminsUserId(anyLong());
    }

    @Test
    void testGetTournaments_AsTournyAdmin_ReturnsOnlyOwn() {
        tournament.getAdmins().add(new TournamentAdmin(tournament, tournamentAdminUser));
        when(userRepository.findByUsername("tourny_admin")).thenReturn(Optional.of(tournamentAdminUser));
        when(tournamentRepository.findByAdminsUserId(2L)).thenReturn(List.of(tournament));

        TournamentResponse response = tournamentService.getTournaments("tourny_admin");

        assertTrue(response.isSuccess());
        assertEquals(1, response.getTournaments().size());
        assertEquals("Spring Championship", response.getTournaments().get(0).getName());
        verify(tournamentRepository, times(1)).findByAdminsUserId(2L);
        verify(tournamentRepository, never()).findAll();
    }

    // -------------------------------------------------------------------------
    // getTournamentById — role-scoped
    // -------------------------------------------------------------------------

    @Test
    void testGetTournamentById_AsAdmin_Success() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        TournamentResponse response = tournamentService.getTournamentById(1L, "admin");

        assertTrue(response.isSuccess());
        assertEquals("Tournament retrieved successfully", response.getMessage());
        assertNotNull(response.getTournament());
        assertEquals("Spring Championship", response.getTournament().getName());
    }

    @Test
    void testGetTournamentById_AsTournyAdmin_IsAdmin_Success() {
        tournament.getAdmins().add(new TournamentAdmin(tournament, tournamentAdminUser));
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(userRepository.findByUsername("tourny_admin")).thenReturn(Optional.of(tournamentAdminUser));

        TournamentResponse response = tournamentService.getTournamentById(1L, "tourny_admin");

        assertTrue(response.isSuccess());
        assertEquals("Tournament retrieved successfully", response.getMessage());
    }

    @Test
    void testGetTournamentById_AsTournyAdmin_NotAdmin_Throws403() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(userRepository.findByUsername("tourny_admin")).thenReturn(Optional.of(tournamentAdminUser));

        assertThrows(AccessDeniedException.class,
            () -> tournamentService.getTournamentById(1L, "tourny_admin"));
    }

    @Test
    void testGetTournamentById_NotFound() {
        when(tournamentRepository.findById(999L)).thenReturn(Optional.empty());

        TournamentResponse response = tournamentService.getTournamentById(999L, "admin");

        assertFalse(response.isSuccess());
        assertEquals("Tournament not found", response.getMessage());
    }

    // -------------------------------------------------------------------------
    // getUsersByRole
    // -------------------------------------------------------------------------

    @Test
    void testGetUsersByRole_Success() {
        List<User> users = new ArrayList<>();
        users.add(tournamentAdminUser);
        when(userRepository.findByRole(Role.TOURNY_ADMIN.name())).thenReturn(users);

        UserListResponse response = tournamentService.getUsersByRole(Role.TOURNY_ADMIN.name());

        assertTrue(response.isSuccess());
        assertEquals("Users retrieved successfully", response.getMessage());
        assertNotNull(response.getUsers());
        assertEquals(1, response.getUsers().size());
        assertEquals("tourny_admin", response.getUsers().get(0).getUsername());
    }

    // -------------------------------------------------------------------------
    // addTournamentPlayer — rankScore
    // -------------------------------------------------------------------------

    @Test
    void testAddTournamentPlayer_DefaultsRankScoreToZeroWhenNotProvided() {
        AddTournamentPlayerRequest request = new AddTournamentPlayerRequest(3L);
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(userRepository.findById(3L)).thenReturn(Optional.of(playerUser));
        when(tournamentRepository.save(any(Tournament.class))).thenAnswer(inv -> {
            Tournament saved = inv.getArgument(0);
            TournamentPlayer added = saved.getPlayers().get(saved.getPlayers().size() - 1);
            assertEquals(BigDecimal.ZERO, added.getRankScore());
            return saved;
        });

        TournamentResponse response = tournamentService.addTournamentPlayer(1L, request);

        assertTrue(response.isSuccess());
    }

    @Test
    void testAddTournamentPlayer_UsesProvidedRankScore() {
        BigDecimal score = new BigDecimal("15.75");
        AddTournamentPlayerRequest request = new AddTournamentPlayerRequest(3L, score);
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(userRepository.findById(3L)).thenReturn(Optional.of(playerUser));
        when(tournamentRepository.save(any(Tournament.class))).thenAnswer(inv -> {
            Tournament saved = inv.getArgument(0);
            TournamentPlayer added = saved.getPlayers().get(saved.getPlayers().size() - 1);
            assertEquals(score, added.getRankScore());
            return saved;
        });

        TournamentResponse response = tournamentService.addTournamentPlayer(1L, request);

        assertTrue(response.isSuccess());
    }

    // -------------------------------------------------------------------------
    // toDto — player sort order (rankScore desc, userId asc)
    // -------------------------------------------------------------------------

    @Test
    void testGetTournamentById_PlayersSortedByRankScoreDescThenUserIdAsc() {
        TournamentPlayer tp1 = new TournamentPlayer(tournament, playerUser, new BigDecimal("5.00"));
        TournamentPlayer tp2 = new TournamentPlayer(tournament, playerUser2, new BigDecimal("10.00"));
        tournament.getPlayers().add(tp1);
        tournament.getPlayers().add(tp2);

        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        TournamentResponse response = tournamentService.getTournamentById(1L, "admin");

        assertTrue(response.isSuccess());
        List<TournamentResponse.PlayerDto> players = response.getTournament().getPlayers();
        assertEquals(2, players.size());
        assertEquals(playerUser2.getId(), players.get(0).getId());
        assertEquals(playerUser.getId(), players.get(1).getId());
    }

    @Test
    void testGetTournamentById_PlayersTieOnRankScore_SortedByUserIdAsc() {
        TournamentPlayer tp1 = new TournamentPlayer(tournament, playerUser, new BigDecimal("7.50"));
        TournamentPlayer tp2 = new TournamentPlayer(tournament, playerUser2, new BigDecimal("7.50"));
        tournament.getPlayers().add(tp2);
        tournament.getPlayers().add(tp1);

        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        TournamentResponse response = tournamentService.getTournamentById(1L, "admin");

        assertTrue(response.isSuccess());
        List<TournamentResponse.PlayerDto> players = response.getTournament().getPlayers();
        assertEquals(2, players.size());
        assertEquals(playerUser.getId(), players.get(0).getId());
        assertEquals(playerUser2.getId(), players.get(1).getId());
    }

    @Test
    void testGetTournamentById_PlayerDtoContainsRankAndRankScore() {
        TournamentPlayer tp = new TournamentPlayer(tournament, playerUser, new BigDecimal("8.50"));
        tp.setRank(2);
        tournament.getPlayers().add(tp);

        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        TournamentResponse response = tournamentService.getTournamentById(1L, "admin");

        TournamentResponse.PlayerDto dto = response.getTournament().getPlayers().get(0);
        assertEquals(Integer.valueOf(2), dto.getRank());
        assertEquals(new BigDecimal("8.50"), dto.getRankScore());
    }

    @Test
    void testGetTournamentById_NewPlayerHasNullRankAndZeroRankScore() {
        TournamentPlayer tp = new TournamentPlayer(tournament, playerUser);
        tournament.getPlayers().add(tp);

        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        TournamentResponse response = tournamentService.getTournamentById(1L, "admin");

        TournamentResponse.PlayerDto dto = response.getTournament().getPlayers().get(0);
        assertNull(dto.getRank());
        assertEquals(BigDecimal.ZERO, dto.getRankScore());
    }
}
