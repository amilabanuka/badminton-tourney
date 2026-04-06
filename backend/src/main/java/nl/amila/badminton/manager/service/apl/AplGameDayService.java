package nl.amila.badminton.manager.service.apl;

import nl.amila.badminton.manager.dto.apl.AplCreateGameDayRequest;
import nl.amila.badminton.manager.dto.apl.AplGameDayResponse;
import nl.amila.badminton.manager.dto.apl.AplPlayerHistoryResponse;
import nl.amila.badminton.manager.dto.apl.AplSubmitMatchScoreRequest;
import nl.amila.badminton.manager.entity.*;
import nl.amila.badminton.manager.entity.apl.AplGameDay;
import nl.amila.badminton.manager.entity.apl.AplGameDayGroup;
import nl.amila.badminton.manager.entity.apl.AplGameDayGroupMatch;
import nl.amila.badminton.manager.entity.apl.AplGameDayGroupPlayer;
import nl.amila.badminton.manager.entity.apl.AplGameDayStatus;
import nl.amila.badminton.manager.entity.apl.AplTournamentSettings;
import nl.amila.badminton.manager.entity.apl.AplRankScoreHistory;
import nl.amila.badminton.manager.repository.apl.AplGameDayGroupMatchRepository;
import nl.amila.badminton.manager.repository.apl.AplGameDayRepository;
import nl.amila.badminton.manager.repository.apl.AplTournamentSettingsRepository;
import nl.amila.badminton.manager.repository.apl.AplRankScoreHistoryRepository;
import nl.amila.badminton.manager.repository.TournamentPlayerRepository;
import nl.amila.badminton.manager.repository.TournamentRepository;
import nl.amila.badminton.manager.repository.UserRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AplGameDayService {

    /**
     * Player counts that cannot be split into groups of 4 or 5 (up to MAX_PLAYERS).
     * A count n is valid iff n = 4a + 5b (a,b >= 0). Invalid values up to 32: {1,2,3,6,7,11}.
     */
    private static final Set<Integer> INVALID_COUNTS = Set.of(1, 2, 3, 6, 7, 11);
    private static final int MAX_PLAYERS = 32;

    private final TournamentRepository tournamentRepository;
    private final TournamentPlayerRepository tournamentPlayerRepository;
    private final AplGameDayRepository aplGameDayRepository;
    private final AplGameDayGroupMatchRepository matchRepository;
    private final UserRepository userRepository;
    private final AplTournamentSettingsRepository aplSettingsRepository;
    private final AplRankScoreHistoryRepository aplRankScoreHistoryRepository;

    public AplGameDayService(TournamentRepository tournamentRepository,
                             TournamentPlayerRepository tournamentPlayerRepository,
                             AplGameDayRepository aplGameDayRepository,
                             AplGameDayGroupMatchRepository matchRepository,
                             UserRepository userRepository,
                             AplTournamentSettingsRepository aplSettingsRepository,
                             AplRankScoreHistoryRepository aplRankScoreHistoryRepository) {
        this.tournamentRepository = tournamentRepository;
        this.tournamentPlayerRepository = tournamentPlayerRepository;
        this.aplGameDayRepository = aplGameDayRepository;
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
        this.aplSettingsRepository = aplSettingsRepository;
        this.aplRankScoreHistoryRepository = aplRankScoreHistoryRepository;
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
     * Create a new APL game day with auto-computed, rank-ordered, randomly-spread groups.
     */
    @Transactional
    public AplGameDayResponse createGameDay(Long tournamentId, AplCreateGameDayRequest request, String callerUsername) {
        // Validate tournament
        Optional<Tournament> tournamentOpt = tournamentRepository.findById(tournamentId);
        if (tournamentOpt.isEmpty()) {
            return new AplGameDayResponse(false, "Tournament not found");
        }
        Tournament tournament = tournamentOpt.get();

        if (tournament.getType() != TournamentType.APL) {
            return new AplGameDayResponse(false, "Game days can only be created for APL tournaments");
        }
        if (!tournament.isEnabled()) {
            return new AplGameDayResponse(false, "Tournament is disabled");
        }

        // Access check
        if (!isAuthorized(tournament, callerUsername)) {
            return new AplGameDayResponse(false, "Access denied: you are not an admin of this tournament");
        }

        // Validate date
        if (request.getGameDate() == null || request.getGameDate().isBlank()) {
            return new AplGameDayResponse(false, "Game date is required");
        }
        LocalDate gameDate;
        try {
            gameDate = LocalDate.parse(request.getGameDate());
        } catch (DateTimeParseException e) {
            return new AplGameDayResponse(false, "Invalid date format. Use YYYY-MM-DD");
        }

        // Validate player list
        List<Long> playerIds = request.getPlayerIds();
        if (playerIds == null || playerIds.isEmpty()) {
            return new AplGameDayResponse(false, "Player list is required");
        }
        int n = playerIds.size();
        if (!isValidPlayerCount(n)) {
            if (n < 4) return new AplGameDayResponse(false, "Cannot create a game day with fewer than 4 players");
            if (n > MAX_PLAYERS) return new AplGameDayResponse(false, "Cannot create a game day with more than " + MAX_PLAYERS + " players");
            return new AplGameDayResponse(false, "Player count of " + n + " cannot be split into groups of 4 or 5. Invalid counts: 6, 7, 11");
        }

        // Resolve TournamentPlayer records, validate they belong to this tournament and are ENABLED/ACTIVE
        List<TournamentPlayer> selectedPlayers = new ArrayList<>();
        for (Long tpId : playerIds) {
            Optional<TournamentPlayer> tpOpt = tournamentPlayerRepository.findById(tpId);
            if (tpOpt.isEmpty()) {
                return new AplGameDayResponse(false, "Tournament player not found: " + tpId);
            }
            TournamentPlayer tp = tpOpt.get();
            if (!tp.getTournament().getId().equals(tournamentId)) {
                return new AplGameDayResponse(false, "Player " + tpId + " does not belong to this tournament");
            }
            if (tp.getStatus() == PlayerStatus.DISABLED) {
                return new AplGameDayResponse(false, "Player " + tp.getUser().getFirstName() + " " + tp.getUser().getLastName() + " is DISABLED and cannot be added to a game day");
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
        if (aplGameDayRepository.existsByTournamentIdAndGameDate(tournamentId, gameDate)) {
            return new AplGameDayResponse(false, "A game day already exists for " + gameDate + " in this tournament");
        }

        // Build the game day and groups (players only — no matches yet)
        AplGameDay gameDay = new AplGameDay(tournament, gameDate);
        int playerIndex = 0;
        for (int g = 0; g < groupSizes.size(); g++) {
            AplGameDayGroup group = new AplGameDayGroup(gameDay, g + 1);
            int size = groupSizes.get(g);
            for (int p = 0; p < size; p++) {
                group.getPlayers().add(new AplGameDayGroupPlayer(group, selectedPlayers.get(playerIndex++)));
            }
            gameDay.getGroups().add(group);
        }

        // First save: persists game day, groups, and group players — all get DB-assigned ids
        AplGameDay saved = aplGameDayRepository.save(gameDay);

        // Second pass: generate match combinations now that group players have ids
        for (AplGameDayGroup group : saved.getGroups()) {
            generateMatches(group);
        }
        // Save again to persist the matches (cascade ALL on matches covers this)
        saved = aplGameDayRepository.save(saved);

        return new AplGameDayResponse(true, "Game day created successfully", toDto(saved));
    }

    /**
     * Get a game day by id. Only accessible by ADMIN or TOURNY_ADMIN of the tournament.
     */
    @Transactional(readOnly = true)
    public AplGameDayResponse getGameDay(Long tournamentId, Long dayId, String callerUsername) {
        Optional<AplGameDay> dayOpt = aplGameDayRepository.findByIdWithAll(dayId);
        if (dayOpt.isEmpty() || !dayOpt.get().getTournament().getId().equals(tournamentId)) {
            return new AplGameDayResponse(false, "Game day not found");
        }
        AplGameDay day = dayOpt.get();
        if (!isAuthorized(day.getTournament(), callerUsername)) {
            return new AplGameDayResponse(false, "Access denied");
        }
        return new AplGameDayResponse(true, "Game day retrieved successfully", toDto(day));
    }

    /**
     * Get all game days for a tournament. Only accessible by ADMIN or TOURNY_ADMIN of the tournament.
     */
    public AplGameDayResponse getGameDays(Long tournamentId, String callerUsername) {
        Optional<Tournament> tournamentOpt = tournamentRepository.findById(tournamentId);
        if (tournamentOpt.isEmpty()) {
            return new AplGameDayResponse(false, "Tournament not found");
        }
        Tournament tournament = tournamentOpt.get();
        if (!isAuthorized(tournament, callerUsername)) {
            return new AplGameDayResponse(false, "Access denied");
        }
        List<AplGameDayResponse.GameDayDto> dtos = aplGameDayRepository
                .findByTournamentIdOrderByGameDateDesc(tournamentId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return new AplGameDayResponse(true, "Game days retrieved successfully", dtos);
    }

    /**
     * Start a game day: PENDING → ONGOING.
     */
    @Transactional
    public AplGameDayResponse startGameDay(Long tournamentId, Long dayId, String callerUsername) {
        Optional<AplGameDay> dayOpt = aplGameDayRepository.findByIdWithAll(dayId);
        if (dayOpt.isEmpty() || !dayOpt.get().getTournament().getId().equals(tournamentId)) {
            return new AplGameDayResponse(false, "Game day not found");
        }
        AplGameDay day = dayOpt.get();
        if (!isAuthorized(day.getTournament(), callerUsername)) {
            return new AplGameDayResponse(false, "Access denied");
        }
        if (day.getStatus() != AplGameDayStatus.PENDING) {
            return new AplGameDayResponse(false, "Only PENDING game days can be started");
        }
        day.setStatus(AplGameDayStatus.ONGOING);
        day.setUpdatedAt(System.currentTimeMillis());
        aplGameDayRepository.save(day);
        return new AplGameDayResponse(true, "Game day started successfully", toDto(day));
    }

    /**
     * Discard a game day: must be PENDING → delete (cascade removes groups and players).
     */
    @Transactional
    public AplGameDayResponse discardGameDay(Long tournamentId, Long dayId, String callerUsername) {
        Optional<AplGameDay> dayOpt = aplGameDayRepository.findById(dayId);
        if (dayOpt.isEmpty() || !dayOpt.get().getTournament().getId().equals(tournamentId)) {
            return new AplGameDayResponse(false, "Game day not found");
        }
        AplGameDay day = dayOpt.get();
        if (!isAuthorized(day.getTournament(), callerUsername)) {
            return new AplGameDayResponse(false, "Access denied");
        }
        if (day.getStatus() != AplGameDayStatus.PENDING) {
            return new AplGameDayResponse(false, "Only PENDING game days can be discarded");
        }
        aplGameDayRepository.delete(day);
        return new AplGameDayResponse(true, "Game day discarded successfully");
    }

    /**
     * Cancel a game day: PENDING or ONGOING → delete (cascade removes all groups, players, and matches).
     */
    @Transactional
    public AplGameDayResponse cancelGameDay(Long tournamentId, Long dayId, String callerUsername) {
        Optional<AplGameDay> dayOpt = aplGameDayRepository.findById(dayId);
        if (dayOpt.isEmpty() || !dayOpt.get().getTournament().getId().equals(tournamentId)) {
            return new AplGameDayResponse(false, "Game day not found");
        }
        AplGameDay day = dayOpt.get();
        if (!isAuthorized(day.getTournament(), callerUsername)) {
            return new AplGameDayResponse(false, "Access denied");
        }
        if (day.getStatus() == AplGameDayStatus.COMPLETED) {
            return new AplGameDayResponse(false, "Completed game days cannot be cancelled");
        }
        aplGameDayRepository.delete(day);
        return new AplGameDayResponse(true, "Game day cancelled successfully");
    }

    /**
     * Submit or overwrite the score for a single match.
     * Only allowed when the game day is ONGOING.
     */
    @Transactional
    public AplGameDayResponse submitMatchScore(Long tournamentId, Long dayId, Long groupId, Long matchId,
                                               AplSubmitMatchScoreRequest request, String callerUsername) {
        Optional<AplGameDay> dayOpt = aplGameDayRepository.findByIdWithAll(dayId);
        if (dayOpt.isEmpty() || !dayOpt.get().getTournament().getId().equals(tournamentId)) {
            return new AplGameDayResponse(false, "Game day not found");
        }
        AplGameDay day = dayOpt.get();
        if (!isAuthorized(day.getTournament(), callerUsername)) {
            return new AplGameDayResponse(false, "Access denied");
        }
        if (day.getStatus() != AplGameDayStatus.ONGOING) {
            return new AplGameDayResponse(false, "Scores can only be submitted for ONGOING game days");
        }

        // Validate scores
        if (request.getTeam1Score() == null || request.getTeam2Score() == null) {
            return new AplGameDayResponse(false, "Both team1Score and team2Score are required");
        }
        if (request.getTeam1Score() < 0 || request.getTeam2Score() < 0) {
            return new AplGameDayResponse(false, "Scores must be non-negative");
        }

        // Find the match, ensure it belongs to the right group and game day
        Optional<AplGameDayGroupMatch> matchOpt = matchRepository.findById(matchId);
        if (matchOpt.isEmpty()) {
            return new AplGameDayResponse(false, "Match not found");
        }
        AplGameDayGroupMatch match = matchOpt.get();
        if (!match.getGroup().getId().equals(groupId) || !match.getGroup().getGameDay().getId().equals(dayId)) {
            return new AplGameDayResponse(false, "Match does not belong to the specified group/day");
        }

        match.setTeam1Score(request.getTeam1Score());
        match.setTeam2Score(request.getTeam2Score());
        matchRepository.save(match);

        // Re-fetch with full joins so the returned DTO reflects the updated score
        AplGameDay refreshed = aplGameDayRepository.findByIdWithAll(dayId).orElse(day);
        return new AplGameDayResponse(true, "Score submitted successfully", toDto(refreshed));
    }

    /**
     * Finish a game day: ONGOING → COMPLETED.
     * Validates all matches have scores, runs Modified-ELO calculation,
     * saves rank score history per player per match, and updates player rank scores.
     */
    @Transactional
    public AplGameDayResponse finishGameDay(Long tournamentId, Long dayId, String callerUsername) {
        Optional<AplGameDay> dayOpt = aplGameDayRepository.findByIdWithAll(dayId);
        if (dayOpt.isEmpty() || !dayOpt.get().getTournament().getId().equals(tournamentId)) {
            return new AplGameDayResponse(false, "Game day not found");
        }
        AplGameDay day = dayOpt.get();
        if (!isAuthorized(day.getTournament(), callerUsername)) {
            return new AplGameDayResponse(false, "Access denied");
        }
        if (day.getStatus() != AplGameDayStatus.ONGOING) {
            return new AplGameDayResponse(false, "Only ONGOING game days can be finished");
        }

        // Validate all matches have scores
        for (AplGameDayGroup group : day.getGroups()) {
            for (AplGameDayGroupMatch match : group.getMatches()) {
                if (match.getTeam1Score() == null || match.getTeam2Score() == null) {
                    return new AplGameDayResponse(false,
                        "All matches must have scores before finishing. Match #"
                        + match.getMatchOrder() + " in Group " + group.getGroupNumber() + " is missing a score.");
                }
            }
        }

        // Load ELO config
        Optional<AplTournamentSettings> settingsOpt =
            aplSettingsRepository.findByTournamentId(tournamentId);
        if (settingsOpt.isEmpty() || !(settingsOpt.get().getRankingConfig() instanceof ModifiedEloConfig eloConfig)) {
            return new AplGameDayResponse(false, "APL ELO settings not found for this tournament");
        }
        AplTournamentSettings settings = settingsOpt.get();
        double K = eloConfig.k();

        // For each match: compute ELO delta, save history rows (using current rankScore as baseline),
        // then accumulate deltas. History records the pre-delta score for each player.
        Map<Long, BigDecimal> deltas = new HashMap<>();

        for (AplGameDayGroup group : day.getGroups()) {
            for (AplGameDayGroupMatch match : group.getMatches()) {
                TournamentPlayer tp1p1 = match.getTeam1Player1().getTournamentPlayer();
                TournamentPlayer tp1p2 = match.getTeam1Player2().getTournamentPlayer();
                TournamentPlayer tp2p1 = match.getTeam2Player1().getTournamentPlayer();
                TournamentPlayer tp2p2 = match.getTeam2Player2().getTournamentPlayer();

                double scoreK = tp1p1.getRankScore().doubleValue();
                double scoreL = tp1p2.getRankScore().doubleValue();
                double scoreM = tp2p1.getRankScore().doubleValue();
                double scoreN = tp2p2.getRankScore().doubleValue();

                double X = (scoreK + scoreL) / 2.0; // team1 average strength
                double Y = (scoreM + scoreN) / 2.0; // team2 average strength
                double T = 1.0 / (1.0 + Math.pow(10.0, (Y - X) / 480.0));

                boolean team1Wins = match.getTeam1Score() > match.getTeam2Score();
                double team1Delta = team1Wins ? K * T : -K * T;
                double team2Delta = -team1Delta;

                BigDecimal bd1 = BigDecimal.valueOf(team1Delta).setScale(2, RoundingMode.HALF_UP);
                BigDecimal bd2 = BigDecimal.valueOf(team2Delta).setScale(2, RoundingMode.HALF_UP);

                // Save history with current (pre-delta) score as previous
                aplRankScoreHistoryRepository.save(new AplRankScoreHistory(tp1p1, match,
                    tp1p1.getRankScore(), tp1p1.getRankScore().add(bd1)));
                aplRankScoreHistoryRepository.save(new AplRankScoreHistory(tp1p2, match,
                    tp1p2.getRankScore(), tp1p2.getRankScore().add(bd1)));
                aplRankScoreHistoryRepository.save(new AplRankScoreHistory(tp2p1, match,
                    tp2p1.getRankScore(), tp2p1.getRankScore().add(bd2)));
                aplRankScoreHistoryRepository.save(new AplRankScoreHistory(tp2p2, match,
                    tp2p2.getRankScore(), tp2p2.getRankScore().add(bd2)));

                // Accumulate deltas
                deltas.merge(tp1p1.getId(), bd1, BigDecimal::add);
                deltas.merge(tp1p2.getId(), bd1, BigDecimal::add);
                deltas.merge(tp2p1.getId(), bd2, BigDecimal::add);
                deltas.merge(tp2p2.getId(), bd2, BigDecimal::add);
            }
        }

        // Apply accumulated deltas and persist updated rank scores
        for (Map.Entry<Long, BigDecimal> entry : deltas.entrySet()) {
            tournamentPlayerRepository.findById(entry.getKey()).ifPresent(tp -> {
                tp.setRankScore(tp.getRankScore().add(entry.getValue()));
                tournamentPlayerRepository.save(tp);
            });
        }

        // Process absences: deduct demerit points and possibly disable absent players
        boolean hasDemeritConfig = settings.getAbsenteeDemeritPoints() != null
            && !settings.getAbsenteeDemeritPoints().isBlank();
        boolean hasDeactivationConfig = settings.getDeactivationCount() != null;
        if (hasDemeritConfig || hasDeactivationConfig) {
            Set<Long> participantIds = day.getGroups().stream()
                .flatMap(g -> g.getPlayers().stream())
                .map(gp -> gp.getTournamentPlayer().getId())
                .collect(Collectors.toSet());

            int[] demeritPoints = parseDemeritPoints(settings.getAbsenteeDemeritPoints());

            List<TournamentPlayer> allPlayers = tournamentPlayerRepository.findByTournamentId(tournamentId);
            for (TournamentPlayer tp : allPlayers) {
                if (tp.getStatus() == PlayerStatus.DISABLED) continue;
                if (participantIds.contains(tp.getId())) continue;

                int consecutiveAbsences = countConsecutiveAbsences(tp.getId(), dayId, tournamentId) + 1;

                if (demeritPoints.length > 0) {
                    int idx = Math.min(consecutiveAbsences - 1, demeritPoints.length - 1);
                    BigDecimal demerit = BigDecimal.valueOf(demeritPoints[idx]);
                    tp.setRankScore(tp.getRankScore().subtract(demerit));
                }

                if (hasDeactivationConfig && consecutiveAbsences >= settings.getDeactivationCount()) {
                    tp.setStatus(PlayerStatus.DISABLED);
                    tp.setStatusChangedAt(System.currentTimeMillis());
                }

                tournamentPlayerRepository.save(tp);
            }
        }

        day.setStatus(AplGameDayStatus.COMPLETED);
        day.setUpdatedAt(System.currentTimeMillis());
        aplGameDayRepository.save(day);

        AplGameDay refreshed = aplGameDayRepository.findByIdWithAll(dayId).orElse(day);
        return new AplGameDayResponse(true, "Game day finished and rankings updated", toDto(refreshed));
    }

    private int countConsecutiveAbsences(Long playerId, Long currentDayId, Long tournamentId) {
        List<AplGameDay> history = aplGameDayRepository.findByTournamentIdOrderByGameDateDesc(tournamentId);
        int count = 0;
        for (AplGameDay d : history) {
            if (d.getId().equals(currentDayId)) continue;
            if (d.getStatus() != AplGameDayStatus.COMPLETED) continue;
            if (aplGameDayRepository.isPlayerPresentInGameDay(d.getId(), playerId)) break;
            count++;
        }
        return count;
    }

    private int[] parseDemeritPoints(String raw) {
        if (raw == null || raw.isBlank()) return new int[0];
        String[] parts = raw.split(",");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Integer.parseInt(parts[i].trim());
        }
        return result;
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
    private void generateMatches(AplGameDayGroup group) {
        // Convert Set to indexed List (insertion order preserved via LinkedHashSet + @OrderBy id ASC)
        List<AplGameDayGroupPlayer> p = new ArrayList<>(group.getPlayers());
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
            group.getMatches().add(new AplGameDayGroupMatch(
                    group, i + 1,
                    p.get(s[0]), p.get(s[1]),
                    p.get(s[2]), p.get(s[3])
            ));
        }
    }

    /**
     * Map an AplGameDay entity to a GameDayDto.
     */
    private AplGameDayResponse.GameDayDto toDto(AplGameDay day) {
        AplGameDayResponse.GameDayDto dto = new AplGameDayResponse.GameDayDto();
        dto.setId(day.getId());
        dto.setTournamentId(day.getTournament().getId());
        dto.setGameDate(day.getGameDate().toString());
        dto.setStatus(day.getStatus().name());
        dto.setCreatedAt(day.getCreatedAt());
        dto.setUpdatedAt(day.getUpdatedAt());

        List<AplGameDayResponse.GroupDto> groupDtos = day.getGroups().stream()
                .map(group -> {
                    AplGameDayResponse.GroupDto groupDto = new AplGameDayResponse.GroupDto();
                    groupDto.setId(group.getId());
                    groupDto.setGroupNumber(group.getGroupNumber());
                    List<AplGameDayResponse.GroupPlayerDto> playerDtos = group.getPlayers().stream()
                            .map(gp -> {
                                AplGameDayResponse.GroupPlayerDto pdto = new AplGameDayResponse.GroupPlayerDto();
                                pdto.setTournamentPlayerId(gp.getTournamentPlayer().getId());
                                pdto.setUserId(gp.getTournamentPlayer().getUser().getId());
                                pdto.setFirstName(gp.getTournamentPlayer().getUser().getFirstName());
                                pdto.setLastName(gp.getTournamentPlayer().getUser().getLastName());
                                pdto.setRankScore(gp.getTournamentPlayer().getRankScore());
                                return pdto;
                            })
                            .sorted(Comparator.comparing(AplGameDayResponse.GroupPlayerDto::getRankScore,
                                            Comparator.nullsLast(Comparator.reverseOrder()))
                                    .thenComparing(AplGameDayResponse.GroupPlayerDto::getUserId))
                            .collect(Collectors.toList());
                    groupDto.setPlayers(playerDtos);

                    List<AplGameDayResponse.MatchDto> matchDtos = group.getMatches().stream()
                            .map(m -> {
                                AplGameDayResponse.MatchDto mdto = new AplGameDayResponse.MatchDto();
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

    // ── Player-scoped game day methods ────────────────────────────────────────

    /**
     * Resolve the calling user, verify PLAYER role, and confirm they are a non-DISABLED
     * TournamentPlayer in the given tournament. Returns the TournamentPlayer.
     */
    private TournamentPlayer resolveRegisteredPlayer(Long tournamentId, String callerUsername) {
        User caller = userRepository.findByUsername(callerUsername)
            .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        if (!Role.PLAYER.name().equals(caller.getRole())) {
            throw new AccessDeniedException("Only players can access this resource");
        }
        TournamentPlayer tp = tournamentPlayerRepository
            .findByTournamentIdAndUserId(tournamentId, caller.getId())
            .orElseThrow(() -> new AccessDeniedException("You are not registered in this tournament"));
        if (tp.getStatus() == PlayerStatus.DISABLED) {
            throw new AccessDeniedException("Your participation in this tournament has been disabled");
        }
        return tp;
    }

    /**
     * Get all game days for a tournament (player view).
     * Returns game days ordered ONGOING first, then by date descending.
     */
    @Transactional(readOnly = true)
    public AplGameDayResponse getGameDaysForPlayer(Long tournamentId, String callerUsername) {
        Optional<Tournament> tournamentOpt = tournamentRepository.findById(tournamentId);
        if (tournamentOpt.isEmpty()) {
            return new AplGameDayResponse(false, "Tournament not found");
        }
        try {
            resolveRegisteredPlayer(tournamentId, callerUsername);
        } catch (AccessDeniedException e) {
            return new AplGameDayResponse(false, e.getMessage());
        }
        List<AplGameDayResponse.GameDayDto> dtos = aplGameDayRepository
            .findByTournamentIdOrderByGameDateDesc(tournamentId)
            .stream()
            .map(this::toDto)
            .sorted(Comparator.comparing(
                (AplGameDayResponse.GameDayDto d) -> "ONGOING".equals(d.getStatus()) ? 0 : 1))
            .collect(Collectors.toList());
        return new AplGameDayResponse(true, "Game days retrieved successfully", dtos);
    }

    /**
     * Get a specific game day for a player — filters groups/matches to only those
     * containing the calling player.
     */
    @Transactional(readOnly = true)
    public AplGameDayResponse getGameDayForPlayer(Long tournamentId, Long dayId, String callerUsername) {
        Optional<AplGameDay> dayOpt = aplGameDayRepository.findByIdWithAll(dayId);
        if (dayOpt.isEmpty() || !dayOpt.get().getTournament().getId().equals(tournamentId)) {
            return new AplGameDayResponse(false, "Game day not found");
        }
        TournamentPlayer tp;
        try {
            tp = resolveRegisteredPlayer(tournamentId, callerUsername);
        } catch (AccessDeniedException e) {
            return new AplGameDayResponse(false, e.getMessage());
        }
        AplGameDay day = dayOpt.get();
        AplGameDayResponse.GameDayDto dto = toDto(day);

        // Filter to only groups that contain this player
        Long callerTpId = tp.getId();
        List<AplGameDayResponse.GroupDto> filtered = dto.getGroups().stream()
            .filter(g -> g.getPlayers().stream()
                .anyMatch(p -> callerTpId.equals(p.getTournamentPlayerId())))
            .collect(Collectors.toList());
        dto.setGroups(filtered);

        return new AplGameDayResponse(true, "Game day retrieved successfully", dto);
    }

    /**
     * Submit match score as a player.
     * Rules:
     *  - Caller must be one of the four players in the match.
     *  - Score may only be set once (code-level check); @Version provides DB-level race guard.
     */
    @Transactional
    public AplGameDayResponse submitMatchScoreAsPlayer(Long tournamentId, Long dayId, Long groupId,
                                                       Long matchId, AplSubmitMatchScoreRequest request,
                                                       String callerUsername) {
        Optional<AplGameDay> dayOpt = aplGameDayRepository.findByIdWithAll(dayId);
        if (dayOpt.isEmpty() || !dayOpt.get().getTournament().getId().equals(tournamentId)) {
            return new AplGameDayResponse(false, "Game day not found");
        }
        TournamentPlayer tp;
        try {
            tp = resolveRegisteredPlayer(tournamentId, callerUsername);
        } catch (AccessDeniedException e) {
            return new AplGameDayResponse(false, e.getMessage());
        }
        AplGameDay day = dayOpt.get();
        if (day.getStatus() != AplGameDayStatus.ONGOING) {
            return new AplGameDayResponse(false, "Scores can only be submitted for ONGOING game days");
        }

        // Validate scores
        if (request.getTeam1Score() == null || request.getTeam2Score() == null) {
            return new AplGameDayResponse(false, "Both team1Score and team2Score are required");
        }
        if (request.getTeam1Score() < 0 || request.getTeam2Score() < 0) {
            return new AplGameDayResponse(false, "Scores must be non-negative");
        }

        Optional<AplGameDayGroupMatch> matchOpt = matchRepository.findById(matchId);
        if (matchOpt.isEmpty()) {
            return new AplGameDayResponse(false, "Match not found");
        }
        AplGameDayGroupMatch match = matchOpt.get();
        if (!match.getGroup().getId().equals(groupId) || !match.getGroup().getGameDay().getId().equals(dayId)) {
            return new AplGameDayResponse(false, "Match does not belong to the specified group/day");
        }

        // Verify caller is one of the four players in this match
        Long tpId = tp.getId();
        boolean isParticipant = tpId.equals(match.getTeam1Player1().getTournamentPlayer().getId())
            || tpId.equals(match.getTeam1Player2().getTournamentPlayer().getId())
            || tpId.equals(match.getTeam2Player1().getTournamentPlayer().getId())
            || tpId.equals(match.getTeam2Player2().getTournamentPlayer().getId());
        if (!isParticipant) {
            return new AplGameDayResponse(false, "You are not a participant in this match");
        }

        // Code-level lock: reject if score already set
        if (match.getTeam1Score() != null) {
            return new AplGameDayResponse(false, "Score has already been submitted for this match");
        }

        match.setTeam1Score(request.getTeam1Score());
        match.setTeam2Score(request.getTeam2Score());
        // @Version on the entity provides DB-level optimistic lock —
        // a concurrent save will throw ObjectOptimisticLockingFailureException (caught in controller)
        matchRepository.save(match);

        AplGameDay refreshed = aplGameDayRepository.findByIdWithAll(dayId).orElse(day);
        AplGameDayResponse.GameDayDto dto = toDto(refreshed);

        // Return only the caller's groups
        Long callerTpId = tp.getId();
        List<AplGameDayResponse.GroupDto> filtered = dto.getGroups().stream()
            .filter(g -> g.getPlayers().stream()
                .anyMatch(p -> callerTpId.equals(p.getTournamentPlayerId())))
            .collect(Collectors.toList());
        dto.setGroups(filtered);

        return new AplGameDayResponse(true, "Score submitted successfully", dto);
    }

    /**
     * Get the completed-game-day history for a specific tournament player.
     * Any authenticated user may view any player's history.
     */
    @Transactional(readOnly = true)
    public AplPlayerHistoryResponse getPlayerHistory(Long tournamentId, Long tournamentPlayerId) {
        Optional<TournamentPlayer> tpOpt = tournamentPlayerRepository.findById(tournamentPlayerId);
        if (tpOpt.isEmpty() || !tpOpt.get().getTournament().getId().equals(tournamentId)) {
            return new AplPlayerHistoryResponse(false, "Player not found in this tournament");
        }
        TournamentPlayer tp = tpOpt.get();
        String playerName = tp.getUser().getFirstName() + " " + tp.getUser().getLastName();

        // All history rows for this player, oldest first so match order within a day is chronological
        List<AplRankScoreHistory> historyRows = aplRankScoreHistoryRepository
            .findByTournamentPlayerIdOrderByChangedAtDesc(tournamentPlayerId);

        // Group by game day id, preserving insertion order (reversed so newest day is first)
        Map<Long, List<AplRankScoreHistory>> byDay = new LinkedHashMap<>();
        for (AplRankScoreHistory h : historyRows) {
            Long dayId = h.getMatch().getGroup().getGameDay().getId();
            byDay.computeIfAbsent(dayId, k -> new ArrayList<>()).add(h);
        }

        List<AplPlayerHistoryResponse.GameDayHistoryDto> gameDayDtos = new ArrayList<>();
        for (Map.Entry<Long, List<AplRankScoreHistory>> entry : byDay.entrySet()) {
            List<AplRankScoreHistory> rows = entry.getValue();
            // rows are already newest-first from the repo; reverse to chronological within the day
            List<AplRankScoreHistory> chronological = new ArrayList<>(rows);
            Collections.reverse(chronological);

            String gameDate = chronological.get(0).getMatch().getGroup().getGameDay()
                .getGameDate().toString();

            List<AplPlayerHistoryResponse.MatchHistoryDto> matchDtos = chronological.stream()
                .map(h -> {
                    AplGameDayGroupMatch m = h.getMatch();
                    Long tpId = tp.getId();
                    boolean onTeam1 = tpId.equals(m.getTeam1Player1().getTournamentPlayer().getId())
                        || tpId.equals(m.getTeam1Player2().getTournamentPlayer().getId());
                    return new AplPlayerHistoryResponse.MatchHistoryDto(
                        m.getId(),
                        m.getMatchOrder(),
                        m.getTeam1Player1().getTournamentPlayer().getUser().getFirstName() + " "
                            + m.getTeam1Player1().getTournamentPlayer().getUser().getLastName(),
                        m.getTeam1Player2().getTournamentPlayer().getUser().getFirstName() + " "
                            + m.getTeam1Player2().getTournamentPlayer().getUser().getLastName(),
                        m.getTeam2Player1().getTournamentPlayer().getUser().getFirstName() + " "
                            + m.getTeam2Player1().getTournamentPlayer().getUser().getLastName(),
                        m.getTeam2Player2().getTournamentPlayer().getUser().getFirstName() + " "
                            + m.getTeam2Player2().getTournamentPlayer().getUser().getLastName(),
                        m.getTeam1Score(),
                        m.getTeam2Score(),
                        onTeam1,
                        h.getPreviousScore(),
                        h.getNewScore()
                    );
                })
                .collect(Collectors.toList());

            gameDayDtos.add(new AplPlayerHistoryResponse.GameDayHistoryDto(entry.getKey(), gameDate, matchDtos));
        }

        return new AplPlayerHistoryResponse(true, "History retrieved successfully",
            tournamentPlayerId, playerName, gameDayDtos);
    }
}
