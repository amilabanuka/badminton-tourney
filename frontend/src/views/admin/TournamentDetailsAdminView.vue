<template>
  <div class="container-fluid mt-4">
    <!-- Back Button -->
    <div class="row mb-4">
      <div class="col-12">
        <button class="btn btn-secondary" @click="goBack">
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
          <i class="bi bi-exclamation-triangle me-2"></i>
          {{ error }}
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
            <div class="d-flex gap-2">
              <button
                v-if="tournament.type === 'LEAGUE' && tournament.enabled"
                class="btn btn-sm btn-primary"
                @click="$router.push('/admin/tournaments/' + tournament.id + '/game-days/new')"
                title="Start a new league day"
              >
                <i class="bi bi-calendar-plus me-1"></i>New League Day
              </button>
              <button
                v-if="!isTournyAdmin"
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
          </div>
          <div class="card-body">
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="text-muted small">Tournament Name</label>
                <p class="mb-0">{{ tournament.name }}</p>
              </div>
              <div class="col-md-6">
                <label class="text-muted small">Owner</label>
                <p class="mb-0">{{ getOwnerName() }}</p>
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
              <div class="col-md-6">
                <label class="text-muted small">Status</label>
                <p class="mb-0">
                  <span v-if="tournament.enabled" class="badge bg-success">Enabled</span>
                  <span v-else class="badge bg-secondary">Disabled</span>
                </p>
              </div>
            </div>
            <div class="row">
              <div class="col-md-6">
                <label class="text-muted small">Created</label>
                <p class="mb-0">{{ formatDate(tournament.createdAt) }}</p>
              </div>
            </div>
            <div class="row">
              <div class="col-md-6">
                <label class="text-muted small">Total Admins</label>
                <p class="mb-0">{{ tournament.adminIds?.length || 0 }}</p>
              </div>
              <div class="col-md-6">
                <label class="text-muted small">Total Players</label>
                <p class="mb-0">{{ tournament.playerIds?.length || 0 }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Tournament Admins Section -->
        <div class="card mb-4">
          <div class="card-header bg-light d-flex justify-content-between align-items-center">
            <h5 class="mb-0">Tournament Administrators</h5>
            <button v-if="!isTournyAdmin" class="btn btn-sm btn-success" @click="showAddAdminModal = true">
              <i class="bi bi-plus-circle me-1"></i>Add Admin
            </button>
          </div>
          <div class="card-body">
            <!-- Admins Table -->
            <div v-if="tournamentAdmins.length > 0" class="table-responsive">
              <table class="table table-sm">
                <thead class="table-light">
                  <tr>
                    <th>Admin Name</th>
                    <th>Email</th>
                    <th v-if="!isTournyAdmin">Action</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="admin in tournamentAdmins" :key="admin.id">
                    <td>{{ admin.firstName }} {{ admin.lastName }}</td>
                    <td>{{ admin.email }}</td>
                    <td v-if="!isTournyAdmin">
                      <button
                        class="btn btn-sm btn-danger"
                        @click="removeAdmin(admin.id)"
                        :disabled="removingAdminId === admin.id"
                      >
                        <span v-if="removingAdminId === admin.id">
                          <i class="bi bi-hourglass-split"></i>
                        </span>
                        <span v-else>
                          <i class="bi bi-trash"></i>
                        </span>
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div v-else class="alert alert-info mb-0">
              <i class="bi bi-inbox me-2"></i>No admins assigned yet
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
              <label class="text-muted small">Last Updated</label>
              <p class="mb-0">{{ formatDate(tournament.updatedAt) }}</p>
            </div>
            <div class="mb-3">
              <label class="text-muted small">Status</label>
              <p class="mb-0">
                <span v-if="tournament.enabled" class="badge bg-success">Active</span>
                <span v-else class="badge bg-warning text-dark">Inactive</span>
              </p>
            </div>
            <div class="mt-3 d-flex flex-column gap-2">
              <button
                v-if="tournament.type === 'LEAGUE' && tournament.enabled"
                class="btn w-100 btn-primary"
                @click="$router.push('/admin/tournaments/' + tournament.id + '/game-days/new')"
              >
                <i class="bi bi-calendar-plus me-2"></i>New League Day
              </button>
              <button
                v-if="!isTournyAdmin"
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

        <!-- Tournament Settings Card -->
        <div v-if="tournament.settings" id="tournament-settings-card" class="card">
          <div class="card-header bg-light d-flex justify-content-between align-items-center">
            <h5 class="mb-0">Tournament Settings</h5>
            <button class="btn btn-sm btn-outline-primary" @click="openEditSettingsModal">
              <i class="bi bi-pencil me-1"></i>Edit
            </button>
          </div>
          <div class="card-body">
            <!-- League settings display -->
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
            <!-- One-off settings display -->
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

    <!-- Add Admin Modal -->
    <div v-if="showAddAdminModal && !isTournyAdmin" class="modal d-block" style="background-color: rgba(0,0,0,0.5);">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Add Tournament Administrator</h5>
            <button type="button" class="btn-close" @click="showAddAdminModal = false"></button>
          </div>
          <div class="modal-body">
            <div v-if="addAdminError" class="alert alert-danger alert-dismissible fade show">
              {{ addAdminError }}
              <button type="button" class="btn-close" @click="addAdminError = null"></button>
            </div>
            <div class="mb-3">
              <label for="adminSelect" class="form-label">Select Administrator</label>
              <select
                id="adminSelect"
                v-model="selectedAdminId"
                class="form-select"
              >
                <option value="">-- Choose an admin --</option>
                <option v-for="admin in availableAdminsToAdd" :key="admin.id" :value="admin.id">
                  {{ admin.firstName }} {{ admin.lastName }} ({{ admin.email }})
                </option>
              </select>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="showAddAdminModal = false">
              Cancel
            </button>
            <button
              type="button"
              class="btn btn-primary"
              @click="confirmAddAdmin"
              :disabled="!selectedAdminId || addingAdmin"
            >
              <span v-if="addingAdmin">
                <i class="bi bi-hourglass-split me-2"></i>Adding...
              </span>
              <span v-else>
                <i class="bi bi-check-circle me-2"></i>Add Admin
              </span>
            </button>
          </div>
        </div>
    </div>
  </div>

  <!-- Edit Settings Modal -->
  <div v-if="showEditSettingsModal" class="modal d-block" style="background-color: rgba(0,0,0,0.5);">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">Edit Tournament Settings</h5>
          <button type="button" class="btn-close" @click="showEditSettingsModal = false"></button>
        </div>
        <div class="modal-body">
          <div v-if="editSettingsError" class="alert alert-danger alert-dismissible fade show">
            {{ editSettingsError }}
            <button type="button" class="btn-close" @click="editSettingsError = null"></button>
          </div>
          <!-- League settings fields -->
          <template v-if="tournament.type === 'LEAGUE'">
            <div class="mb-3">
              <label class="form-label">Ranking Logic</label>
              <p class="form-control-plaintext fw-bold mb-0">Modified ELO</p>
              <div class="form-text">Ranking logic cannot be changed after creation.</div>
            </div>
            <div class="mb-3">
              <label for="editK" class="form-label">K Factor <span class="text-danger">*</span></label>
              <input
                id="editK"
                v-model.number="editSettings.k"
                type="number"
                min="1"
                class="form-control"
                :class="{ 'is-invalid': editSettingsErrors.k }"
              >
              <div v-if="editSettingsErrors.k" class="invalid-feedback d-block">{{ editSettingsErrors.k }}</div>
            </div>
            <div class="mb-0">
              <label for="editAbsenteeDemerit" class="form-label">Absentee Demerit <span class="text-danger">*</span></label>
              <input
                id="editAbsenteeDemerit"
                v-model.number="editSettings.absenteeDemerit"
                type="number"
                min="0"
                class="form-control"
                :class="{ 'is-invalid': editSettingsErrors.absenteeDemerit }"
              >
              <div v-if="editSettingsErrors.absenteeDemerit" class="invalid-feedback d-block">{{ editSettingsErrors.absenteeDemerit }}</div>
            </div>
          </template>
          <!-- One-off settings fields -->
          <template v-else-if="tournament.type === 'ONE_OFF'">
            <div class="mb-3">
              <label for="editNumberOfRounds" class="form-label">Number of Rounds <span class="text-danger">*</span></label>
              <input
                id="editNumberOfRounds"
                v-model.number="editSettings.numberOfRounds"
                type="number"
                min="1"
                class="form-control"
                :class="{ 'is-invalid': editSettingsErrors.numberOfRounds }"
              >
              <div v-if="editSettingsErrors.numberOfRounds" class="invalid-feedback d-block">{{ editSettingsErrors.numberOfRounds }}</div>
            </div>
            <div class="mb-0">
              <label for="editMaxPoints" class="form-label">Max Points Per Game <span class="text-danger">*</span></label>
              <select
                id="editMaxPoints"
                v-model.number="editSettings.maxPoints"
                class="form-select"
                :class="{ 'is-invalid': editSettingsErrors.maxPoints }"
              >
                <option :value="15">15</option>
                <option :value="21">21</option>
              </select>
              <div v-if="editSettingsErrors.maxPoints" class="invalid-feedback d-block">{{ editSettingsErrors.maxPoints }}</div>
            </div>
          </template>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="showEditSettingsModal = false">
            Cancel
          </button>
          <button
            type="button"
            class="btn btn-primary"
            @click="saveSettings"
            :disabled="savingSettings"
          >
            <span v-if="savingSettings"><i class="bi bi-hourglass-split me-2"></i>Saving...</span>
            <span v-else><i class="bi bi-check-circle me-2"></i>Save Settings</span>
          </button>
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
          <button type="button" class="btn-close" @click="showAddPlayerModal = false; newPlayerRankScore = ''; rankScoreError = null"></button>
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
          <div v-if="!loadingAvailablePlayers" class="mb-3">
            <label for="rankScoreInput" class="form-label">
              Initial Rank Score <span class="text-muted small">(optional, defaults to 0.00)</span>
            </label>
            <input
              id="rankScoreInput"
              v-model="newPlayerRankScore"
              type="number"
              step="0.01"
              min="0"
              class="form-control"
              placeholder="0.00"
            />
            <div v-if="rankScoreError" class="text-danger small mt-1">{{ rankScoreError }}</div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="showAddPlayerModal = false; newPlayerRankScore = ''; rankScoreError = null">Cancel</button>
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
  name: 'TournamentDetailsAdminView',
  computed: {
    ...mapGetters('auth', ['isTournyAdmin']),
    availableAdminsToAdd () {
      return this.availableAdmins.filter(
        admin => !this.tournamentAdmins.some(ta => ta.id === admin.id)
      )
    }
  },
  data () {
    return {
      tournament: null,
      tournamentAdmins: [],
      tournamentPlayers: [],
      availableAdmins: [],
      availablePlayers: [],
      loading: true,
      error: null,
      successMessage: null,
      toggling: false,
      showAddAdminModal: false,
      selectedAdminId: '',
      addingAdmin: false,
      addAdminError: null,
      removingAdminId: null,
      showAddPlayerModal: false,
      selectedPlayerId: '',
      newPlayerRankScore: '',
      rankScoreError: null,
      addingPlayer: false,
      addPlayerError: null,
      loadingAvailablePlayers: false,
      togglingPlayerId: null,
      showEditSettingsModal: false,
      editSettings: { k: null, absenteeDemerit: null, numberOfRounds: null, maxPoints: null },
      editSettingsErrors: {},
      editSettingsError: null,
      savingSettings: false
    }
  },
  mounted () {
    this.loadTournamentDetails()
  },
  methods: {
    async loadTournamentDetails () {
      this.loading = true
      this.error = null
      try {
        const tournamentId = this.$route.params.id

        // TOURNY_ADMIN does not have access to the available-admins endpoint
        if (this.isTournyAdmin) {
          const tournamentRes = await tournamentAPI.getTournamentById(tournamentId)
          if (tournamentRes.data.success) {
            this.tournament = tournamentRes.data.tournament
            await this.loadTournamentAdmins()
          } else {
            this.error = tournamentRes.data.message || 'Failed to load tournament'
          }
        } else {
          const [tournamentRes, adminsRes] = await Promise.all([
            tournamentAPI.getTournamentById(tournamentId),
            tournamentAPI.getAvailableAdmins()
          ])

          if (tournamentRes.data.success) {
            this.tournament = tournamentRes.data.tournament
            await this.loadTournamentAdmins()
          } else {
            this.error = tournamentRes.data.message || 'Failed to load tournament'
          }

          if (adminsRes.data.success) {
            this.availableAdmins = adminsRes.data.users || []
          }
        }
      } catch (err) {
        if (err.response?.status === 403) {
          this.error = 'Access denied: You do not have permission to view this tournament.'
        } else {
          this.error = err.response?.data?.message || 'Error loading tournament details'
        }
        console.error('Error loading tournament:', err)
      } finally {
        this.loading = false
      }
    },

    async loadTournamentAdmins () {
      // Admin details (name, email) are now embedded in the tournament response
      // via the admins array — no separate API call needed for any role
      if (this.tournament.admins && this.tournament.admins.length > 0) {
        this.tournamentAdmins = this.tournament.admins
      } else {
        this.tournamentAdmins = []
      }
      // Load players from embedded players list
      this.tournamentPlayers = this.tournament.players || []
    },

    async removeAdmin (adminId) {
      if (!confirm('Are you sure you want to remove this administrator?')) {
        return
      }

      this.removingAdminId = adminId
      try {
        const response = await tournamentAPI.removeTournamentAdmin(this.tournament.id, adminId)
        if (response.data.success) {
          this.tournamentAdmins = this.tournamentAdmins.filter(admin => admin.id !== adminId)
          this.tournament.adminIds = this.tournament.adminIds.filter(id => id !== adminId)
        } else {
          this.error = response.data.message || 'Failed to remove administrator'
        }
      } catch (err) {
        this.error = err.response?.data?.message || 'Error removing administrator'
        console.error('Error removing admin:', err)
      } finally {
        this.removingAdminId = null
      }
    },

    async confirmAddAdmin () {
      if (!this.selectedAdminId) {
        this.addAdminError = 'Please select an administrator'
        return
      }

      this.addingAdmin = true
      this.addAdminError = null
      try {
        const response = await tournamentAPI.addTournamentAdmin(this.tournament.id, {
          userId: parseInt(this.selectedAdminId)
        })

        if (response.data.success) {
          // Add the new admin to the list
          const newAdmin = this.availableAdmins.find(a => a.id === parseInt(this.selectedAdminId))
          if (newAdmin) {
            this.tournamentAdmins.push(newAdmin)
            this.tournament.adminIds.push(newAdmin.id)
          }
          this.showAddAdminModal = false
          this.selectedAdminId = ''
        } else {
          this.addAdminError = response.data.message || 'Failed to add administrator'
        }
      } catch (err) {
        this.addAdminError = err.response?.data?.message || 'Error adding administrator'
        console.error('Error adding admin:', err)
      } finally {
        this.addingAdmin = false
      }
    },

    getOwnerName () {
      if (!this.tournament) return '-'
      // First try availableAdmins (populated for ADMIN users)
      if (this.availableAdmins && this.availableAdmins.length > 0) {
        const owner = this.availableAdmins.find(a => a.id === this.tournament.ownerId)
        if (owner) return `${owner.firstName} ${owner.lastName}`
      }
      // Fall back to the embedded admins list from the tournament response (works for all roles)
      if (this.tournament.admins && this.tournament.admins.length > 0) {
        const owner = this.tournament.admins.find(a => a.id === this.tournament.ownerId)
        if (owner) return `${owner.firstName} ${owner.lastName}`
      }
      return `ID: ${this.tournament.ownerId}`
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

    async openAddPlayerModal () {
      this.showAddPlayerModal = true
      this.selectedPlayerId = ''
      this.newPlayerRankScore = ''
      this.rankScoreError = null
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

      this.rankScoreError = null
      let rankScore = null
      if (this.newPlayerRankScore !== '' && this.newPlayerRankScore !== null) {
        const parsed = parseFloat(this.newPlayerRankScore)
        if (isNaN(parsed) || parsed < 0) {
          this.rankScoreError = 'Rank score must be a non-negative number'
          return
        }
        rankScore = parseFloat(parsed.toFixed(2))
      }

      this.addingPlayer = true
      this.addPlayerError = null
      try {
        const payload = { userId: parseInt(this.selectedPlayerId) }
        if (rankScore !== null) {
          payload.rankScore = rankScore
        }
        const response = await tournamentAPI.addTournamentPlayer(this.tournament.id, payload)

        if (response.data.success) {
          // Reload tournament to get the updated player list with status
          const tournamentRes = await tournamentAPI.getTournamentById(this.tournament.id)
          if (tournamentRes.data.success) {
            this.tournament = tournamentRes.data.tournament
            this.tournamentPlayers = this.tournament.players || []
          }
          this.showAddPlayerModal = false
          this.selectedPlayerId = ''
          this.newPlayerRankScore = ''
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

    openEditSettingsModal () {
      const s = this.tournament.settings || {}
      this.editSettings = {
        k: s.k ?? null,
        absenteeDemerit: s.absenteeDemerit ?? null,
        numberOfRounds: s.numberOfRounds ?? null,
        maxPoints: s.maxPoints ?? null
      }
      this.editSettingsErrors = {}
      this.editSettingsError = null
      this.showEditSettingsModal = true
    },

    async saveSettings () {
      this.editSettingsErrors = {}
      this.editSettingsError = null
      let isValid = true

      if (this.tournament.type === 'LEAGUE') {
        if (!this.editSettings.k || this.editSettings.k <= 0) {
          this.editSettingsErrors.k = 'K factor must be a positive integer'
          isValid = false
        }
        if (this.editSettings.absenteeDemerit === null || this.editSettings.absenteeDemerit < 0) {
          this.editSettingsErrors.absenteeDemerit = 'Absentee demerit must be 0 or greater'
          isValid = false
        }
      } else if (this.tournament.type === 'ONE_OFF') {
        if (!this.editSettings.numberOfRounds || this.editSettings.numberOfRounds <= 0) {
          this.editSettingsErrors.numberOfRounds = 'Number of rounds must be a positive integer'
          isValid = false
        }
        if (this.editSettings.maxPoints !== 15 && this.editSettings.maxPoints !== 21) {
          this.editSettingsErrors.maxPoints = 'Max points must be 15 or 21'
          isValid = false
        }
      }

      if (!isValid) return

      this.savingSettings = true
      try {
        const payload = this.tournament.type === 'LEAGUE'
          ? { k: this.editSettings.k, absenteeDemerit: this.editSettings.absenteeDemerit }
          : { numberOfRounds: this.editSettings.numberOfRounds, maxPoints: this.editSettings.maxPoints }

        const response = await tournamentAPI.updateTournamentSettings(this.tournament.id, payload)
        if (response.data.success) {
          this.tournament = response.data.tournament
          this.showEditSettingsModal = false
          this.successMessage = 'Tournament settings updated successfully.'
        } else {
          this.editSettingsError = response.data.message || 'Failed to update settings'
        }
      } catch (err) {
        this.editSettingsError = err.response?.data?.message || 'Error updating settings'
        console.error('Error updating settings:', err)
      } finally {
        this.savingSettings = false
      }
    },

    goBack () {
      this.$router.back()
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
