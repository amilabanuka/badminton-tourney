package nl.amila.badminton.manager.controller;

import nl.amila.badminton.manager.dto.CreateGameDayRequest;
import nl.amila.badminton.manager.dto.GameDayResponse;
import nl.amila.badminton.manager.dto.SubmitMatchScoreRequest;
import nl.amila.badminton.manager.service.LeagueGameDayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}

