package nl.amila.badminton.manager.service;

import nl.amila.badminton.manager.dto.CreateGameDayRequest;
import nl.amila.badminton.manager.dto.GameDayResponse;
import nl.amila.badminton.manager.dto.SubmitMatchScoreRequest;
import nl.amila.badminton.manager.entity.*;
import nl.amila.badminton.manager.repository.LeagueGameDayGroupMatchRepository;
import nl.amila.badminton.manager.repository.LeagueGameDayRepository;
import nl.amila.badminton.manager.repository.TournamentPlayerRepository;
import nl.amila.badminton.manager.repository.TournamentRepository;
import nl.amila.badminton.manager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeagueGameDayService {

    /**
     * Player counts that cannot be split into groups of 4 or 5 (up to MAX_PLAYERS).
     * A count n is valid iff n = 4a + 5b (a,b >= 0). Invalid values up to 32: {1,2,3,6,7,11}.
     */
    private static final Set<Integer> INVALID_COUNTS = Set.of(1, 2, 3, 6, 7, 11);
    private static final int MAX_PLAYERS = 32;

    private final TournamentRepository tournamentRepository;
    private final TournamentPlayerRepository tournamentPlayerRepository;
    private final LeagueGameDayRepository leagueGameDayRepository;
    private final LeagueGameDayGroupMatchRepository matchRepository;
    private final UserRepository userRepository;

    public LeagueGameDayService(TournamentRepository tournamentRepository,
                                TournamentPlayerRepository tournamentPlayerRepository,
                                LeagueGameDayRepository leagueGameDayRepository,
                                LeagueGameDayGroupMatchRepository matchRepository,
                                UserRepository userRepository) {
        this.tournamentRepository = tournamentRepository;
        this.tournamentPlayerRepository = tournamentPlayerRepository;
        this.leagueGameDayRepository = leagueGameDayRepository;
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
    }

    /**
     * Compute a randomly-ordered list of group sizes (each 4 or 5) for n players.
     * Minimises total group count.
     *   n % 4 == 0  → all groups of 4
     *   n % 4 == 2  → two groups of 5, rest groups of 4
     *   n % 4 == 3  → one group of 5, rest groups of 4
     *   n % 4 == 1  → only valid for n=5 (one group of 5); larger cases caught by INVALID_COUNTS
     * The list is shuffled so the spread of 4s and 5s is randomised.
     */
    static List<Integer> computeGroupSizes(int n) {
        int fives;
        switch (n % 4) {
            case 0: fives = 0; break;
            case 2: fives = 2; break;
            case 3: fives = 1; break;
            default: fives = 1; break; // n==5 (n%4==1, only valid case)
        }
        int fours = (n - 5 * fives) / 4;

        List<Integer> sizes = new ArrayList<>();
        for (int i = 0; i < fours; i++) sizes.add(4);
        for (int i = 0; i < fives; i++) sizes.add(5);
        Collections.shuffle(sizes);
        return sizes;
    }

    /**
     * Validate that a player count is groupable.
     */
    static boolean isValidPlayerCount(int n) {
        return n >= 4 && n <= MAX_PLAYERS && !INVALID_COUNTS.contains(n);
    }

    /**
     * Check that the caller is an ADMIN role, or a TOURNY_ADMIN who is an admin of this tournament.
     */
    private boolean isAuthorized(Tournament tournament, String callerUsername) {
        User caller = userRepository.findByUsername(callerUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        if (Role.ADMIN.name().equals(caller.getRole())) {
            return true;
        }
        if (Role.TOURNY_ADMIN.name().equals(caller.getRole())) {
            return tournament.getAdmins().stream()
                    .anyMatch(a -> a.getUser().getId().equals(caller.getId()));
        }
        return false;
    }

    /**
     * Create a new league game day with auto-computed, rank-ordered, randomly-spread groups.
     */
    @Transactional
    public GameDayResponse createGameDay(Long tournamentId, CreateGameDayRequest request, String callerUsername) {
        // Validate tournament
        Optional<Tournament> tournamentOpt = tournamentRepository.findById(tournamentId);
        if (tournamentOpt.isEmpty()) {
            return new GameDayResponse(false, "Tournament not found");
        }
        Tournament tournament = tournamentOpt.get();

        if (tournament.getType() != TournamentType.LEAGUE) {
            return new GameDayResponse(false, "Game days can only be created for LEAGUE tournaments");
        }
        if (!tournament.isEnabled()) {
            return new GameDayResponse(false, "Tournament is disabled");
        }

        // Access check
        if (!isAuthorized(tournament, callerUsername)) {
            return new GameDayResponse(false, "Access denied: you are not an admin of this tournament");
        }

        // Validate date
        if (request.getGameDate() == null || request.getGameDate().isBlank()) {
            return new GameDayResponse(false, "Game date is required");
        }
        LocalDate gameDate;
        try {
            gameDate = LocalDate.parse(request.getGameDate());
        } catch (DateTimeParseException e) {
            return new GameDayResponse(false, "Invalid date format. Use YYYY-MM-DD");
        }

        // Validate player list
        List<Long> playerIds = request.getPlayerIds();
        if (playerIds == null || playerIds.isEmpty()) {
            return new GameDayResponse(false, "Player list is required");
        }
        int n = playerIds.size();
        if (!isValidPlayerCount(n)) {
            if (n < 4) return new GameDayResponse(false, "Cannot create a game day with fewer than 4 players");
            if (n > MAX_PLAYERS) return new GameDayResponse(false, "Cannot create a game day with more than " + MAX_PLAYERS + " players");
            return new GameDayResponse(false, "Player count of " + n + " cannot be split into groups of 4 or 5. Invalid counts: 6, 7, 11");
        }

        // Resolve TournamentPlayer records, validate they belong to this tournament and are ENABLED/ACTIVE
        List<TournamentPlayer> selectedPlayers = new ArrayList<>();
        for (Long tpId : playerIds) {
            Optional<TournamentPlayer> tpOpt = tournamentPlayerRepository.findById(tpId);
            if (tpOpt.isEmpty()) {
                return new GameDayResponse(false, "Tournament player not found: " + tpId);
            }
            TournamentPlayer tp = tpOpt.get();
            if (!tp.getTournament().getId().equals(tournamentId)) {
                return new GameDayResponse(false, "Player " + tpId + " does not belong to this tournament");
            }
            if (tp.getStatus() == PlayerStatus.DISABLED) {
                return new GameDayResponse(false, "Player " + tp.getUser().getFirstName() + " " + tp.getUser().getLastName() + " is DISABLED and cannot be added to a game day");
            }
            selectedPlayers.add(tp);
        }

        // Sort by rankScore descending (players with equal score keep stable order by id)
        selectedPlayers.sort(Comparator
                .comparing(TournamentPlayer::getRankScore, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(tp -> tp.getUser().getId()));

        // Compute randomised group sizes
        List<Integer> groupSizes = computeGroupSizes(n);

        // Pre-check for duplicate date to avoid a constraint violation inside the transaction
        if (leagueGameDayRepository.existsByTournamentIdAndGameDate(tournamentId, gameDate)) {
            return new GameDayResponse(false, "A game day already exists for " + gameDate + " in this tournament");
        }

        // Build the game day and groups (players only — no matches yet)
        LeagueGameDay gameDay = new LeagueGameDay(tournament, gameDate);
        int playerIndex = 0;
        for (int g = 0; g < groupSizes.size(); g++) {
            LeagueGameDayGroup group = new LeagueGameDayGroup(gameDay, g + 1);
            int size = groupSizes.get(g);
            for (int p = 0; p < size; p++) {
                group.getPlayers().add(new LeagueGameDayGroupPlayer(group, selectedPlayers.get(playerIndex++)));
            }
            gameDay.getGroups().add(group);
        }

        // First save: persists game day, groups, and group players — all get DB-assigned ids
        LeagueGameDay saved = leagueGameDayRepository.save(gameDay);

        // Second pass: generate match combinations now that group players have ids
        for (LeagueGameDayGroup group : saved.getGroups()) {
            generateMatches(group);
        }
        // Save again to persist the matches (cascade ALL on matches covers this)
        saved = leagueGameDayRepository.save(saved);

        return new GameDayResponse(true, "Game day created successfully", toDto(saved));
    }

    /**
     * Get a game day by id. Only accessible by ADMIN or TOURNY_ADMIN of the tournament.
     */
    @Transactional(readOnly = true)
    public GameDayResponse getGameDay(Long tournamentId, Long dayId, String callerUsername) {
        Optional<LeagueGameDay> dayOpt = leagueGameDayRepository.findByIdWithAll(dayId);
        if (dayOpt.isEmpty() || !dayOpt.get().getTournament().getId().equals(tournamentId)) {
            return new GameDayResponse(false, "Game day not found");
        }
        LeagueGameDay day = dayOpt.get();
        if (!isAuthorized(day.getTournament(), callerUsername)) {
            return new GameDayResponse(false, "Access denied");
        }
        return new GameDayResponse(true, "Game day retrieved successfully", toDto(day));
    }

    /**
     * Get all game days for a tournament. Only accessible by ADMIN or TOURNY_ADMIN of the tournament.
     */
    public GameDayResponse getGameDays(Long tournamentId, String callerUsername) {
        Optional<Tournament> tournamentOpt = tournamentRepository.findById(tournamentId);
        if (tournamentOpt.isEmpty()) {
            return new GameDayResponse(false, "Tournament not found");
        }
        Tournament tournament = tournamentOpt.get();
        if (!isAuthorized(tournament, callerUsername)) {
            return new GameDayResponse(false, "Access denied");
        }
        List<GameDayResponse.GameDayDto> dtos = leagueGameDayRepository
                .findByTournamentIdOrderByGameDateDesc(tournamentId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return new GameDayResponse(true, "Game days retrieved successfully", dtos);
    }

    /**
     * Start a game day: PENDING → ONGOING.
     */
    @Transactional
    public GameDayResponse startGameDay(Long tournamentId, Long dayId, String callerUsername) {
        Optional<LeagueGameDay> dayOpt = leagueGameDayRepository.findByIdWithAll(dayId);
        if (dayOpt.isEmpty() || !dayOpt.get().getTournament().getId().equals(tournamentId)) {
            return new GameDayResponse(false, "Game day not found");
        }
        LeagueGameDay day = dayOpt.get();
        if (!isAuthorized(day.getTournament(), callerUsername)) {
            return new GameDayResponse(false, "Access denied");
        }
        if (day.getStatus() != GameDayStatus.PENDING) {
            return new GameDayResponse(false, "Only PENDING game days can be started");
        }
        day.setStatus(GameDayStatus.ONGOING);
        day.setUpdatedAt(System.currentTimeMillis());
        leagueGameDayRepository.save(day);
        return new GameDayResponse(true, "Game day started successfully", toDto(day));
    }

    /**
     * Discard a game day: must be PENDING → delete (cascade removes groups and players).
     */
    @Transactional
    public GameDayResponse discardGameDay(Long tournamentId, Long dayId, String callerUsername) {
        Optional<LeagueGameDay> dayOpt = leagueGameDayRepository.findById(dayId);
        if (dayOpt.isEmpty() || !dayOpt.get().getTournament().getId().equals(tournamentId)) {
            return new GameDayResponse(false, "Game day not found");
        }
        LeagueGameDay day = dayOpt.get();
        if (!isAuthorized(day.getTournament(), callerUsername)) {
            return new GameDayResponse(false, "Access denied");
        }
        if (day.getStatus() != GameDayStatus.PENDING) {
            return new GameDayResponse(false, "Only PENDING game days can be discarded");
        }
        leagueGameDayRepository.delete(day);
        return new GameDayResponse(true, "Game day discarded successfully");
    }

    /**
     * Cancel a game day: PENDING or ONGOING → delete (cascade removes all groups, players, and matches).
     */
    @Transactional
    public GameDayResponse cancelGameDay(Long tournamentId, Long dayId, String callerUsername) {
        Optional<LeagueGameDay> dayOpt = leagueGameDayRepository.findById(dayId);
        if (dayOpt.isEmpty() || !dayOpt.get().getTournament().getId().equals(tournamentId)) {
            return new GameDayResponse(false, "Game day not found");
        }
        LeagueGameDay day = dayOpt.get();
        if (!isAuthorized(day.getTournament(), callerUsername)) {
            return new GameDayResponse(false, "Access denied");
        }
        if (day.getStatus() == GameDayStatus.COMPLETED) {
            return new GameDayResponse(false, "Completed game days cannot be cancelled");
        }
        leagueGameDayRepository.delete(day);
        return new GameDayResponse(true, "Game day cancelled successfully");
    }

    /**
     * Submit or overwrite the score for a single match.
     * Only allowed when the game day is ONGOING.
     */
    @Transactional
    public GameDayResponse submitMatchScore(Long tournamentId, Long dayId, Long groupId, Long matchId,
                                            SubmitMatchScoreRequest request, String callerUsername) {
        Optional<LeagueGameDay> dayOpt = leagueGameDayRepository.findByIdWithAll(dayId);
        if (dayOpt.isEmpty() || !dayOpt.get().getTournament().getId().equals(tournamentId)) {
            return new GameDayResponse(false, "Game day not found");
        }
        LeagueGameDay day = dayOpt.get();
        if (!isAuthorized(day.getTournament(), callerUsername)) {
            return new GameDayResponse(false, "Access denied");
        }
        if (day.getStatus() != GameDayStatus.ONGOING) {
            return new GameDayResponse(false, "Scores can only be submitted for ONGOING game days");
        }

        // Validate scores
        if (request.getTeam1Score() == null || request.getTeam2Score() == null) {
            return new GameDayResponse(false, "Both team1Score and team2Score are required");
        }
        if (request.getTeam1Score() < 0 || request.getTeam2Score() < 0) {
            return new GameDayResponse(false, "Scores must be non-negative");
        }

        // Find the match, ensure it belongs to the right group and game day
        Optional<LeagueGameDayGroupMatch> matchOpt = matchRepository.findById(matchId);
        if (matchOpt.isEmpty()) {
            return new GameDayResponse(false, "Match not found");
        }
        LeagueGameDayGroupMatch match = matchOpt.get();
        if (!match.getGroup().getId().equals(groupId) || !match.getGroup().getGameDay().getId().equals(dayId)) {
            return new GameDayResponse(false, "Match does not belong to the specified group/day");
        }

        match.setTeam1Score(request.getTeam1Score());
        match.setTeam2Score(request.getTeam2Score());
        matchRepository.save(match);

        // Re-fetch with full joins so the returned DTO reflects the updated score
        LeagueGameDay refreshed = leagueGameDayRepository.findByIdWithAll(dayId).orElse(day);
        return new GameDayResponse(true, "Score submitted successfully", toDto(refreshed));
    }

    /**
     * Generate match combinations for a group.
     * Players in the group list are already rank-sorted (index 0 = A = highest rank).
     *
     * Size 4 (A,B,C,D) — 3 matches:
     *   1: A,B vs C,D
     *   2: A,C vs B,D
     *   3: A,D vs B,C
     *
     * Size 5 (A,B,C,D,E) — 5 matches:
     *   1: A,B vs C,D
     *   2: A,C vs B,E
     *   3: A,E vs B,D
     *   4: A,D vs C,E
     *   5: B,C vs D,E
     */
    private void generateMatches(LeagueGameDayGroup group) {
        // Convert Set to indexed List (insertion order preserved via LinkedHashSet + @OrderBy id ASC)
        List<LeagueGameDayGroupPlayer> p = new ArrayList<>(group.getPlayers());
        int size = p.size();

        int[][] schedule;
        if (size == 4) {
            // indices: A=0, B=1, C=2, D=3
            schedule = new int[][] {
                {0, 1, 2, 3},   // A,B vs C,D
                {0, 2, 1, 3},   // A,C vs B,D
                {0, 3, 1, 2},   // A,D vs B,C
            };
        } else { // size == 5
            // indices: A=0, B=1, C=2, D=3, E=4
            schedule = new int[][] {
                {0, 1, 2, 3},   // A,B vs C,D
                {0, 2, 1, 4},   // A,C vs B,E
                {0, 4, 1, 3},   // A,E vs B,D
                {0, 3, 2, 4},   // A,D vs C,E
                {1, 2, 3, 4},   // B,C vs D,E
            };
        }

        for (int i = 0; i < schedule.length; i++) {
            int[] s = schedule[i];
            group.getMatches().add(new LeagueGameDayGroupMatch(
                    group, i + 1,
                    p.get(s[0]), p.get(s[1]),
                    p.get(s[2]), p.get(s[3])
            ));
        }
    }

    /**
     * Map a LeagueGameDay entity to a GameDayDto.
     */
    private GameDayResponse.GameDayDto toDto(LeagueGameDay day) {
        GameDayResponse.GameDayDto dto = new GameDayResponse.GameDayDto();
        dto.setId(day.getId());
        dto.setTournamentId(day.getTournament().getId());
        dto.setGameDate(day.getGameDate().toString());
        dto.setStatus(day.getStatus().name());
        dto.setCreatedAt(day.getCreatedAt());
        dto.setUpdatedAt(day.getUpdatedAt());

        List<GameDayResponse.GroupDto> groupDtos = day.getGroups().stream()
                .map(group -> {
                    GameDayResponse.GroupDto groupDto = new GameDayResponse.GroupDto();
                    groupDto.setId(group.getId());
                    groupDto.setGroupNumber(group.getGroupNumber());
                    List<GameDayResponse.GroupPlayerDto> playerDtos = group.getPlayers().stream()
                            .map(gp -> {
                                GameDayResponse.GroupPlayerDto pdto = new GameDayResponse.GroupPlayerDto();
                                pdto.setTournamentPlayerId(gp.getTournamentPlayer().getId());
                                pdto.setUserId(gp.getTournamentPlayer().getUser().getId());
                                pdto.setFirstName(gp.getTournamentPlayer().getUser().getFirstName());
                                pdto.setLastName(gp.getTournamentPlayer().getUser().getLastName());
                                pdto.setRankScore(gp.getTournamentPlayer().getRankScore());
                                return pdto;
                            })
                            .sorted(Comparator.comparing(GameDayResponse.GroupPlayerDto::getRankScore,
                                            Comparator.nullsLast(Comparator.reverseOrder()))
                                    .thenComparing(GameDayResponse.GroupPlayerDto::getUserId))
                            .collect(Collectors.toList());
                    groupDto.setPlayers(playerDtos);

                    List<GameDayResponse.MatchDto> matchDtos = group.getMatches().stream()
                            .map(m -> {
                                GameDayResponse.MatchDto mdto = new GameDayResponse.MatchDto();
                                mdto.setId(m.getId());
                                mdto.setMatchOrder(m.getMatchOrder());
                                mdto.setTeam1Player1Id(m.getTeam1Player1().getTournamentPlayer().getId());
                                mdto.setTeam1Player1Name(m.getTeam1Player1().getTournamentPlayer().getUser().getFirstName()
                                        + " " + m.getTeam1Player1().getTournamentPlayer().getUser().getLastName());
                                mdto.setTeam1Player2Id(m.getTeam1Player2().getTournamentPlayer().getId());
                                mdto.setTeam1Player2Name(m.getTeam1Player2().getTournamentPlayer().getUser().getFirstName()
                                        + " " + m.getTeam1Player2().getTournamentPlayer().getUser().getLastName());
                                mdto.setTeam2Player1Id(m.getTeam2Player1().getTournamentPlayer().getId());
                                mdto.setTeam2Player1Name(m.getTeam2Player1().getTournamentPlayer().getUser().getFirstName()
                                        + " " + m.getTeam2Player1().getTournamentPlayer().getUser().getLastName());
                                mdto.setTeam2Player2Id(m.getTeam2Player2().getTournamentPlayer().getId());
                                mdto.setTeam2Player2Name(m.getTeam2Player2().getTournamentPlayer().getUser().getFirstName()
                                        + " " + m.getTeam2Player2().getTournamentPlayer().getUser().getLastName());
                                mdto.setTeam1Score(m.getTeam1Score());
                                mdto.setTeam2Score(m.getTeam2Score());
                                return mdto;
                            })
                            .collect(Collectors.toList());
                    groupDto.setMatches(matchDtos);

                    return groupDto;
                })
                .collect(Collectors.toList());
        dto.setGroups(groupDtos);
        return dto;
    }
}

