package nl.amila.badminton.manager.controller;

import nl.amila.badminton.manager.dto.CreateGameDayRequest;
import nl.amila.badminton.manager.dto.GameDayResponse;
import nl.amila.badminton.manager.dto.SubmitMatchScoreRequest;
import nl.amila.badminton.manager.service.LeagueGameDayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tournaments/{tournamentId}/game-days")
@CrossOrigin(origins = "*")
public class LeagueGameDayController {

    private final LeagueGameDayService leagueGameDayService;

    public LeagueGameDayController(LeagueGameDayService leagueGameDayService) {
        this.leagueGameDayService = leagueGameDayService;
    }

    /**
     * Create a new league game day (ADMIN or TOURNY_ADMIN of the tournament)
     */
    @PostMapping
    public ResponseEntity<GameDayResponse> createGameDay(
            @PathVariable Long tournamentId,
            @RequestBody CreateGameDayRequest request,
            Authentication authentication) {
        GameDayResponse response = leagueGameDayService.createGameDay(tournamentId, request, authentication.getName());
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Get all game days for a tournament
     */
    @GetMapping
    public ResponseEntity<GameDayResponse> getGameDays(
            @PathVariable Long tournamentId,
            Authentication authentication) {
        GameDayResponse response = leagueGameDayService.getGameDays(tournamentId, authentication.getName());
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
    }

    /**
     * Get a specific game day by id
     */
    @GetMapping("/{dayId}")
    public ResponseEntity<GameDayResponse> getGameDay(
            @PathVariable Long tournamentId,
            @PathVariable Long dayId,
            Authentication authentication) {
        GameDayResponse response = leagueGameDayService.getGameDay(tournamentId, dayId, authentication.getName());
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Start a game day (PENDING → ONGOING)
     */
    @PostMapping("/{dayId}/start")
    public ResponseEntity<GameDayResponse> startGameDay(
            @PathVariable Long tournamentId,
            @PathVariable Long dayId,
            Authentication authentication) {
        GameDayResponse response = leagueGameDayService.startGameDay(tournamentId, dayId, authentication.getName());
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Discard a game day (PENDING only — deletes it)
     */
    @DeleteMapping("/{dayId}")
    public ResponseEntity<GameDayResponse> discardGameDay(
            @PathVariable Long tournamentId,
            @PathVariable Long dayId,
            Authentication authentication) {
        GameDayResponse response = leagueGameDayService.discardGameDay(tournamentId, dayId, authentication.getName());
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Cancel a game day (PENDING or ONGOING — deletes it and all associated data)
     */
    @DeleteMapping("/{dayId}/cancel")
    public ResponseEntity<GameDayResponse> cancelGameDay(
            @PathVariable Long tournamentId,
            @PathVariable Long dayId,
            Authentication authentication) {
        GameDayResponse response = leagueGameDayService.cancelGameDay(tournamentId, dayId, authentication.getName());
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Submit or update the score for a match (ONGOING game days only)
     */
    @PutMapping("/{dayId}/groups/{groupId}/matches/{matchId}/score")
    public ResponseEntity<GameDayResponse> submitMatchScore(
            @PathVariable Long tournamentId,
            @PathVariable Long dayId,
            @PathVariable Long groupId,
            @PathVariable Long matchId,
            @RequestBody SubmitMatchScoreRequest request,
            Authentication authentication) {
        GameDayResponse response = leagueGameDayService.submitMatchScore(
                tournamentId, dayId, groupId, matchId, request, authentication.getName());
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Finish a game day (ONGOING → COMPLETED): validates all scores, runs Modified-ELO calculation,
     * saves rank score history, and updates player rank scores.
     */
    @PostMapping("/{dayId}/finish")
    public ResponseEntity<GameDayResponse> finishGameDay(
            @PathVariable Long tournamentId,
            @PathVariable Long dayId,
            Authentication authentication) {
        GameDayResponse response = leagueGameDayService.finishGameDay(tournamentId, dayId, authentication.getName());
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ── Player-scoped endpoints ───────────────────────────────────────────────

    /**
     * Get all game days for a tournament (PLAYER role — filtered to player's own registration).
     */
    @GetMapping("/player-list")
    public ResponseEntity<GameDayResponse> getGameDaysForPlayer(
            @PathVariable Long tournamentId,
            Authentication authentication) {
        GameDayResponse response = leagueGameDayService.getGameDaysForPlayer(tournamentId, authentication.getName());
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
    }

    /**
     * Get a specific game day for the authenticated player — returns only the player's group.
     */
    @GetMapping("/{dayId}/player-view")
    public ResponseEntity<GameDayResponse> getGameDayForPlayer(
            @PathVariable Long tournamentId,
            @PathVariable Long dayId,
            Authentication authentication) {
        GameDayResponse response = leagueGameDayService.getGameDayForPlayer(tournamentId, dayId, authentication.getName());
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Submit match score as a player. Score can only be set once — first submission wins.
     * Returns HTTP 409 if score was already submitted (concurrent duplicate).
     */
    @PutMapping("/{dayId}/groups/{groupId}/matches/{matchId}/player-score")
    public ResponseEntity<GameDayResponse> submitMatchScoreAsPlayer(
            @PathVariable Long tournamentId,
            @PathVariable Long dayId,
            @PathVariable Long groupId,
            @PathVariable Long matchId,
            @RequestBody SubmitMatchScoreRequest request,
            Authentication authentication) {
        try {
            GameDayResponse response = leagueGameDayService.submitMatchScoreAsPlayer(
                tournamentId, dayId, groupId, matchId, request, authentication.getName());
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (ObjectOptimisticLockingFailureException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new GameDayResponse(false, "Score was already submitted by another player"));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new GameDayResponse(false, e.getMessage()));
        }
    }
}

