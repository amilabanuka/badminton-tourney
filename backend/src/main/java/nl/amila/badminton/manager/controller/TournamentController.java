package nl.amila.badminton.manager.controller;

import nl.amila.badminton.manager.dto.*;
import nl.amila.badminton.manager.entity.Role;
import nl.amila.badminton.manager.service.TournamentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tournaments")
@CrossOrigin(origins = "*")
public class TournamentController {
    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    /**
     * Create a new tournament (ADMIN only)
     */
    @PostMapping
    public ResponseEntity<TournamentResponse> createTournament(@RequestBody CreateTournamentRequest request) {
        TournamentResponse response = tournamentService.createTournament(request);
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Get list of available tournament admins (ADMIN only)
     */
    @GetMapping("/admins/available")
    public ResponseEntity<UserListResponse> getAvailableAdmins() {
        UserListResponse response = tournamentService.getUsersByRole(Role.TOURNY_ADMIN.name());
        return ResponseEntity.ok(response);
    }

    /**
     * Add tournament admin (ADMIN only)
     */
    @PostMapping("/{id}/admins")
    public ResponseEntity<TournamentResponse> addTournamentAdmin(
            @PathVariable Long id,
            @RequestBody AddTournamentAdminRequest request) {
        TournamentResponse response = tournamentService.addTournamentAdmin(id, request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Add tournament player (ADMIN or TOURNY_ADMIN)
     */
    @PostMapping("/{id}/players")
    public ResponseEntity<TournamentResponse> addTournamentPlayer(
            @PathVariable Long id,
            @RequestBody AddTournamentPlayerRequest request) {
        TournamentResponse response = tournamentService.addTournamentPlayer(id, request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Remove tournament admin (ADMIN only)
     */
    @DeleteMapping("/{id}/admins/{userId}")
    public ResponseEntity<TournamentResponse> removeTournamentAdmin(
            @PathVariable Long id,
            @PathVariable Long userId) {
        TournamentResponse response = tournamentService.removeTournamentAdmin(id, userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Remove tournament player (ADMIN or TOURNY_ADMIN) — returns 400, players cannot be removed
     */
    @DeleteMapping("/{id}/players/{userId}")
    public ResponseEntity<TournamentResponse> removeTournamentPlayer(
            @PathVariable Long id,
            @PathVariable Long userId) {
        TournamentResponse response = tournamentService.removeTournamentPlayer(id, userId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Get available players (with PLAYER role not yet in the tournament) - ADMIN or TOURNY_ADMIN
     */
    @GetMapping("/{id}/players/available")
    public ResponseEntity<UserListResponse> getAvailablePlayers(@PathVariable Long id) {
        UserListResponse response = tournamentService.getAvailablePlayersForTournament(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Enable a player in a tournament (ADMIN or TOURNY_ADMIN)
     */
    @PostMapping("/{id}/players/{userId}/enable")
    public ResponseEntity<TournamentResponse> enablePlayer(
            @PathVariable Long id,
            @PathVariable Long userId) {
        TournamentResponse response = tournamentService.enablePlayer(id, userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Disable a player in a tournament (ADMIN or TOURNY_ADMIN)
     */
    @PostMapping("/{id}/players/{userId}/disable")
    public ResponseEntity<TournamentResponse> disablePlayer(
            @PathVariable Long id,
            @PathVariable Long userId) {
        TournamentResponse response = tournamentService.disablePlayer(id, userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Toggle tournament enabled/disabled (ADMIN only)
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<TournamentResponse> toggleTournament(@PathVariable Long id) {
        TournamentResponse response = tournamentService.toggleTournament(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Get all tournaments (ADMIN sees all, TOURNY_ADMIN sees only their own)
     */
    @GetMapping
    public ResponseEntity<TournamentResponse> getTournaments(Authentication authentication) {
        TournamentResponse response = tournamentService.getTournaments(authentication.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * Get tournament by ID - TOURNY_ADMIN receives 403 if not an admin of this tournament
     */
    @GetMapping("/{id}")
    public ResponseEntity<TournamentResponse> getTournamentById(@PathVariable Long id, Authentication authentication) {
        try {
            TournamentResponse response = tournamentService.getTournamentById(id, authentication.getName());
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new TournamentResponse(false, e.getMessage()));
        }
    }
}

