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
      loading: true,
      error: null,
      successMessage: null,
      toggling: false
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
</style>
