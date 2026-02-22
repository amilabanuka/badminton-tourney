package nl.amila.badminton.manager.service;

import nl.amila.badminton.manager.dto.*;
import nl.amila.badminton.manager.entity.Role;
import nl.amila.badminton.manager.entity.Tournament;
import nl.amila.badminton.manager.entity.User;
import nl.amila.badminton.manager.repository.TournamentRepository;
import nl.amila.badminton.manager.repository.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TournamentService {
    private final TournamentRepository tournamentRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    public TournamentService(TournamentRepository tournamentRepository, UserRepository userRepository, JdbcTemplate jdbcTemplate) {
        this.tournamentRepository = tournamentRepository;
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Create a new tournament
     */
    @Transactional
    public TournamentResponse createTournament(CreateTournamentRequest request) {
        // Validate input
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return new TournamentResponse(false, "Tournament name is required");
        }
        if (request.getOwnerId() == null) {
            return new TournamentResponse(false, "Owner ID is required");
        }

        // Check if tournament name already exists
        if (tournamentRepository.existsByName(request.getName())) {
            return new TournamentResponse(false, "Tournament name already exists");
        }

        // Validate owner exists and has TOURNY_ADMIN role
        Optional<User> ownerOpt = userRepository.findById(request.getOwnerId());
        if (ownerOpt.isEmpty()) {
            return new TournamentResponse(false, "Owner user not found");
        }

        User owner = ownerOpt.get();
        if (!Role.TOURNY_ADMIN.name().equals(owner.getRole())) {
            return new TournamentResponse(false, "Owner must have TOURNY_ADMIN role");
        }

        // Create tournament
        Tournament tournament = new Tournament(
            request.getName(),
            request.getOwnerId(),
            request.isEnabled()
        );

        Tournament savedTournament = tournamentRepository.save(tournament);

        // Return success response
        TournamentResponse.TournamentDto tournamentDto = new TournamentResponse.TournamentDto(
            savedTournament.getId(),
            savedTournament.getName(),
            savedTournament.getOwnerId(),
            savedTournament.isEnabled(),
            savedTournament.getCreatedAt(),
            savedTournament.getUpdatedAt()
        );

        return new TournamentResponse(true, "Tournament created successfully", tournamentDto);
    }

    /**
     * Add a tournament admin
     */
    @Transactional
    public TournamentResponse addTournamentAdmin(Long tournamentId, AddTournamentAdminRequest request) {
        // Validate tournament exists
        Optional<Tournament> tournamentOpt = tournamentRepository.findById(tournamentId);
        if (tournamentOpt.isEmpty()) {
            return new TournamentResponse(false, "Tournament not found");
        }

        // Validate user exists and has TOURNY_ADMIN role
        Optional<User> userOpt = userRepository.findById(request.getUserId());
        if (userOpt.isEmpty()) {
            return new TournamentResponse(false, "User not found");
        }

        User user = userOpt.get();
        if (!Role.TOURNY_ADMIN.name().equals(user.getRole())) {
            return new TournamentResponse(false, "User must have TOURNY_ADMIN role");
        }

        // Check if user is already an admin
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM tournament_admins WHERE tournament_id = ? AND user_id = ?",
            Integer.class,
            tournamentId,
            request.getUserId()
        );

        if (count != null && count > 0) {
            return new TournamentResponse(false, "User is already a tournament admin");
        }

        // Add admin
        jdbcTemplate.update(
            "INSERT INTO tournament_admins (tournament_id, user_id) VALUES (?, ?)",
            tournamentId,
            request.getUserId()
        );

        return new TournamentResponse(true, "Tournament admin added successfully");
    }

    /**
     * Add a tournament player
     */
    @Transactional
    public TournamentResponse addTournamentPlayer(Long tournamentId, AddTournamentPlayerRequest request) {
        // Validate tournament exists
        Optional<Tournament> tournamentOpt = tournamentRepository.findById(tournamentId);
        if (tournamentOpt.isEmpty()) {
            return new TournamentResponse(false, "Tournament not found");
        }

        // Validate user exists and has PLAYER role
        Optional<User> userOpt = userRepository.findById(request.getUserId());
        if (userOpt.isEmpty()) {
            return new TournamentResponse(false, "User not found");
        }

        User user = userOpt.get();
        if (!Role.PLAYER.name().equals(user.getRole())) {
            return new TournamentResponse(false, "User must have PLAYER role");
        }

        // Check if user is already a player
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM tournament_players WHERE tournament_id = ? AND user_id = ?",
            Integer.class,
            tournamentId,
            request.getUserId()
        );

        if (count != null && count > 0) {
            return new TournamentResponse(false, "User is already a tournament player");
        }

        // Add player
        jdbcTemplate.update(
            "INSERT INTO tournament_players (tournament_id, user_id) VALUES (?, ?)",
            tournamentId,
            request.getUserId()
        );

        return new TournamentResponse(true, "Tournament player added successfully");
    }

    /**
     * Remove a tournament admin
     */
    @Transactional
    public TournamentResponse removeTournamentAdmin(Long tournamentId, Long userId) {
        // Validate tournament exists
        Optional<Tournament> tournamentOpt = tournamentRepository.findById(tournamentId);
        if (tournamentOpt.isEmpty()) {
            return new TournamentResponse(false, "Tournament not found");
        }

        // Remove admin
        int rowsAffected = jdbcTemplate.update(
            "DELETE FROM tournament_admins WHERE tournament_id = ? AND user_id = ?",
            tournamentId,
            userId
        );

        if (rowsAffected == 0) {
            return new TournamentResponse(false, "User is not a tournament admin");
        }

        return new TournamentResponse(true, "Tournament admin removed successfully");
    }

    /**
     * Remove a tournament player
     */
    @Transactional
    public TournamentResponse removeTournamentPlayer(Long tournamentId, Long userId) {
        // Validate tournament exists
        Optional<Tournament> tournamentOpt = tournamentRepository.findById(tournamentId);
        if (tournamentOpt.isEmpty()) {
            return new TournamentResponse(false, "Tournament not found");
        }

        // Remove player
        int rowsAffected = jdbcTemplate.update(
            "DELETE FROM tournament_players WHERE tournament_id = ? AND user_id = ?",
            tournamentId,
            userId
        );

        if (rowsAffected == 0) {
            return new TournamentResponse(false, "User is not a tournament player");
        }

        return new TournamentResponse(true, "Tournament player removed successfully");
    }

    /**
     * Get all tournaments
     */
    public TournamentResponse getTournaments() {
        List<Tournament> tournaments = new ArrayList<>();
        tournamentRepository.findAll().forEach(tournaments::add);

        List<TournamentResponse.TournamentDto> tournamentDtos = tournaments.stream()
            .map(t -> {
                TournamentResponse.TournamentDto dto = new TournamentResponse.TournamentDto(
                    t.getId(),
                    t.getName(),
                    t.getOwnerId(),
                    t.isEnabled(),
                    t.getCreatedAt(),
                    t.getUpdatedAt()
                );

                // Get admin and player IDs
                List<Long> adminIds = jdbcTemplate.queryForList(
                    "SELECT user_id FROM tournament_admins WHERE tournament_id = ?",
                    Long.class,
                    t.getId()
                );
                List<Long> playerIds = jdbcTemplate.queryForList(
                    "SELECT user_id FROM tournament_players WHERE tournament_id = ?",
                    Long.class,
                    t.getId()
                );

                dto.setAdminIds(adminIds);
                dto.setPlayerIds(playerIds);

                return dto;
            })
            .collect(Collectors.toList());

        return new TournamentResponse(true, "Tournaments retrieved successfully", tournamentDtos);
    }

    /**
     * Get tournament by ID
     */
    public TournamentResponse getTournamentById(Long id) {
        Optional<Tournament> tournamentOpt = tournamentRepository.findById(id);
        if (tournamentOpt.isEmpty()) {
            return new TournamentResponse(false, "Tournament not found");
        }

        Tournament tournament = tournamentOpt.get();
        TournamentResponse.TournamentDto dto = new TournamentResponse.TournamentDto(
            tournament.getId(),
            tournament.getName(),
            tournament.getOwnerId(),
            tournament.isEnabled(),
            tournament.getCreatedAt(),
            tournament.getUpdatedAt()
        );

        // Get admin and player IDs
        List<Long> adminIds = jdbcTemplate.queryForList(
            "SELECT user_id FROM tournament_admins WHERE tournament_id = ?",
            Long.class,
            tournament.getId()
        );
        List<Long> playerIds = jdbcTemplate.queryForList(
            "SELECT user_id FROM tournament_players WHERE tournament_id = ?",
            Long.class,
            tournament.getId()
        );

        dto.setAdminIds(adminIds);
        dto.setPlayerIds(playerIds);

        return new TournamentResponse(true, "Tournament retrieved successfully", dto);
    }

    /**
     * Get users by role
     */
    public UserListResponse getUsersByRole(String role) {
        List<User> users = userRepository.findByRole(role);

        List<UserListResponse.UserListDto> userDtos = users.stream()
            .map(u -> new UserListResponse.UserListDto(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getFirstName(),
                u.getLastName()
            ))
            .collect(Collectors.toList());

        return new UserListResponse(true, "Users retrieved successfully", userDtos);
    }
}

