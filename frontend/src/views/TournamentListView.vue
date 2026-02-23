<template>
  <div class="container-fluid mt-4">
    <div class="row mb-4">
      <div class="col-12">
        <div class="d-flex justify-content-between align-items-center">
          <h1 class="mb-0">Tournaments</h1>
          <router-link v-if="isAdmin" to="/admin/tournaments/create" class="btn btn-primary">
            <i class="bi bi-plus-circle me-2"></i>Create New Tournament
          </router-link>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="row">
      <div class="col-12">
        <div class="alert alert-info">
          <i class="bi bi-hourglass-split me-2"></i>Loading tournaments...
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

    <!-- Tournaments Table -->
    <div v-if="!loading && tournaments.length > 0" class="row">
      <div class="col-12">
        <div class="table-responsive">
          <table class="table table-hover">
            <thead class="table-light">
              <tr>
                <th>Tournament Name</th>
                <th>Type</th>
                <th>Status</th>
                <th>Created Date</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="tournament in tournaments" :key="tournament.id">
                <td class="fw-bold">{{ tournament.name }}</td>
                <td>
                  <span v-if="tournament.type === 'LEAGUE'" class="badge bg-primary">League</span>
                  <span v-else-if="tournament.type === 'ONE_OFF'" class="badge bg-info text-dark">One-off</span>
                  <span v-else class="badge bg-secondary">-</span>
                </td>
                <td>
                  <span v-if="tournament.enabled" class="badge bg-success">Enabled</span>
                  <span v-else class="badge bg-secondary">Disabled</span>
                </td>
                <td>{{ formatDate(tournament.createdAt) }}</td>
                <td>
                  <button
                    class="btn btn-sm btn-info me-2"
                    @click="viewDetails(tournament.id)"
                    title="View Details"
                  >
                    <i class="bi bi-eye"></i>
                  </button>
                  <button
                    v-if="isAdmin"
                    class="btn btn-sm me-2"
                    :class="tournament.enabled ? 'btn-warning' : 'btn-success'"
                    @click="toggleTournament(tournament)"
                    :disabled="togglingId === tournament.id"
                    :title="tournament.enabled ? 'Disable Tournament' : 'Enable Tournament'"
                  >
                    <span v-if="togglingId === tournament.id">
                      <i class="bi bi-hourglass-split"></i>
                    </span>
                    <span v-else>
                      <i :class="tournament.enabled ? 'bi bi-toggle-on' : 'bi bi-toggle-off'"></i>
                    </span>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-if="!loading && tournaments.length === 0 && !error" class="row">
      <div class="col-12">
        <div class="alert alert-info">
          <i class="bi bi-inbox me-2"></i>No tournaments found.
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { tournamentAPI } from '@/services/api'

export default {
  name: 'TournamentListView',
  data () {
    return {
      tournaments: [],
      loading: true,
      error: null,
      successMessage: null,
      togglingId: null
    }
  },
  computed: {
    ...mapGetters('auth', ['isAdmin'])
  },
  mounted () {
    this.loadTournaments()
  },
  methods: {
    async loadTournaments () {
      this.loading = true
      this.error = null
      try {
        const response = await tournamentAPI.getTournaments()
        if (response.data.success) {
          this.tournaments = response.data.tournaments || []
        } else {
          this.error = response.data.message || 'Failed to load tournaments'
        }
      } catch (err) {
        this.error = err.response?.data?.message || 'Error loading tournaments'
        console.error('Error loading tournaments:', err)
      } finally {
        this.loading = false
      }
    },

    async toggleTournament (tournament) {
      this.togglingId = tournament.id
      this.successMessage = null
      try {
        const response = await tournamentAPI.toggleTournament(tournament.id)
        if (response.data.success) {
          tournament.enabled = response.data.tournament.enabled
          const status = tournament.enabled ? 'enabled' : 'disabled'
          this.successMessage = `"${tournament.name}" has been ${status}.`
        } else {
          this.error = response.data.message || 'Failed to toggle tournament'
        }
      } catch (err) {
        this.error = err.response?.data?.message || 'Error toggling tournament'
        console.error('Error toggling tournament:', err)
      } finally {
        this.togglingId = null
      }
    },

    viewDetails (id) {
      this.$router.push(`/tournaments/${id}`)
    },

    formatDate (timestamp) {
      if (!timestamp) return '-'
      const date = new Date(timestamp)
      return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      })
    }
  }
}
</script>

<style scoped>
.table-hover tbody tr:hover {
  background-color: #f8f9fa;
}
h1 {
  color: #2c3e50;
  font-weight: 600;
}
</style>
