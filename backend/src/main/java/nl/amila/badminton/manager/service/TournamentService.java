package nl.amila.badminton.manager.service;

import nl.amila.badminton.manager.dto.*;
import nl.amila.badminton.manager.entity.PlayerStatus;
import nl.amila.badminton.manager.entity.Role;
import nl.amila.badminton.manager.entity.Tournament;
import nl.amila.badminton.manager.entity.TournamentAdmin;
import nl.amila.badminton.manager.entity.TournamentPlayer;
import nl.amila.badminton.manager.entity.TournamentType;
import nl.amila.badminton.manager.entity.User;
import nl.amila.badminton.manager.repository.TournamentPlayerRepository;
import nl.amila.badminton.manager.repository.TournamentRepository;
import nl.amila.badminton.manager.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TournamentService {
    private final TournamentRepository tournamentRepository;
    private final UserRepository userRepository;
    private final TournamentPlayerRepository tournamentPlayerRepository;

    public TournamentService(TournamentRepository tournamentRepository, UserRepository userRepository,
                             TournamentPlayerRepository tournamentPlayerRepository) {
        this.tournamentRepository = tournamentRepository;
        this.userRepository = userRepository;
        this.tournamentPlayerRepository = tournamentPlayerRepository;
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
        if (request.getType() == null) {
            return new TournamentResponse(false, "Tournament type is required");
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
            request.isEnabled(),
            request.getType()
        );

        // Add owner as a tournament admin to the admins list
        tournament.getAdmins().add(new TournamentAdmin(tournament, owner));

        Tournament savedTournament = tournamentRepository.save(tournament);

        // Return success response
        // Return success response
        TournamentResponse.TournamentDto tournamentDto = new TournamentResponse.TournamentDto(
            savedTournament.getId(),
            savedTournament.getName(),
            savedTournament.getOwnerId(),
            savedTournament.isEnabled(),
            savedTournament.getCreatedAt(),
            savedTournament.getUpdatedAt(),
            savedTournament.getType()
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
        boolean alreadyAdmin = tournamentOpt.get().getAdmins().stream()
            .anyMatch(a -> a.getUser().getId().equals(request.getUserId()));

        if (alreadyAdmin) {
            return new TournamentResponse(false, "User is already a tournament admin");
        }

        // Add admin to tournament's admins list
        tournamentOpt.get().getAdmins().add(new TournamentAdmin(tournamentOpt.get(), userOpt.get()));
        tournamentRepository.save(tournamentOpt.get());

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
        boolean alreadyPlayer = tournamentOpt.get().getPlayers().stream()
            .anyMatch(p -> p.getUser().getId().equals(request.getUserId()));

        if (alreadyPlayer) {
            return new TournamentResponse(false, "User is already a tournament player");
        }

        // Add player to tournament's players list
        BigDecimal rankScore = request.getRankScore() != null ? request.getRankScore() : BigDecimal.ZERO;
        tournamentOpt.get().getPlayers().add(new TournamentPlayer(tournamentOpt.get(), userOpt.get(), rankScore));
        tournamentRepository.save(tournamentOpt.get());

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

        // Remove admin from tournament's admins list
        boolean removed = tournamentOpt.get().getAdmins().removeIf(a -> a.getUser().getId().equals(userId));

        if (!removed) {
            return new TournamentResponse(false, "User is not a tournament admin");
        }

        tournamentRepository.save(tournamentOpt.get());

        return new TournamentResponse(true, "Tournament admin removed successfully");
    }

    /**
     * Remove a tournament player — not permitted; players can only be disabled
     */
    @Transactional
    public TournamentResponse removeTournamentPlayer(Long tournamentId, Long userId) {
        return new TournamentResponse(false, "Players cannot be removed from a tournament. Use disable instead.");
    }

    /**
     * Get players with PLAYER role not yet in the tournament
     */
    public UserListResponse getAvailablePlayersForTournament(Long tournamentId) {
        Optional<Tournament> tournamentOpt = tournamentRepository.findById(tournamentId);
        if (tournamentOpt.isEmpty()) {
            return new UserListResponse(false, "Tournament not found");
        }

        // Collect IDs of players already in the tournament
        List<Long> existingPlayerIds = tournamentOpt.get().getPlayers().stream()
            .map(p -> p.getUser().getId())
            .collect(Collectors.toList());

        List<User> availablePlayers = userRepository.findByRole(Role.PLAYER.name()).stream()
            .filter(u -> !existingPlayerIds.contains(u.getId()))
            .collect(Collectors.toList());

        List<UserListResponse.UserListDto> userDtos = availablePlayers.stream()
            .map(u -> new UserListResponse.UserListDto(
                u.getId(), u.getUsername(), u.getEmail(), u.getFirstName(), u.getLastName()
            ))
            .collect(Collectors.toList());

        return new UserListResponse(true, "Available players retrieved successfully", userDtos);
    }

    /**
     * Enable a player — only valid when status is DISABLED
     */
    @Transactional
    public TournamentResponse enablePlayer(Long tournamentId, Long userId) {
        Optional<TournamentPlayer> playerOpt = tournamentPlayerRepository.findByTournamentIdAndUserId(tournamentId, userId);
        if (playerOpt.isEmpty()) {
            return new TournamentResponse(false, "Player is not registered in this tournament");
        }

        TournamentPlayer player = playerOpt.get();
        if (player.getStatus() != PlayerStatus.DISABLED) {
            return new TournamentResponse(false, "Player can only be enabled when currently DISABLED");
        }

        player.setStatus(PlayerStatus.ENABLED);
        tournamentPlayerRepository.save(player);

        return new TournamentResponse(true, "Player enabled successfully");
    }

    /**
     * Disable a player — valid when status is ENABLED or ACTIVE
     */
    @Transactional
    public TournamentResponse disablePlayer(Long tournamentId, Long userId) {
        Optional<TournamentPlayer> playerOpt = tournamentPlayerRepository.findByTournamentIdAndUserId(tournamentId, userId);
        if (playerOpt.isEmpty()) {
            return new TournamentResponse(false, "Player is not registered in this tournament");
        }

        TournamentPlayer player = playerOpt.get();
        if (player.getStatus() == PlayerStatus.DISABLED) {
            return new TournamentResponse(false, "Player is already DISABLED");
        }

        player.setStatus(PlayerStatus.DISABLED);
        tournamentPlayerRepository.save(player);

        return new TournamentResponse(true, "Player disabled successfully");
    }

    /**
     * Get tournaments - ADMIN sees all, TOURNY_ADMIN sees only tournaments they are an admin of
     */
    public TournamentResponse getTournaments(String username) {
        User caller = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        List<Tournament> tournaments;
        if (Role.TOURNY_ADMIN.name().equals(caller.getRole())) {
            tournaments = tournamentRepository.findByAdminsUserId(caller.getId());
        } else {
            tournaments = new ArrayList<>();
            tournamentRepository.findAll().forEach(tournaments::add);
        }

        List<TournamentResponse.TournamentDto> tournamentDtos = tournaments.stream()
            .map(this::toDto)
            .collect(Collectors.toList());

        return new TournamentResponse(true, "Tournaments retrieved successfully", tournamentDtos);
    }

    /**
     * Get tournament by ID - TOURNY_ADMIN must be an admin of that tournament, else 403
     */
    public TournamentResponse getTournamentById(Long id, String username) {
        Optional<Tournament> tournamentOpt = tournamentRepository.findById(id);
        if (tournamentOpt.isEmpty()) {
            return new TournamentResponse(false, "Tournament not found");
        }

        User caller = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        Tournament tournament = tournamentOpt.get();

        if (Role.TOURNY_ADMIN.name().equals(caller.getRole())) {
            boolean isAdmin = tournament.getAdmins().stream()
                .anyMatch(a -> a.getUser().getId().equals(caller.getId()));
            if (!isAdmin) {
                throw new AccessDeniedException("You do not have access to this tournament");
            }
        }

        return new TournamentResponse(true, "Tournament retrieved successfully", toDto(tournament));
    }

    /**
     * Toggle tournament enabled/disabled status
     */
    @Transactional
    public TournamentResponse toggleTournament(Long id) {
        Optional<Tournament> tournamentOpt = tournamentRepository.findById(id);
        if (tournamentOpt.isEmpty()) {
            return new TournamentResponse(false, "Tournament not found");
        }

        Tournament tournament = tournamentOpt.get();
        tournament.setEnabled(!tournament.isEnabled());
        tournamentRepository.save(tournament);

        String statusMsg = tournament.isEnabled() ? "enabled" : "disabled";
        return new TournamentResponse(true, "Tournament " + statusMsg + " successfully", toDto(tournament));
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

    /**
     * Maps a Tournament entity to a TournamentDto, including full admin and player details
     */
    private TournamentResponse.TournamentDto toDto(Tournament t) {
        TournamentResponse.TournamentDto dto = new TournamentResponse.TournamentDto(
            t.getId(), t.getName(), t.getOwnerId(), t.isEnabled(), t.getCreatedAt(), t.getUpdatedAt(), t.getType()
        );
        dto.setAdminIds(t.getAdmins().stream()
            .map(a -> a.getUser().getId())
            .collect(Collectors.toList()));
        dto.setPlayerIds(t.getPlayers().stream()
            .map(p -> p.getUser().getId())
            .collect(Collectors.toList()));
        dto.setAdmins(t.getAdmins().stream()
            .map(a -> new TournamentResponse.AdminDto(
                a.getUser().getId(),
                a.getUser().getFirstName(),
                a.getUser().getLastName(),
                a.getUser().getEmail()
            ))
            .collect(Collectors.toList()));
        dto.setPlayers(t.getPlayers().stream()
            .sorted(Comparator.comparing(TournamentPlayer::getRankScore,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(p -> p.getUser().getId()))
            .map(p -> new TournamentResponse.PlayerDto(
                p.getUser().getId(),
                p.getId(),
                p.getUser().getFirstName(),
                p.getUser().getLastName(),
                p.getUser().getEmail(),
                p.getStatus().name(),
                p.getStatusChangedAt(),
                p.getRank(),
                p.getRankScore()
            ))
            .collect(Collectors.toList()));
        return dto;
    }
}

