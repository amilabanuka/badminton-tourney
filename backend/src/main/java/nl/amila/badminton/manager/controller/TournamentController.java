package nl.amila.badminton.manager.controller;

import nl.amila.badminton.manager.dto.*;
import nl.amila.badminton.manager.entity.Role;
import nl.amila.badminton.manager.service.TournamentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
     * Remove tournament player (ADMIN or TOURNY_ADMIN)
     */
    @DeleteMapping("/{id}/players/{userId}")
    public ResponseEntity<TournamentResponse> removeTournamentPlayer(
            @PathVariable Long id,
            @PathVariable Long userId) {
        TournamentResponse response = tournamentService.removeTournamentPlayer(id, userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Get all tournaments (authenticated users)
     */
    @GetMapping
    public ResponseEntity<TournamentResponse> getTournaments() {
        TournamentResponse response = tournamentService.getTournaments();
        return ResponseEntity.ok(response);
    }

    /**
     * Get tournament by ID (authenticated users)
     */
    @GetMapping("/{id}")
    public ResponseEntity<TournamentResponse> getTournamentById(@PathVariable Long id) {
        TournamentResponse response = tournamentService.getTournamentById(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}

