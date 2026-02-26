<template>
  <div class="container-fluid mt-4">
    <!-- Back Button -->
    <div class="row mb-4">
      <div class="col-12">
        <button class="btn btn-secondary" @click="$router.push('/tournaments')">
          <i class="bi bi-arrow-left me-2"></i>Back to Tournaments
        </button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="row">
      <div class="col-12">
        <div class="alert alert-info">
          <i class="bi bi-hourglass-split me-2"></i>Loading tournament details...
        </div>
      </div>
    </div>

    <!-- Error State -->
    <div v-if="error && !loading" class="row">
      <div class="col-12">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
          <i class="bi bi-exclamation-triangle me-2"></i>{{ error }}
          <button type="button" class="btn-close" @click="error = null"></button>
        </div>
      </div>
    </div>

    <!-- Success Message -->
    <div v-if="successMessage" class="row">
      <div class="col-12">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
          <i class="bi bi-check-circle me-2"></i>{{ successMessage }}
          <button type="button" class="btn-close" @click="successMessage = null"></button>
        </div>
      </div>
    </div>

    <!-- Tournament Details -->
    <div v-if="!loading && tournament" class="row">
      <!-- Left Column: Tournament Info -->
      <div class="col-lg-8">
        <div class="card mb-4">
          <div class="card-header bg-light d-flex justify-content-between align-items-center">
            <h3 class="mb-0">{{ tournament.name }}</h3>
            <button
              v-if="isAdmin"
              class="btn btn-sm"
              :class="tournament.enabled ? 'btn-warning' : 'btn-success'"
              @click="toggleTournament"
              :disabled="toggling"
              :title="tournament.enabled ? 'Disable Tournament' : 'Enable Tournament'"
            >
              <span v-if="toggling">
                <i class="bi bi-hourglass-split me-1"></i>Updating...
              </span>
              <span v-else-if="tournament.enabled">
                <i class="bi bi-toggle-on me-1"></i>Disable
              </span>
              <span v-else>
                <i class="bi bi-toggle-off me-1"></i>Enable
              </span>
            </button>
          </div>
          <div class="card-body">
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="text-muted small">Tournament Name</label>
                <p class="mb-0">{{ tournament.name }}</p>
              </div>
              <div class="col-md-6">
                <label class="text-muted small">Status</label>
                <p class="mb-0">
                  <span v-if="tournament.enabled" class="badge bg-success">Enabled</span>
                  <span v-else class="badge bg-secondary">Disabled</span>
                </p>
              </div>
            </div>
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="text-muted small">Type</label>
                <p class="mb-0">
                  <span v-if="tournament.type === 'LEAGUE'" class="badge bg-primary">League</span>
                  <span v-else-if="tournament.type === 'ONE_OFF'" class="badge bg-info text-dark">One-off</span>
                  <span v-else class="badge bg-secondary">Unknown</span>
                </p>
              </div>
            </div>
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="text-muted small">Total Admins</label>
                <p class="mb-0">{{ tournament.adminIds?.length || 0 }}</p>
              </div>
              <div class="col-md-6">
                <label class="text-muted small">Total Players</label>
                <p class="mb-0">{{ tournament.playerIds?.length || 0 }}</p>
              </div>
            </div>
            <div class="row">
              <div class="col-md-6">
                <label class="text-muted small">Created</label>
                <p class="mb-0">{{ formatDate(tournament.createdAt) }}</p>
              </div>
              <div class="col-md-6">
                <label class="text-muted small">Last Updated</label>
                <p class="mb-0">{{ formatDate(tournament.updatedAt) }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Tournament Players Section -->
        <div class="card">
          <div class="card-header bg-light d-flex justify-content-between align-items-center">
            <h5 class="mb-0">Tournament Players</h5>
            <button class="btn btn-sm btn-success" @click="openAddPlayerModal">
              <i class="bi bi-plus-circle me-1"></i>Add Player
            </button>
          </div>
          <div class="card-body">
            <div v-if="tournamentPlayers.length > 0" class="table-responsive">
              <table class="table table-sm">
                <thead class="table-light">
                  <tr>
                    <th>Player Name</th>
                    <th>Email</th>
                    <th>Status</th>
                    <th>Status Changed</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="player in tournamentPlayers" :key="player.id">
                    <td>{{ player.firstName }} {{ player.lastName }}</td>
                    <td>{{ player.email }}</td>
                    <td>
                      <span v-if="player.status === 'ENABLED'" class="badge bg-success">ENABLED</span>
                      <span v-else-if="player.status === 'DISABLED'" class="badge bg-secondary">DISABLED</span>
                      <span v-else-if="player.status === 'ACTIVE'" class="badge bg-primary">ACTIVE</span>
                    </td>
                    <td>{{ formatDate(player.statusChangedAt) }}</td>
                    <td>
                      <button
                        v-if="player.status === 'ENABLED'"
                        class="btn btn-sm btn-warning"
                        @click="disablePlayer(player)"
                        :disabled="togglingPlayerId === player.id"
                      >
                        <i v-if="togglingPlayerId === player.id" class="bi bi-hourglass-split"></i>
                        <span v-else><i class="bi bi-toggle-on me-1"></i>Disable</span>
                      </button>
                      <button
                        v-else-if="player.status === 'ACTIVE'"
                        class="btn btn-sm btn-warning"
                        @click="disablePlayer(player)"
                        :disabled="togglingPlayerId === player.id"
                      >
                        <i v-if="togglingPlayerId === player.id" class="bi bi-hourglass-split"></i>
                        <span v-else><i class="bi bi-toggle-on me-1"></i>Disable</span>
                      </button>
                      <button
                        v-else-if="player.status === 'DISABLED'"
                        class="btn btn-sm btn-success"
                        @click="enablePlayer(player)"
                        :disabled="togglingPlayerId === player.id"
                      >
                        <i v-if="togglingPlayerId === player.id" class="bi bi-hourglass-split"></i>
                        <span v-else><i class="bi bi-toggle-off me-1"></i>Enable</span>
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div v-else class="alert alert-info mb-0">
              <i class="bi bi-inbox me-2"></i>No players added yet
            </div>
          </div>
        </div>
      </div>

      <!-- Right Column: Quick Info -->
      <div class="col-lg-4">
        <div class="card mb-3">
          <div class="card-header bg-light">
            <h5 class="mb-0">Summary</h5>
          </div>
          <div class="card-body">
            <div class="mb-3">
              <label class="text-muted small">Tournament ID</label>
              <p class="mb-0 font-monospace text-muted">{{ tournament.id }}</p>
            </div>
            <div class="mb-3">
              <label class="text-muted small">Status</label>
              <p class="mb-0">
                <span v-if="tournament.enabled" class="badge bg-success">Active</span>
                <span v-else class="badge bg-warning text-dark">Inactive</span>
              </p>
            </div>
            <div v-if="isAdmin" class="mt-3">
              <button
                class="btn w-100"
                :class="tournament.enabled ? 'btn-warning' : 'btn-success'"
                @click="toggleTournament"
                :disabled="toggling"
              >
                <span v-if="toggling">
                  <i class="bi bi-hourglass-split me-2"></i>Updating...
                </span>
                <span v-else-if="tournament.enabled">
                  <i class="bi bi-toggle-on me-2"></i>Disable Tournament
                </span>
                <span v-else>
                  <i class="bi bi-toggle-off me-2"></i>Enable Tournament
                </span>
              </button>
            </div>
          </div>
        </div>

        <!-- Tournament Settings (read-only) -->
        <div v-if="tournament.settings" class="card">
          <div class="card-header bg-light">
            <h5 class="mb-0">Tournament Settings</h5>
          </div>
          <div class="card-body">
            <template v-if="tournament.type === 'LEAGUE'">
              <div class="mb-2">
                <label class="text-muted small">Ranking Logic</label>
                <p class="mb-0 fw-bold">Modified ELO</p>
              </div>
              <div class="mb-2">
                <label class="text-muted small">K Factor</label>
                <p class="mb-0">{{ tournament.settings.k }}</p>
              </div>
              <div class="mb-0">
                <label class="text-muted small">Absentee Demerit</label>
                <p class="mb-0">{{ tournament.settings.absenteeDemerit }}</p>
              </div>
            </template>
            <template v-else-if="tournament.type === 'ONE_OFF'">
              <div class="mb-2">
                <label class="text-muted small">Number of Rounds</label>
                <p class="mb-0">{{ tournament.settings.numberOfRounds }}</p>
              </div>
              <div class="mb-0">
                <label class="text-muted small">Max Points Per Game</label>
                <p class="mb-0">{{ tournament.settings.maxPoints }}</p>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>

    <!-- Add Player Modal -->
    <div v-if="showAddPlayerModal" class="modal d-block" style="background-color: rgba(0,0,0,0.5);">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Add Tournament Player</h5>
            <button type="button" class="btn-close" @click="showAddPlayerModal = false"></button>
          </div>
          <div class="modal-body">
            <div v-if="addPlayerError" class="alert alert-danger alert-dismissible fade show">
              {{ addPlayerError }}
              <button type="button" class="btn-close" @click="addPlayerError = null"></button>
            </div>
            <div v-if="loadingAvailablePlayers" class="text-center py-2">
              <i class="bi bi-hourglass-split me-2"></i>Loading available players...
            </div>
            <div v-else class="mb-3">
              <label for="playerSelect" class="form-label">Select Player</label>
              <select id="playerSelect" v-model="selectedPlayerId" class="form-select">
                <option value="">-- Choose a player --</option>
                <option v-for="player in availablePlayers" :key="player.id" :value="player.id">
                  {{ player.firstName }} {{ player.lastName }} ({{ player.email }})
                </option>
              </select>
              <div v-if="availablePlayers.length === 0" class="text-muted small mt-1">
                No available players to add.
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="showAddPlayerModal = false">Cancel</button>
            <button
              type="button"
              class="btn btn-primary"
              @click="confirmAddPlayer"
              :disabled="!selectedPlayerId || addingPlayer"
            >
              <span v-if="addingPlayer"><i class="bi bi-hourglass-split me-2"></i>Adding...</span>
              <span v-else><i class="bi bi-check-circle me-2"></i>Add Player</span>
            </button>
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
  name: 'TournamentDetailsView',
  data () {
    return {
      tournament: null,
      tournamentPlayers: [],
      availablePlayers: [],
      loading: true,
      error: null,
      successMessage: null,
      toggling: false,
      showAddPlayerModal: false,
      selectedPlayerId: '',
      addingPlayer: false,
      addPlayerError: null,
      loadingAvailablePlayers: false,
      togglingPlayerId: null
    }
  },
  computed: {
    ...mapGetters('auth', ['isAdmin'])
  },
  mounted () {
    this.loadTournament()
  },
  methods: {
    async loadTournament () {
      this.loading = true
      this.error = null
      try {
        const id = this.$route.params.id
        const response = await tournamentAPI.getTournamentById(id)
        if (response.data.success) {
          this.tournament = response.data.tournament
          this.tournamentPlayers = this.tournament.players || []
        } else {
          this.error = response.data.message || 'Failed to load tournament'
        }
      } catch (err) {
        this.error = err.response?.data?.message || 'Error loading tournament'
        console.error('Error loading tournament:', err)
      } finally {
        this.loading = false
      }
    },

    async openAddPlayerModal () {
      this.showAddPlayerModal = true
      this.selectedPlayerId = ''
      this.addPlayerError = null
      this.loadingAvailablePlayers = true
      try {
        const response = await tournamentAPI.getAvailablePlayers(this.tournament.id)
        if (response.data.success) {
          this.availablePlayers = response.data.users || []
        } else {
          this.addPlayerError = response.data.message || 'Failed to load available players'
        }
      } catch (err) {
        this.addPlayerError = err.response?.data?.message || 'Error loading available players'
        console.error('Error loading available players:', err)
      } finally {
        this.loadingAvailablePlayers = false
      }
    },

    async confirmAddPlayer () {
      if (!this.selectedPlayerId) {
        this.addPlayerError = 'Please select a player'
        return
      }

      this.addingPlayer = true
      this.addPlayerError = null
      try {
        const response = await tournamentAPI.addTournamentPlayer(this.tournament.id, {
          userId: parseInt(this.selectedPlayerId)
        })

        if (response.data.success) {
          const tournamentRes = await tournamentAPI.getTournamentById(this.tournament.id)
          if (tournamentRes.data.success) {
            this.tournament = tournamentRes.data.tournament
            this.tournamentPlayers = this.tournament.players || []
          }
          this.showAddPlayerModal = false
          this.selectedPlayerId = ''
          this.successMessage = 'Player added successfully.'
        } else {
          this.addPlayerError = response.data.message || 'Failed to add player'
        }
      } catch (err) {
        this.addPlayerError = err.response?.data?.message || 'Error adding player'
        console.error('Error adding player:', err)
      } finally {
        this.addingPlayer = false
      }
    },

    async enablePlayer (player) {
      this.togglingPlayerId = player.id
      this.error = null
      try {
        const response = await tournamentAPI.enablePlayer(this.tournament.id, player.id)
        if (response.data.success) {
          const idx = this.tournamentPlayers.findIndex(p => p.id === player.id)
          if (idx !== -1) {
            this.tournamentPlayers[idx] = { ...this.tournamentPlayers[idx], status: 'ENABLED', statusChangedAt: Date.now() }
          }
          this.successMessage = `${player.firstName} ${player.lastName} has been enabled.`
        } else {
          this.error = response.data.message || 'Failed to enable player'
        }
      } catch (err) {
        this.error = err.response?.data?.message || 'Error enabling player'
        console.error('Error enabling player:', err)
      } finally {
        this.togglingPlayerId = null
      }
    },

    async disablePlayer (player) {
      const confirmMsg = player.status === 'ACTIVE'
        ? 'This player is currently ACTIVE. Disabling them may affect ongoing matches. Are you sure?'
        : `Are you sure you want to disable ${player.firstName} ${player.lastName}?`

      if (!confirm(confirmMsg)) return

      this.togglingPlayerId = player.id
      this.error = null
      try {
        const response = await tournamentAPI.disablePlayer(this.tournament.id, player.id)
        if (response.data.success) {
          const idx = this.tournamentPlayers.findIndex(p => p.id === player.id)
          if (idx !== -1) {
            this.tournamentPlayers[idx] = { ...this.tournamentPlayers[idx], status: 'DISABLED', statusChangedAt: Date.now() }
          }
          this.successMessage = `${player.firstName} ${player.lastName} has been disabled.`
        } else {
          this.error = response.data.message || 'Failed to disable player'
        }
      } catch (err) {
        this.error = err.response?.data?.message || 'Error disabling player'
        console.error('Error disabling player:', err)
      } finally {
        this.togglingPlayerId = null
      }
    },

    async toggleTournament () {
      this.toggling = true
      this.successMessage = null
      try {
        const response = await tournamentAPI.toggleTournament(this.tournament.id)
        if (response.data.success) {
          this.tournament.enabled = response.data.tournament.enabled
          const status = this.tournament.enabled ? 'enabled' : 'disabled'
          this.successMessage = `Tournament "${this.tournament.name}" has been ${status}.`
        } else {
          this.error = response.data.message || 'Failed to toggle tournament'
        }
      } catch (err) {
        this.error = err.response?.data?.message || 'Error toggling tournament'
        console.error('Error toggling tournament:', err)
      } finally {
        this.toggling = false
      }
    },

    formatDate (timestamp) {
      if (!timestamp) return '-'
      const date = new Date(timestamp)
      return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
  }
}
</script>

<style scoped>
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1050;
}

.card {
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
}
h3, h5 {
  color: #2c3e50;
}
.font-monospace {
  font-family: 'Courier New', monospace;
  font-size: 0.9rem;
}
.table-sm {
  font-size: 0.9rem;
}
.btn-sm {
  padding: 0.25rem 0.5rem;
}
</style>
