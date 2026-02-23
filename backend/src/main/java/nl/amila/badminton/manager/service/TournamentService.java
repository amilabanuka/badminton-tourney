package nl.amila.badminton.manager.service;

import nl.amila.badminton.manager.dto.*;
import nl.amila.badminton.manager.entity.Role;
import nl.amila.badminton.manager.entity.Tournament;
import nl.amila.badminton.manager.entity.TournamentAdmin;
import nl.amila.badminton.manager.entity.TournamentPlayer;
import nl.amila.badminton.manager.entity.User;
import nl.amila.badminton.manager.repository.TournamentRepository;
import nl.amila.badminton.manager.repository.UserRepository;
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

    public TournamentService(TournamentRepository tournamentRepository, UserRepository userRepository) {
        this.tournamentRepository = tournamentRepository;
        this.userRepository = userRepository;
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
        tournamentOpt.get().getPlayers().add(new TournamentPlayer(tournamentOpt.get(), userOpt.get()));
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
     * Remove a tournament player
     */
    @Transactional
    public TournamentResponse removeTournamentPlayer(Long tournamentId, Long userId) {
        // Validate tournament exists
        Optional<Tournament> tournamentOpt = tournamentRepository.findById(tournamentId);
        if (tournamentOpt.isEmpty()) {
            return new TournamentResponse(false, "Tournament not found");
        }

        // Remove player from tournament's players list
        boolean removed = tournamentOpt.get().getPlayers().removeIf(p -> p.getUser().getId().equals(userId));

        if (!removed) {
            return new TournamentResponse(false, "User is not a tournament player");
        }

        tournamentRepository.save(tournamentOpt.get());

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

                // Get admin and player IDs from tournament's collections
                List<Long> adminIds = t.getAdmins().stream()
                    .map(a -> a.getUser().getId())
                    .collect(Collectors.toList());
                List<Long> playerIds = t.getPlayers().stream()
                    .map(p -> p.getUser().getId())
                    .collect(Collectors.toList());

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

        // Get admin and player IDs from tournament's collections
        List<Long> adminIds = tournament.getAdmins().stream()
            .map(a -> a.getUser().getId())
            .collect(Collectors.toList());
        List<Long> playerIds = tournament.getPlayers().stream()
            .map(p -> p.getUser().getId())
            .collect(Collectors.toList());

        dto.setAdminIds(adminIds);
        dto.setPlayerIds(playerIds);

        return new TournamentResponse(true, "Tournament retrieved successfully", dto);
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
        TournamentResponse.TournamentDto dto = new TournamentResponse.TournamentDto(
            tournament.getId(),
            tournament.getName(),
            tournament.getOwnerId(),
            tournament.isEnabled(),
            tournament.getCreatedAt(),
            tournament.getUpdatedAt()
        );
        List<Long> adminIds = tournament.getAdmins().stream()
            .map(a -> a.getUser().getId())
            .collect(Collectors.toList());
        List<Long> playerIds = tournament.getPlayers().stream()
            .map(p -> p.getUser().getId())
            .collect(Collectors.toList());
        dto.setAdminIds(adminIds);
        dto.setPlayerIds(playerIds);

        return new TournamentResponse(true, "Tournament " + statusMsg + " successfully", dto);
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

