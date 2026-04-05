package nl.amila.badminton.manager.controller.apl;

import nl.amila.badminton.manager.dto.apl.AplCreateGameDayRequest;
import nl.amila.badminton.manager.dto.apl.AplGameDayResponse;
import nl.amila.badminton.manager.dto.apl.AplPlayerHistoryResponse;
import nl.amila.badminton.manager.dto.apl.AplSubmitMatchScoreRequest;
import nl.amila.badminton.manager.service.apl.AplGameDayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tournaments/{tournamentId}/apl-game-days")
@CrossOrigin(origins = "*")
public class AplGameDayController {

    private final AplGameDayService aplGameDayService;

    public AplGameDayController(AplGameDayService aplGameDayService) {
        this.aplGameDayService = aplGameDayService;
    }

    /**
     * Create a new APL game day (ADMIN or TOURNY_ADMIN of the tournament)
     */
    @PostMapping
    public ResponseEntity<AplGameDayResponse> createGameDay(
            @PathVariable Long tournamentId,
            @RequestBody AplCreateGameDayRequest request,
            Authentication authentication) {
        AplGameDayResponse response = aplGameDayService.createGameDay(tournamentId, request, authentication.getName());
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
    public ResponseEntity<AplGameDayResponse> getGameDays(
            @PathVariable Long tournamentId,
            Authentication authentication) {
        AplGameDayResponse response = aplGameDayService.getGameDays(tournamentId, authentication.getName());
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
    public ResponseEntity<AplGameDayResponse> getGameDay(
            @PathVariable Long tournamentId,
            @PathVariable Long dayId,
            Authentication authentication) {
        AplGameDayResponse response = aplGameDayService.getGameDay(tournamentId, dayId, authentication.getName());
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
    public ResponseEntity<AplGameDayResponse> startGameDay(
            @PathVariable Long tournamentId,
            @PathVariable Long dayId,
            Authentication authentication) {
        AplGameDayResponse response = aplGameDayService.startGameDay(tournamentId, dayId, authentication.getName());
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
    public ResponseEntity<AplGameDayResponse> discardGameDay(
            @PathVariable Long tournamentId,
            @PathVariable Long dayId,
            Authentication authentication) {
        AplGameDayResponse response = aplGameDayService.discardGameDay(tournamentId, dayId, authentication.getName());
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
    public ResponseEntity<AplGameDayResponse> cancelGameDay(
            @PathVariable Long tournamentId,
            @PathVariable Long dayId,
            Authentication authentication) {
        AplGameDayResponse response = aplGameDayService.cancelGameDay(tournamentId, dayId, authentication.getName());
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
    public ResponseEntity<AplGameDayResponse> submitMatchScore(
            @PathVariable Long tournamentId,
            @PathVariable Long dayId,
            @PathVariable Long groupId,
            @PathVariable Long matchId,
            @RequestBody AplSubmitMatchScoreRequest request,
            Authentication authentication) {
        AplGameDayResponse response = aplGameDayService.submitMatchScore(
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
    public ResponseEntity<AplGameDayResponse> finishGameDay(
            @PathVariable Long tournamentId,
            @PathVariable Long dayId,
            Authentication authentication) {
        AplGameDayResponse response = aplGameDayService.finishGameDay(tournamentId, dayId, authentication.getName());
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
    public ResponseEntity<AplGameDayResponse> getGameDaysForPlayer(
            @PathVariable Long tournamentId,
            Authentication authentication) {
        AplGameDayResponse response = aplGameDayService.getGameDaysForPlayer(tournamentId, authentication.getName());
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
    public ResponseEntity<AplGameDayResponse> getGameDayForPlayer(
            @PathVariable Long tournamentId,
            @PathVariable Long dayId,
            Authentication authentication) {
        AplGameDayResponse response = aplGameDayService.getGameDayForPlayer(tournamentId, dayId, authentication.getName());
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
    public ResponseEntity<AplGameDayResponse> submitMatchScoreAsPlayer(
            @PathVariable Long tournamentId,
            @PathVariable Long dayId,
            @PathVariable Long groupId,
            @PathVariable Long matchId,
            @RequestBody AplSubmitMatchScoreRequest request,
            Authentication authentication) {
        try {
            AplGameDayResponse response = aplGameDayService.submitMatchScoreAsPlayer(
                tournamentId, dayId, groupId, matchId, request, authentication.getName());
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (ObjectOptimisticLockingFailureException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new AplGameDayResponse(false, "Score was already submitted by another player"));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new AplGameDayResponse(false, e.getMessage()));
        }
    }

    /**
     * Get completed game day history for a specific tournament player.
     * Any authenticated user may view any player's history.
     * URL: GET /api/tournaments/{tournamentId}/apl-game-days/players/{tournamentPlayerId}/history
     */
    @GetMapping("/players/{tournamentPlayerId}/history")
    public ResponseEntity<AplPlayerHistoryResponse> getPlayerHistory(
            @PathVariable Long tournamentId,
            @PathVariable Long tournamentPlayerId) {
        AplPlayerHistoryResponse response = aplGameDayService.getPlayerHistory(tournamentId, tournamentPlayerId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
