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
          <i class="bi bi-hourglass-split me-2"></i>Loading players...
        </div>
      </div>
    </div>

    <!-- Error -->
    <div v-if="error" class="row">
      <div class="col-12">
        <div class="alert alert-danger alert-dismissible fade show">
          <i class="bi bi-exclamation-triangle me-2"></i>{{ error }}
          <button type="button" class="btn-close" @click="error = null"></button>
        </div>
      </div>
    </div>

    <div v-if="!loading" class="row justify-content-center">
      <div class="col-lg-8">
        <div class="card">
          <div class="card-header bg-light">
            <h4 class="mb-0"><i class="bi bi-calendar-plus me-2"></i>New League Day</h4>
          </div>
          <div class="card-body">

            <!-- Date Picker -->
            <div class="mb-4">
              <label for="gameDate" class="form-label fw-semibold">Game Date</label>
              <input
                id="gameDate"
                v-model="gameDate"
                type="date"
                class="form-control"
                style="max-width: 220px"
                required
              />
            </div>

            <!-- Validation Warning -->
            <div v-if="validationWarning" class="alert alert-warning d-flex align-items-center mb-3">
              <i class="bi bi-exclamation-triangle-fill me-2 fs-5"></i>
              <div>{{ validationWarning }}</div>
            </div>

            <!-- Player Selection -->
            <div class="mb-3 d-flex justify-content-between align-items-center">
              <label class="form-label fw-semibold mb-0">
                Select Players
                <span class="badge ms-2" :class="countBadgeClass">{{ selectedIds.length }} selected</span>
              </label>
              <div class="d-flex gap-2">
                <button class="btn btn-sm btn-outline-secondary" @click="selectAll">Select All</button>
                <button class="btn btn-sm btn-outline-secondary" @click="deselectAll">Deselect All</button>
              </div>
            </div>

            <div v-if="eligiblePlayers.length === 0" class="alert alert-info">
              <i class="bi bi-inbox me-2"></i>No enabled or active players found in this tournament.
            </div>

            <div v-else class="border rounded" style="max-height: 420px; overflow-y: auto;">
              <table class="table table-sm table-hover mb-0">
                <thead class="table-light sticky-top">
                  <tr>
                    <th style="width: 40px">
                      <input
                        type="checkbox"
                        class="form-check-input"
                        :checked="allSelected"
                        :indeterminate.prop="someSelected"
                        @change="toggleAll"
                      />
                    </th>
                    <th>Player</th>
                    <th>Status</th>
                    <th class="text-end">Rank Score</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="player in eligiblePlayers"
                    :key="player.tournamentPlayerId"
                    @click="togglePlayer(player.tournamentPlayerId)"
                    style="cursor: pointer"
                    :class="{ 'table-primary': selectedIds.includes(player.tournamentPlayerId) }"
                  >
                    <td>
                      <input
                        type="checkbox"
                        class="form-check-input"
                        :checked="selectedIds.includes(player.tournamentPlayerId)"
                        @click.stop
                        @change="togglePlayer(player.tournamentPlayerId)"
                      />
                    </td>
                    <td>{{ player.firstName }} {{ player.lastName }}</td>
                    <td>
                      <span v-if="player.status === 'ACTIVE'" class="badge bg-primary">ACTIVE</span>
                      <span v-else class="badge bg-success">ENABLED</span>
                    </td>
                    <td class="text-end font-monospace">{{ player.rankScore }}</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- Actions -->
            <div class="mt-4 d-flex gap-2">
              <button
                class="btn btn-primary"
                :disabled="!!validationWarning || !gameDate || submitting || selectedIds.length === 0"
                @click="submitGameDay"
              >
                <span v-if="submitting">
                  <i class="bi bi-hourglass-split me-2"></i>Creating...
                </span>
                <span v-else>
                  <i class="bi bi-check-circle me-2"></i>Create Game Day
                </span>
              </button>
              <button class="btn btn-secondary" @click="$router.push('/admin/tournaments/' + tournamentId)">
                Cancel
              </button>
            </div>

          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { tournamentAPI } from '@/services/api'

/** Player counts that cannot be split into groups of 4 or 5 */
const INVALID_COUNTS = new Set([1, 2, 3, 6, 7, 11])
const MAX_PLAYERS = 32

export default {
  name: 'CreateLeagueDayView',

  data () {
    return {
      tournamentId: null,
      gameDate: new Date().toISOString().split('T')[0],
      eligiblePlayers: [],
      selectedIds: [],
      loading: true,
      error: null,
      submitting: false
    }
  },

  computed: {
    allSelected () {
      return this.eligiblePlayers.length > 0 && this.selectedIds.length === this.eligiblePlayers.length
    },
    someSelected () {
      return this.selectedIds.length > 0 && this.selectedIds.length < this.eligiblePlayers.length
    },
    countBadgeClass () {
      if (this.validationWarning) return 'bg-warning text-dark'
      if (this.selectedIds.length >= 4) return 'bg-success'
      return 'bg-secondary'
    },
    validationWarning () {
      const n = this.selectedIds.length
      if (n === 0) return null
      if (n > MAX_PLAYERS) return `Too many players selected (${n}). Maximum is ${MAX_PLAYERS}.`
      if (n < 4) return `At least 4 players are required to create a game day.`
      if (INVALID_COUNTS.has(n)) return `${n} players cannot be evenly split into groups of 4 or 5. Try selecting a different number. (Invalid counts: 6, 7, 11)`
      return null
    }
  },

  async mounted () {
    this.tournamentId = this.$route.params.id
    await this.loadPlayers()
  },

  methods: {
    async loadPlayers () {
      this.loading = true
      this.error = null
      try {
        const res = await tournamentAPI.getTournamentById(this.tournamentId)
        if (res.data.success) {
          const players = res.data.tournament.players || []
          // Only show ENABLED or ACTIVE players, sorted by rankScore descending
          // PlayerDto exposes: id (userId), tournamentPlayerId, firstName, lastName, rankScore, status
          this.eligiblePlayers = players
            .filter(p => p.status === 'ENABLED' || p.status === 'ACTIVE')
            .sort((a, b) => (b.rankScore || 0) - (a.rankScore || 0))
            .map(p => ({
              tournamentPlayerId: p.tournamentPlayerId,
              userId: p.id,
              firstName: p.firstName,
              lastName: p.lastName,
              rankScore: p.rankScore,
              status: p.status
            }))
        } else {
          this.error = res.data.message || 'Failed to load tournament players'
        }
      } catch (err) {
        this.error = err.response?.data?.message || 'Error loading players'
        console.error('Error loading players:', err)
      } finally {
        this.loading = false
      }
    },

    togglePlayer (tpId) {
      const idx = this.selectedIds.indexOf(tpId)
      if (idx === -1) {
        this.selectedIds.push(tpId)
      } else {
        this.selectedIds.splice(idx, 1)
      }
    },

    selectAll () {
      this.selectedIds = this.eligiblePlayers.map(p => p.tournamentPlayerId)
    },

    deselectAll () {
      this.selectedIds = []
    },

    toggleAll (event) {
      if (event.target.checked) {
        this.selectAll()
      } else {
        this.deselectAll()
      }
    },

    async submitGameDay () {
      if (this.validationWarning || !this.gameDate || this.selectedIds.length === 0) return

      this.submitting = true
      this.error = null
      try {
        const response = await tournamentAPI.createGameDay(this.tournamentId, {
          gameDate: this.gameDate,
          playerIds: this.selectedIds
        })
        if (response.data.success) {
          const dayId = response.data.gameDay.id
          this.$router.push(`/admin/tournaments/${this.tournamentId}/game-days/${dayId}`)
        } else {
          this.error = response.data.message || 'Failed to create game day'
        }
      } catch (err) {
        this.error = err.response?.data?.message || 'Error creating game day'
        console.error('Error creating game day:', err)
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.card {
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
}
.table-sm {
  font-size: 0.9rem;
}
.font-monospace {
  font-family: 'Courier New', monospace;
  font-size: 0.85rem;
}
</style>


