<template>
  <div class="container-fluid mt-4">
    <!-- Back -->
    <div class="row mb-4">
      <div class="col-12">
        <button class="btn btn-secondary" @click="goBack">
          <i class="bi bi-arrow-left me-2"></i>Back
        </button>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
      <p class="mt-3 text-muted">Loading history...</p>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="alert alert-danger">
      <i class="bi bi-exclamation-triangle me-2"></i>{{ error }}
    </div>

    <template v-else-if="history">
      <!-- Player header -->
      <div class="card mb-4">
        <div class="card-header bg-light d-flex align-items-center gap-3">
          <h4 class="mb-0">
            <i class="bi bi-person-lines-fill me-2"></i>{{ history.playerName }}
          </h4>
          <span class="text-muted small">Completed game day history</span>
        </div>
      </div>

      <!-- Empty state -->
      <div v-if="!history.gameDays || history.gameDays.length === 0" class="alert alert-info">
        <i class="bi bi-inbox me-2"></i>No completed game days found for this player.
      </div>

      <!-- Accordion -->
      <div v-else>
        <div
          v-for="(day, idx) in history.gameDays"
          :key="day.gameDayId"
          class="card mb-2"
        >
          <!-- Card header — click to expand/collapse -->
          <div
            class="card-header d-flex justify-content-between align-items-center"
            style="cursor: pointer;"
            @click="toggle(idx)"
          >
            <div class="d-flex align-items-center gap-2">
              <i class="bi bi-calendar-check me-1"></i>
              <span class="fw-semibold">{{ day.gameDate }}</span>
            </div>
            <div class="d-flex align-items-center gap-3">
              <span
                class="badge fs-6 font-monospace px-3 py-2"
                :class="dayNetDelta(day) >= 0 ? 'bg-success' : 'bg-danger'"
              >
                {{ dayNetDelta(day) >= 0 ? '+' : '' }}{{ dayNetDelta(day).toFixed(2) }} pts
              </span>
              <i class="bi" :class="expandedIdx === idx ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
            </div>
          </div>

          <!-- Collapsible body -->
          <div v-if="expandedIdx === idx" class="card-body p-0">
            <div class="table-responsive">
              <table class="table table-bordered table-hover align-middle mb-0">
                <thead class="table-light">
                  <tr>
                    <th class="text-center ps-3" style="width:50px">#</th>
                    <th>Your Team</th>
                    <th class="text-center" style="width:80px">Score</th>
                    <th class="text-center fw-bold text-muted" style="width:36px">vs</th>
                    <th class="text-center" style="width:80px">Score</th>
                    <th>Opponents</th>
                    <th class="text-center pe-3" style="width:110px">Rank &#916;</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="match in day.matches" :key="match.matchId">
                    <td class="text-center text-muted ps-3">{{ match.matchOrder }}</td>

                    <!-- Your team (highlighted green) -->
                    <td>
                      <template v-if="match.playerOnTeam1">
                        <div class="fw-semibold text-success">{{ match.team1Player1Name }}</div>
                        <div class="fw-semibold text-success">{{ match.team1Player2Name }}</div>
                      </template>
                      <template v-else>
                        <div class="fw-semibold text-success">{{ match.team2Player1Name }}</div>
                        <div class="fw-semibold text-success">{{ match.team2Player2Name }}</div>
                      </template>
                    </td>

                    <!-- Your team score -->
                    <td class="text-center score-display">
                      {{ match.playerOnTeam1 ? match.team1Score : match.team2Score }}
                    </td>

                    <td class="text-center text-muted fw-bold">vs</td>

                    <!-- Opponent score -->
                    <td class="text-center score-display">
                      {{ match.playerOnTeam1 ? match.team2Score : match.team1Score }}
                    </td>

                    <!-- Opponents -->
                    <td>
                      <template v-if="match.playerOnTeam1">
                        <div>{{ match.team2Player1Name }}</div>
                        <div>{{ match.team2Player2Name }}</div>
                      </template>
                      <template v-else>
                        <div>{{ match.team1Player1Name }}</div>
                        <div>{{ match.team1Player2Name }}</div>
                      </template>
                    </td>

                    <!-- Rank delta -->
                    <td class="text-center pe-3">
                      <span
                        class="badge font-monospace px-2 py-1"
                        :class="Number(match.scoreDelta) >= 0 ? 'bg-success' : 'bg-danger'"
                      >
                        {{ Number(match.scoreDelta) >= 0 ? '+' : '' }}{{ Number(match.scoreDelta).toFixed(2) }}
                      </span>
                    </td>
                  </tr>
                </tbody>
                <tfoot class="table-light">
                  <tr>
                    <td colspan="6" class="text-end fw-semibold pe-3 text-muted">Day total</td>
                    <td class="text-center pe-3">
                      <span
                        class="badge font-monospace px-2 py-1"
                        :class="dayNetDelta(day) >= 0 ? 'bg-success' : 'bg-danger'"
                      >
                        {{ dayNetDelta(day) >= 0 ? '+' : '' }}{{ dayNetDelta(day).toFixed(2) }}
                      </span>
                    </td>
                  </tr>
                </tfoot>
              </table>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script>
import { tournamentAPI } from '@/services/api'

export default {
  name: 'PlayerHistoryView',

  data () {
    return {
      tournamentId: null,
      tournamentPlayerId: null,
      history: null,
      loading: true,
      error: null,
      expandedIdx: 0 // first day open by default
    }
  },

  computed: {
    fromRankings () {
      return this.$route.query.from === 'rankings'
    }
  },

  async mounted () {
    this.tournamentId = this.$route.params.id
    this.tournamentPlayerId = this.$route.params.tournamentPlayerId
    await this.loadHistory()
  },

  methods: {
    async loadHistory () {
      this.loading = true
      this.error = null
      try {
        const res = await tournamentAPI.getPlayerHistory(this.tournamentId, this.tournamentPlayerId)
        if (res.data.success) {
          this.history = res.data
        } else {
          this.error = res.data.message || 'Failed to load history'
        }
      } catch (err) {
        this.error = err.response?.data?.message || 'Error loading history'
        console.error('Error loading history:', err)
      } finally {
        this.loading = false
      }
    },

    toggle (idx) {
      this.expandedIdx = this.expandedIdx === idx ? null : idx
    },

    dayNetDelta (day) {
      return (day.matches || []).reduce((sum, m) => sum + Number(m.scoreDelta), 0)
    },

    goBack () {
      if (this.fromRankings) {
        this.$router.push(`/tournaments/${this.tournamentId}/rankings`)
      } else {
        this.$router.push(`/tournaments/${this.tournamentId}/player-view`)
      }
    }
  }
}
</script>

<style scoped>
.card {
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
}
.score-display {
  font-family: 'Courier New', monospace;
  font-size: 1rem;
  font-weight: 600;
}
.font-monospace {
  font-family: 'Courier New', monospace;
}
</style>
