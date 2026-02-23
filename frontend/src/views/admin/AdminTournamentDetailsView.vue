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
                <label class="text-muted small">Status</label>
                <p class="mb-0">
                  <span v-if="tournament.enabled" class="badge bg-success">Enabled</span>
                  <span v-else class="badge bg-secondary">Disabled</span>
                </p>
              </div>
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
        <div class="card">
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
      </div>

      <!-- Right Column: Quick Info -->
      <div class="col-lg-4">
        <div class="card">
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
            <div class="mt-3">
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
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { tournamentAPI } from '@/services/api'

export default {
  name: 'AdminTournamentDetailsView',
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
      availableAdmins: [],
      loading: true,
      error: null,
      successMessage: null,
      toggling: false,
      showAddAdminModal: false,
      selectedAdminId: '',
      addingAdmin: false,
      addAdminError: null,
      removingAdminId: null
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
