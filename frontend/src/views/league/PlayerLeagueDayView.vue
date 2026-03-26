<template>
  <div class="container-fluid mt-4">
    <!-- Back Button -->
    <div class="row mb-4">
      <div class="col-12">
        <button class="btn btn-secondary" @click="$router.push('/tournaments/' + tournamentId + '/player-view')">
          <i class="bi bi-arrow-left me-2"></i>Back to Tournament
        </button>
      </div>
    </div>

    <!-- Access Denied -->
    <div v-if="!isPlayer" class="row">
      <div class="col-12">
        <div class="alert alert-danger">
          <i class="bi bi-lock me-2"></i>Access denied. This page is for registered players only.
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-else-if="loading" class="row">
      <div class="col-12">
        <div class="alert alert-info">
          <i class="bi bi-hourglass-split me-2"></i>Loading game day...
        </div>
      </div>
    </div>

    <!-- Error -->
    <div v-if="error && !loading && isPlayer" class="row">
      <div class="col-12">
        <div class="alert alert-danger alert-dismissible fade show">
          <i class="bi bi-exclamation-triangle me-2"></i>{{ error }}
          <button type="button" class="btn-close" @click="error = null"></button>
        </div>
      </div>
    </div>

    <!-- Success -->
    <div v-if="successMessage" class="row">
      <div class="col-12">
        <div class="alert alert-success alert-dismissible fade show">
          <i class="bi bi-check-circle me-2"></i>{{ successMessage }}
          <button type="button" class="btn-close" @click="successMessage = null"></button>
        </div>
      </div>
    </div>

    <div v-if="isPlayer && !loading && gameDay">

      <!-- Header Card -->
      <div class="card mb-4">
        <div class="card-header bg-light d-flex align-items-center gap-3 flex-wrap">
          <h4 class="mb-0">
            <i class="bi bi-calendar-event me-2"></i>League Day — {{ gameDay.gameDate }}
          </h4>
          <span
            class="badge fs-6"
            :class="{
              'bg-warning text-dark': gameDay.status === 'PENDING',
              'bg-success': gameDay.status === 'ONGOING',
              'bg-secondary': gameDay.status === 'COMPLETED'
            }"
          >{{ gameDay.status }}</span>
        </div>

        <div v-if="gameDay.status === 'COMPLETED'" class="card-body">
          <div class="alert alert-secondary mb-0">
            <i class="bi bi-check-all me-2"></i>This game day has been completed. Scores are read-only.
          </div>
        </div>

        <div v-if="gameDay.status === 'PENDING'" class="card-body">
          <div class="alert alert-warning mb-0">
            <i class="bi bi-hourglass me-2"></i>This game day has not started yet. Check back when it's ONGOING.
          </div>
        </div>
      </div>

      <!-- No groups found for this player -->
      <div v-if="!gameDay.groups || gameDay.groups.length === 0" class="alert alert-info">
        <i class="bi bi-person-x me-2"></i>You are not assigned to any group for this game day.
      </div>

      <!-- Group tabs (typically just one group per player) -->
      <div v-else>
        <ul class="nav nav-tabs mb-0" role="tablist">
          <li
            v-for="(group, idx) in gameDay.groups"
            :key="group.id"
            class="nav-item"
            role="presentation"
          >
            <button
              class="nav-link"
              :class="{ active: activeTab === idx }"
              @click="activeTab = idx"
              type="button"
            >
              <i class="bi bi-people me-1"></i>Group {{ group.groupNumber }}
              <span class="badge bg-secondary ms-1">{{ group.players.length }}</span>
            </button>
          </li>
        </ul>

        <div class="tab-content border border-top-0 rounded-bottom bg-white">
          <div
            v-for="(group, idx) in gameDay.groups"
            :key="group.id"
            class="tab-pane p-3"
            :class="{ 'show active d-block': activeTab === idx }"
          >
            <!-- Players summary -->
            <div class="mb-3">
              <h6 class="text-muted mb-2"><i class="bi bi-person-lines-fill me-1"></i>Players in Your Group</h6>
              <div class="d-flex flex-wrap gap-2">
                <span
                  v-for="(player, pi) in group.players"
                  :key="player.tournamentPlayerId"
                  class="badge px-2 py-1"
                  :class="isCurrentPlayer(player) ? 'bg-success' : 'bg-primary-subtle text-primary-emphasis border border-primary-subtle'"
                >
                  <strong>{{ positionLabel(pi) }}</strong> — {{ player.firstName }} {{ player.lastName }}
                  <span class="text-muted ms-1">({{ player.rankScore }})</span>
                  <span v-if="isCurrentPlayer(player)" class="ms-1">★ You</span>
                </span>
              </div>
            </div>

            <hr class="my-2">

            <!-- Matches table -->
            <h6 class="text-muted mb-2"><i class="bi bi-trophy me-1"></i>Your Matches</h6>
            <div class="table-responsive">
              <table class="table table-bordered table-hover align-middle mb-0">
                <thead class="table-light">
                  <tr>
                    <th class="text-center" style="width:50px">#</th>
                    <th>Team 1</th>
                    <th class="text-center" style="width:90px">T1 Score</th>
                    <th class="text-center" style="width:30px"></th>
                    <th class="text-center" style="width:90px">T2 Score</th>
                    <th>Team 2</th>
                    <th class="text-center" style="width:100px" v-if="gameDay.status === 'ONGOING'">Action</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="match in group.matches"
                    :key="match.id"
                    :class="{ 'table-success': isMatchLocked(match) && gameDay.status === 'ONGOING' }"
                  >
                    <td class="text-center text-muted">{{ match.matchOrder }}</td>
                    <td>
                      <div :class="{ 'fw-bold text-success': isCurrentPlayer({ tournamentPlayerId: match.team1Player1Id }) || isCurrentPlayer({ tournamentPlayerId: match.team1Player2Id }) }">
                        <div>{{ match.team1Player1Name }}</div>
                        <div>{{ match.team1Player2Name }}</div>
                      </div>
                    </td>

                    <!-- Team 1 Score -->
                    <td class="text-center">
                      <input
                        v-if="gameDay.status === 'ONGOING' && !isMatchLocked(match)"
                        type="number"
                        min="0"
                        class="form-control form-control-sm text-center score-input"
                        v-model.number="scoreInputs[match.id].team1Score"
                      />
                      <span v-else class="score-display">
                        {{ match.team1Score != null ? match.team1Score : '—' }}
                      </span>
                    </td>

                    <td class="text-center text-muted fw-bold">vs</td>

                    <!-- Team 2 Score -->
                    <td class="text-center">
                      <input
                        v-if="gameDay.status === 'ONGOING' && !isMatchLocked(match)"
                        type="number"
                        min="0"
                        class="form-control form-control-sm text-center score-input"
                        v-model.number="scoreInputs[match.id].team2Score"
                      />
                      <span v-else class="score-display">
                        {{ match.team2Score != null ? match.team2Score : '—' }}
                      </span>
                    </td>

                    <td>
                      <div :class="{ 'fw-bold text-success': isCurrentPlayer({ tournamentPlayerId: match.team2Player1Id }) || isCurrentPlayer({ tournamentPlayerId: match.team2Player2Id }) }">
                        <div>{{ match.team2Player1Name }}</div>
                        <div>{{ match.team2Player2Name }}</div>
                      </div>
                    </td>

                    <!-- Action column -->
                    <td class="text-center" v-if="gameDay.status === 'ONGOING'">
                      <!-- Locked state -->
                      <span v-if="isMatchLocked(match)" class="badge bg-secondary">
                        <i class="bi bi-lock-fill me-1"></i>Locked
                      </span>
                      <!-- Save button -->
                      <button
                        v-else
                        class="btn btn-sm btn-primary"
                        @click="saveScore(group, match)"
                        :disabled="savingMatch === match.id"
                      >
                        <span v-if="savingMatch === match.id">
                          <i class="bi bi-hourglass-split"></i>
                        </span>
                        <span v-else>
                          <i class="bi bi-floppy me-1"></i>Save
                        </span>
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { tournamentAPI } from '@/services/api'

export default {
  name: 'PlayerLeagueDayView',

  data () {
    return {
      tournamentId: null,
      dayId: null,
      gameDay: null,
      loading: false,
      error: null,
      successMessage: null,
      activeTab: 0,
      scoreInputs: {},
      savingMatch: null
    }
  },

  computed: {
    ...mapGetters('auth', ['isPlayer', 'currentUser']),

    // Map tournamentPlayerId → true for the current logged-in player
    myTournamentPlayerIds () {
      if (!this.gameDay) return new Set()
      const myUserId = this.currentUser?.id
      const ids = new Set()
      for (const group of (this.gameDay.groups || [])) {
        for (const p of (group.players || [])) {
          if (p.userId === myUserId) ids.add(p.tournamentPlayerId)
        }
      }
      return ids
    }
  },

  mounted () {
    this.tournamentId = this.$route.params.id
    this.dayId = this.$route.params.dayId
    if (this.isPlayer) {
      this.loadGameDay()
    }
  },

  methods: {
    async loadGameDay () {
      this.loading = true
      this.error = null
      try {
        const res = await tournamentAPI.getGameDayForPlayer(this.tournamentId, this.dayId)
        if (res.data.success) {
          this.gameDay = res.data.gameDay
          this.initScoreInputs()
        } else {
          this.error = res.data.message || 'Failed to load game day'
        }
      } catch (err) {
        this.error = err.response?.data?.message || 'Error loading game day'
        console.error('Error loading game day:', err)
      } finally {
        this.loading = false
      }
    },

    initScoreInputs () {
      const inputs = {}
      for (const group of (this.gameDay?.groups || [])) {
        for (const match of (group.matches || [])) {
          inputs[match.id] = {
            team1Score: match.team1Score != null ? match.team1Score : '',
            team2Score: match.team2Score != null ? match.team2Score : ''
          }
        }
      }
      this.scoreInputs = inputs
    },

    positionLabel (index) {
      return String.fromCharCode(65 + index)
    },

    isCurrentPlayer (player) {
      return this.myTournamentPlayerIds.has(player.tournamentPlayerId)
    },

    /** A match is locked once either score field is non-null (first submission wins). */
    isMatchLocked (match) {
      return match.team1Score != null
    },

    async saveScore (group, match) {
      const input = this.scoreInputs[match.id]
      if (input.team1Score === '' || input.team1Score === null || input.team1Score === undefined ||
          input.team2Score === '' || input.team2Score === null || input.team2Score === undefined) {
        this.error = 'Both scores are required before saving.'
        return
      }
      if (input.team1Score < 0 || input.team2Score < 0) {
        this.error = 'Scores must be non-negative.'
        return
      }

      this.savingMatch = match.id
      this.error = null
      this.successMessage = null
      try {
        const res = await tournamentAPI.submitPlayerMatchScore(
          this.tournamentId,
          this.dayId,
          group.id,
          match.id,
          { team1Score: input.team1Score, team2Score: input.team2Score }
        )
        if (res.data.success) {
          this.gameDay = res.data.gameDay
          this.initScoreInputs()
          this.successMessage = `Match ${match.matchOrder} score saved successfully.`
        } else {
          this.error = res.data.message || 'Failed to save score'
          // If already submitted by another player, refresh to show their score
          if (res.data.message && res.data.message.includes('already been submitted')) {
            await this.loadGameDay()
          }
        }
      } catch (err) {
        // 409 = concurrent submission — refresh to show the winning score
        if (err.response?.status === 409) {
          this.error = 'Score was already submitted by another player. Refreshing...'
          await this.loadGameDay()
        } else {
          this.error = err.response?.data?.message || 'Error saving score'
        }
        console.error('Error saving score:', err)
      } finally {
        this.savingMatch = null
      }
    }
  }
}
</script>

<style scoped>
.card {
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
}
.score-input {
  width: 72px;
  margin: 0 auto;
}
.score-display {
  font-family: 'Courier New', monospace;
  font-size: 1rem;
  font-weight: 600;
}
.nav-tabs .nav-link {
  cursor: pointer;
}
</style>
