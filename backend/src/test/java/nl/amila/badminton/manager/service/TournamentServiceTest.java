package nl.amila.badminton.manager.service;

import nl.amila.badminton.manager.dto.*;
import nl.amila.badminton.manager.entity.Role;
import nl.amila.badminton.manager.entity.Tournament;
import nl.amila.badminton.manager.entity.TournamentAdmin;
import nl.amila.badminton.manager.entity.TournamentPlayer;
import nl.amila.badminton.manager.entity.TournamentType;
import nl.amila.badminton.manager.entity.User;
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

    @InjectMocks
    private TournamentService tournamentService;

    private User adminUser;
    private User tournamentAdminUser;
    private User playerUser;
    private Tournament tournament;

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

        tournament = new Tournament("Spring Championship", 2L, true, TournamentType.ONE_OFF);
        tournament.setId(1L);
    }

    // -------------------------------------------------------------------------
    // createTournament
    // -------------------------------------------------------------------------

    @Test
    void testCreateTournament_Success() {
        CreateTournamentRequest request = new CreateTournamentRequest("Spring Championship", 2L, true, TournamentType.ONE_OFF);
        when(tournamentRepository.existsByName("Spring Championship")).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(tournamentAdminUser));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        TournamentResponse response = tournamentService.createTournament(request);

        assertTrue(response.isSuccess());
        assertEquals("Tournament created successfully", response.getMessage());
        assertNotNull(response.getTournament());
        assertEquals("Spring Championship", response.getTournament().getName());
        verify(tournamentRepository, times(1)).save(any(Tournament.class));
    }

    @Test
    void testCreateTournament_NameAlreadyExists() {
        CreateTournamentRequest request = new CreateTournamentRequest("Spring Championship", 2L, true, TournamentType.ONE_OFF);
        when(tournamentRepository.existsByName("Spring Championship")).thenReturn(true);

        TournamentResponse response = tournamentService.createTournament(request);

        assertFalse(response.isSuccess());
        assertEquals("Tournament name already exists", response.getMessage());
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    void testCreateTournament_OwnerNotFound() {
        CreateTournamentRequest request = new CreateTournamentRequest("Spring Championship", 999L, true, TournamentType.ONE_OFF);
        when(tournamentRepository.existsByName("Spring Championship")).thenReturn(false);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        TournamentResponse response = tournamentService.createTournament(request);

        assertFalse(response.isSuccess());
        assertEquals("Owner user not found", response.getMessage());
    }

    @Test
    void testCreateTournament_OwnerNotTournyAdmin() {
        CreateTournamentRequest request = new CreateTournamentRequest("Spring Championship", 3L, true, TournamentType.ONE_OFF);
        when(tournamentRepository.existsByName("Spring Championship")).thenReturn(false);
        when(userRepository.findById(3L)).thenReturn(Optional.of(playerUser));

        TournamentResponse response = tournamentService.createTournament(request);

        assertFalse(response.isSuccess());
        assertEquals("Owner must have TOURNY_ADMIN role", response.getMessage());
    }

    // -------------------------------------------------------------------------
    // addTournamentAdmin
    // -------------------------------------------------------------------------

    @Test
    void testAddTournamentAdmin_Success() {
        // Tournament has no admins yet so the duplicate check passes
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
        // Pre-seed the tournament's admins list
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
        // Pre-seed the admins list so removeIf finds the entry
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
        // Empty admins list — user is not an admin
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
        // Player removal is intentionally blocked regardless of whether the player exists
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
        // ADMIN path: findAll called, findByAdminsUserId never called
        verify(tournamentRepository, times(1)).findAll();
        verify(tournamentRepository, never()).findByAdminsUserId(anyLong());
    }

    @Test
    void testGetTournaments_AsTournyAdmin_ReturnsOnlyOwn() {
        // Seed the tournament so tourny_admin is an admin of it
        tournament.getAdmins().add(new TournamentAdmin(tournament, tournamentAdminUser));
        when(userRepository.findByUsername("tourny_admin")).thenReturn(Optional.of(tournamentAdminUser));
        when(tournamentRepository.findByAdminsUserId(2L)).thenReturn(List.of(tournament));

        TournamentResponse response = tournamentService.getTournaments("tourny_admin");

        assertTrue(response.isSuccess());
        assertEquals(1, response.getTournaments().size());
        assertEquals("Spring Championship", response.getTournaments().get(0).getName());
        // TOURNY_ADMIN path: findByAdminsUserId called, findAll never called
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
        // tourny_admin is in the admins list → access granted
        tournament.getAdmins().add(new TournamentAdmin(tournament, tournamentAdminUser));
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(userRepository.findByUsername("tourny_admin")).thenReturn(Optional.of(tournamentAdminUser));

        TournamentResponse response = tournamentService.getTournamentById(1L, "tourny_admin");

        assertTrue(response.isSuccess());
        assertEquals("Tournament retrieved successfully", response.getMessage());
    }

    @Test
    void testGetTournamentById_AsTournyAdmin_NotAdmin_Throws403() {
        // tourny_admin is NOT in the admins list → AccessDeniedException
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
}

