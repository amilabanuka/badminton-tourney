package nl.amila.badminton.manager.service;

import nl.amila.badminton.manager.dto.*;
import nl.amila.badminton.manager.entity.Role;
import nl.amila.badminton.manager.entity.Tournament;
import nl.amila.badminton.manager.entity.User;
import nl.amila.badminton.manager.repository.TournamentRepository;
import nl.amila.badminton.manager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

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
    private JdbcTemplate jdbcTemplate;

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

        tournament = new Tournament("Spring Championship", 2L, true);
        tournament.setId(1L);
    }

    @Test
    void testCreateTournament_Success() {
        // Arrange
        CreateTournamentRequest request = new CreateTournamentRequest("Spring Championship", 2L, true);
        when(tournamentRepository.existsByName("Spring Championship")).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(tournamentAdminUser));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        // Act
        TournamentResponse response = tournamentService.createTournament(request);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Tournament created successfully", response.getMessage());
        assertNotNull(response.getTournament());
        assertEquals("Spring Championship", response.getTournament().getName());
        verify(tournamentRepository, times(1)).save(any(Tournament.class));
    }

    @Test
    void testCreateTournament_NameAlreadyExists() {
        // Arrange
        CreateTournamentRequest request = new CreateTournamentRequest("Spring Championship", 2L, true);
        when(tournamentRepository.existsByName("Spring Championship")).thenReturn(true);

        // Act
        TournamentResponse response = tournamentService.createTournament(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Tournament name already exists", response.getMessage());
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    void testCreateTournament_OwnerNotFound() {
        // Arrange
        CreateTournamentRequest request = new CreateTournamentRequest("Spring Championship", 999L, true);
        when(tournamentRepository.existsByName("Spring Championship")).thenReturn(false);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        TournamentResponse response = tournamentService.createTournament(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Owner user not found", response.getMessage());
    }

    @Test
    void testCreateTournament_OwnerNotTournyAdmin() {
        // Arrange
        CreateTournamentRequest request = new CreateTournamentRequest("Spring Championship", 3L, true);
        when(tournamentRepository.existsByName("Spring Championship")).thenReturn(false);
        when(userRepository.findById(3L)).thenReturn(Optional.of(playerUser));

        // Act
        TournamentResponse response = tournamentService.createTournament(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Owner must have TOURNY_ADMIN role", response.getMessage());
    }

    @Test
    void testAddTournamentAdmin_Success() {
        // Arrange
        AddTournamentAdminRequest request = new AddTournamentAdminRequest(2L);
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(userRepository.findById(2L)).thenReturn(Optional.of(tournamentAdminUser));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(1L), eq(2L))).thenReturn(0);
        when(jdbcTemplate.update(anyString(), eq(1L), eq(2L))).thenReturn(1);

        // Act
        TournamentResponse response = tournamentService.addTournamentAdmin(1L, request);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Tournament admin added successfully", response.getMessage());
        verify(jdbcTemplate, times(1)).update(anyString(), eq(1L), eq(2L));
    }

    @Test
    void testAddTournamentAdmin_UserNotTournyAdmin() {
        // Arrange
        AddTournamentAdminRequest request = new AddTournamentAdminRequest(3L);
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(userRepository.findById(3L)).thenReturn(Optional.of(playerUser));

        // Act
        TournamentResponse response = tournamentService.addTournamentAdmin(1L, request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("User must have TOURNY_ADMIN role", response.getMessage());
    }

    @Test
    void testAddTournamentPlayer_Success() {
        // Arrange
        AddTournamentPlayerRequest request = new AddTournamentPlayerRequest(3L);
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(userRepository.findById(3L)).thenReturn(Optional.of(playerUser));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(1L), eq(3L))).thenReturn(0);
        when(jdbcTemplate.update(anyString(), eq(1L), eq(3L))).thenReturn(1);

        // Act
        TournamentResponse response = tournamentService.addTournamentPlayer(1L, request);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Tournament player added successfully", response.getMessage());
        verify(jdbcTemplate, times(1)).update(anyString(), eq(1L), eq(3L));
    }

    @Test
    void testAddTournamentPlayer_UserNotPlayer() {
        // Arrange
        AddTournamentPlayerRequest request = new AddTournamentPlayerRequest(2L);
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(userRepository.findById(2L)).thenReturn(Optional.of(tournamentAdminUser));

        // Act
        TournamentResponse response = tournamentService.addTournamentPlayer(1L, request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("User must have PLAYER role", response.getMessage());
    }

    @Test
    void testRemoveTournamentAdmin_Success() {
        // Arrange
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(jdbcTemplate.update(anyString(), eq(1L), eq(2L))).thenReturn(1);

        // Act
        TournamentResponse response = tournamentService.removeTournamentAdmin(1L, 2L);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Tournament admin removed successfully", response.getMessage());
    }

    @Test
    void testRemoveTournamentPlayer_Success() {
        // Arrange
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(jdbcTemplate.update(anyString(), eq(1L), eq(3L))).thenReturn(1);

        // Act
        TournamentResponse response = tournamentService.removeTournamentPlayer(1L, 3L);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Tournament player removed successfully", response.getMessage());
    }

    @Test
    void testGetTournaments_Success() {
        // Arrange
        List<Tournament> tournaments = new ArrayList<>();
        tournaments.add(tournament);
        when(tournamentRepository.findAll()).thenReturn(tournaments);
        when(jdbcTemplate.queryForList(anyString(), eq(Long.class), anyLong())).thenReturn(new ArrayList<>());

        // Act
        TournamentResponse response = tournamentService.getTournaments();

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Tournaments retrieved successfully", response.getMessage());
        assertNotNull(response.getTournaments());
        assertEquals(1, response.getTournaments().size());
    }

    @Test
    void testGetTournamentById_Success() {
        // Arrange
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(jdbcTemplate.queryForList(anyString(), eq(Long.class), anyLong())).thenReturn(new ArrayList<>());

        // Act
        TournamentResponse response = tournamentService.getTournamentById(1L);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Tournament retrieved successfully", response.getMessage());
        assertNotNull(response.getTournament());
        assertEquals("Spring Championship", response.getTournament().getName());
    }

    @Test
    void testGetTournamentById_NotFound() {
        // Arrange
        when(tournamentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        TournamentResponse response = tournamentService.getTournamentById(999L);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Tournament not found", response.getMessage());
    }

    @Test
    void testGetUsersByRole_Success() {
        // Arrange
        List<User> users = new ArrayList<>();
        users.add(tournamentAdminUser);
        when(userRepository.findByRole(Role.TOURNY_ADMIN.name())).thenReturn(users);

        // Act
        UserListResponse response = tournamentService.getUsersByRole(Role.TOURNY_ADMIN.name());

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Users retrieved successfully", response.getMessage());
        assertNotNull(response.getUsers());
        assertEquals(1, response.getUsers().size());
        assertEquals("tourny_admin", response.getUsers().get(0).getUsername());
    }
}

