<template>
  <div class="container-fluid mt-4">
    <!-- Back Button -->
    <div class="row mb-4">
      <div class="col-12">
        <button class="btn btn-secondary" @click="$router.push('/admin/tournaments/' + tournamentId)">
          <i class="bi bi-arrow-left me-2"></i>Back to Tournament
        </button>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="row">
      <div class="col-12">
        <div class="alert alert-info">
          <i class="bi bi-hourglass-split me-2"></i>Loading game day...
        </div>
      </div>
    </div>

    <!-- Error -->
    <div v-if="error && !loading" class="row">
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

    <div v-if="!loading && gameDay">

      <!-- Header Card -->
      <div class="card mb-4">
        <div class="card-header bg-light d-flex justify-content-between align-items-center flex-wrap gap-2">
          <div class="d-flex align-items-center gap-3">
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

          <!-- Actions: PENDING -->
          <div v-if="gameDay.status === 'PENDING'" class="d-flex gap-2">
            <button class="btn btn-success" @click="startDay" :disabled="actionInProgress">
              <span v-if="actionInProgress === 'start'">
                <i class="bi bi-hourglass-split me-1"></i>Starting...
              </span>
              <span v-else>
                <i class="bi bi-play-fill me-1"></i>Start Day
              </span>
            </button>
            <button class="btn btn-danger" @click="discardDay" :disabled="actionInProgress">
              <span v-if="actionInProgress === 'discard'">
                <i class="bi bi-hourglass-split me-1"></i>Discarding...
              </span>
              <span v-else>
                <i class="bi bi-trash me-1"></i>Discard
              </span>
            </button>
          </div>

          <!-- Actions: ONGOING -->
          <div v-if="gameDay.status === 'ONGOING'" class="d-flex gap-2">
            <button class="btn btn-warning" @click="cancelDay" :disabled="actionInProgress">
              <span v-if="actionInProgress === 'cancel'">
                <i class="bi bi-hourglass-split me-1"></i>Cancelling...
              </span>
              <span v-else>
                <i class="bi bi-x-circle me-1"></i>Cancel Day
              </span>
            </button>
          </div>
        </div>

        <!-- COMPLETED banner -->
        <div v-if="gameDay.status === 'COMPLETED'" class="card-body">
          <div class="alert alert-secondary mb-0">
            <i class="bi bi-check-all me-2"></i>This game day has been completed. Scores are read-only.
          </div>
        </div>
      </div>

      <!-- Group Tabs -->
      <div v-if="gameDay.groups && gameDay.groups.length">

        <!-- Tab nav -->
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

        <!-- Tab panels -->
        <div class="tab-content border border-top-0 rounded-bottom bg-white">
          <div
            v-for="(group, idx) in gameDay.groups"
            :key="group.id"
            class="tab-pane p-3"
            :class="{ 'show active d-block': activeTab === idx }"
          >
            <!-- Players summary -->
            <div class="mb-3">
              <h6 class="text-muted mb-2"><i class="bi bi-person-lines-fill me-1"></i>Players</h6>
              <div class="d-flex flex-wrap gap-2">
                <span
                  v-for="(player, pi) in group.players"
                  :key="player.tournamentPlayerId"
                  class="badge bg-primary-subtle text-primary-emphasis border border-primary-subtle px-2 py-1"
                >
                  <strong>{{ positionLabel(pi) }}</strong> — {{ player.firstName }} {{ player.lastName }}
                  <span class="text-muted ms-1">({{ player.rankScore }})</span>
                </span>
              </div>
            </div>

            <hr class="my-2">

            <!-- Matches table -->
            <h6 class="text-muted mb-2"><i class="bi bi-trophy me-1"></i>Matches</h6>
            <div class="table-responsive">
              <table class="table table-bordered table-hover align-middle mb-0">
                <thead class="table-light">
                  <tr>
                    <th class="text-center" style="width:50px">#</th>
                    <th>Team 1</th>
                    <th class="text-center" style="width:80px">T1 Score</th>
                    <th class="text-center" style="width:30px"></th>
                    <th class="text-center" style="width:80px">T2 Score</th>
                    <th>Team 2</th>
                    <th class="text-center" style="width:80px" v-if="gameDay.status === 'ONGOING'">Save</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="match in group.matches" :key="match.id">
                    <td class="text-center text-muted">{{ match.matchOrder }}</td>
                    <td>
                      <div>{{ match.team1Player1Name }}</div>
                      <div>{{ match.team1Player2Name }}</div>
                    </td>
                    <td class="text-center">
                      <input
                        v-if="gameDay.status === 'ONGOING'"
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
                    <td class="text-center">
                      <input
                        v-if="gameDay.status === 'ONGOING'"
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
                      <div>{{ match.team2Player1Name }}</div>
                      <div>{{ match.team2Player2Name }}</div>
                    </td>
                    <td class="text-center" v-if="gameDay.status === 'ONGOING'">
                      <button
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
import { tournamentAPI } from '@/services/api'

export default {
  name: 'LeagueDayView',

  data () {
    return {
      tournamentId: null,
      dayId: null,
      gameDay: null,
      loading: true,
      error: null,
      successMessage: null,
      actionInProgress: null,
      activeTab: 0,
      // keyed by matchId → { team1Score, team2Score }
      scoreInputs: {},
      savingMatch: null
    }
  },

  async mounted () {
    this.tournamentId = this.$route.params.id
    this.dayId = this.$route.params.dayId
    await this.loadGameDay()
  },

  methods: {
    async loadGameDay () {
      this.loading = true
      this.error = null
      try {
        const res = await tournamentAPI.getGameDay(this.tournamentId, this.dayId)
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
      if (this.gameDay && this.gameDay.groups) {
        for (const group of this.gameDay.groups) {
          for (const match of group.matches || []) {
            inputs[match.id] = {
              team1Score: match.team1Score != null ? match.team1Score : '',
              team2Score: match.team2Score != null ? match.team2Score : ''
            }
          }
        }
      }
      this.scoreInputs = inputs
    },

    positionLabel (index) {
      return String.fromCharCode(65 + index) // A, B, C, D, E
    },

    async startDay () {
      if (!confirm('Start this game day? This will mark it as ONGOING and cannot be undone.')) return
      this.actionInProgress = 'start'
      this.error = null
      try {
        const res = await tournamentAPI.startGameDay(this.tournamentId, this.dayId)
        if (res.data.success) {
          this.gameDay = res.data.gameDay
          this.initScoreInputs()
          this.successMessage = 'Game day started successfully!'
        } else {
          this.error = res.data.message || 'Failed to start game day'
        }
      } catch (err) {
        this.error = err.response?.data?.message || 'Error starting game day'
        console.error(err)
      } finally {
        this.actionInProgress = null
      }
    },

    async discardDay () {
      if (!confirm('Discard this game day? This will permanently delete it.')) return
      this.actionInProgress = 'discard'
      this.error = null
      try {
        const res = await tournamentAPI.discardGameDay(this.tournamentId, this.dayId)
        if (res.data.success) {
          this.$router.push(`/admin/tournaments/${this.tournamentId}`)
        } else {
          this.error = res.data.message || 'Failed to discard game day'
        }
      } catch (err) {
        this.error = err.response?.data?.message || 'Error discarding game day'
        console.error(err)
      } finally {
        this.actionInProgress = null
      }
    },

    async cancelDay () {
      if (!confirm('Cancel this ongoing game day? All recorded scores will be deleted and this cannot be undone.')) return
      this.actionInProgress = 'cancel'
      this.error = null
      try {
        const res = await tournamentAPI.cancelGameDay(this.tournamentId, this.dayId)
        if (res.data.success) {
          this.$router.push(`/admin/tournaments/${this.tournamentId}`)
        } else {
          this.error = res.data.message || 'Failed to cancel game day'
        }
      } catch (err) {
        this.error = err.response?.data?.message || 'Error cancelling game day'
        console.error(err)
      } finally {
        this.actionInProgress = null
      }
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
        const res = await tournamentAPI.submitMatchScore(
          this.tournamentId,
          this.dayId,
          group.id,
          match.id,
          { team1Score: input.team1Score, team2Score: input.team2Score }
        )
        if (res.data.success) {
          this.gameDay = res.data.gameDay
          this.initScoreInputs()
          this.successMessage = `Match ${match.matchOrder} score saved.`
        } else {
          this.error = res.data.message || 'Failed to save score'
        }
      } catch (err) {
        this.error = err.response?.data?.message || 'Error saving score'
        console.error(err)
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
