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
          <i class="bi bi-hourglass-split me-2"></i>Loading tournament...
        </div>
      </div>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="row">
      <div class="col-12">
        <div class="alert alert-danger alert-dismissible fade show">
          <i class="bi bi-exclamation-triangle me-2"></i>{{ error }}
          <button type="button" class="btn-close" @click="error = null"></button>
        </div>
      </div>
    </div>

    <!-- Content -->
    <div v-else-if="tournament">
      <!-- Tournament Header -->
      <div class="card mb-4">
        <div class="card-header bg-light d-flex align-items-center gap-3">
          <h3 class="mb-0">
            <i class="bi bi-trophy me-2"></i>{{ tournament.name }}
          </h3>
          <span v-if="tournament.type === 'LEAGUE'" class="badge bg-primary fs-6">League</span>
          <span v-else-if="tournament.type === 'ONE_OFF'" class="badge bg-info text-dark fs-6">One-off</span>
          <span v-if="tournament.enabled" class="badge bg-success">Active</span>
          <span v-else class="badge bg-secondary">Inactive</span>
        </div>
      </div>

      <!-- Game Days Section -->
      <div class="card">
        <div class="card-header bg-light">
          <h5 class="mb-0"><i class="bi bi-calendar3 me-2"></i>Game Days</h5>
        </div>
        <div class="card-body">

          <!-- No game days -->
          <div v-if="!tournament.gameDays || tournament.gameDays.length === 0" class="alert alert-info mb-0">
            <i class="bi bi-inbox me-2"></i>No game days scheduled yet.
          </div>

          <div v-else>
            <!-- ONGOING day — highlighted at the top -->
            <template v-for="day in ongoingDays" :key="day.id">
              <div class="card border-success mb-3">
                <div class="card-body d-flex justify-content-between align-items-center flex-wrap gap-2">
                  <div class="d-flex align-items-center gap-3">
                    <i class="bi bi-play-circle-fill text-success fs-4"></i>
                    <div>
                      <div class="fw-bold">{{ day.gameDate }}</div>
                      <span class="badge bg-success">ONGOING</span>
                    </div>
                  </div>
                  <button
                    class="btn btn-success"
                    @click="enterGameDay(day.id)"
                  >
                    <i class="bi bi-door-open me-2"></i>Enter Game Day
                  </button>
                </div>
              </div>
            </template>

            <!-- Other days — greyed out with Coming Soon -->
            <div v-if="otherDays.length > 0">
              <h6 class="text-muted mb-2 mt-3">Previous Game Days</h6>
              <div class="list-group">
                <div
                  v-for="day in otherDays"
                  :key="day.id"
                  class="list-group-item list-group-item-action disabled d-flex justify-content-between align-items-center"
                  title="Coming soon — previous game day view is not yet available"
                  style="cursor: not-allowed; opacity: 0.55;"
                >
                  <div class="d-flex align-items-center gap-3">
                    <i class="bi bi-calendar-event text-muted fs-5"></i>
                    <span>{{ day.gameDate }}</span>
                  </div>
                  <div class="d-flex align-items-center gap-2">
                    <span
                      class="badge"
                      :class="{
                        'bg-secondary': day.status === 'COMPLETED',
                        'bg-warning text-dark': day.status === 'PENDING'
                      }"
                    >{{ day.status }}</span>
                    <span class="badge bg-light text-muted border">Coming soon</span>
                  </div>
                </div>
              </div>
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
  name: 'PlayerTournamentView',

  data () {
    return {
      tournament: null,
      loading: false,
      error: null
    }
  },

  computed: {
    ...mapGetters('auth', ['isPlayer']),

    ongoingDays () {
      return (this.tournament?.gameDays || []).filter(d => d.status === 'ONGOING')
    },

    otherDays () {
      return (this.tournament?.gameDays || []).filter(d => d.status !== 'ONGOING')
    }
  },

  mounted () {
    if (this.isPlayer) {
      this.loadTournament()
    }
  },

  methods: {
    async loadTournament () {
      this.loading = true
      this.error = null
      try {
        const id = this.$route.params.id
        const res = await tournamentAPI.getTournamentPlayerView(id)
        if (res.data.success) {
          this.tournament = res.data.tournament
        } else {
          this.error = res.data.message || 'Failed to load tournament'
        }
      } catch (err) {
        this.error = err.response?.data?.message || 'Error loading tournament'
        console.error('Error loading tournament:', err)
      } finally {
        this.loading = false
      }
    },

    enterGameDay (dayId) {
      this.$router.push(`/tournaments/${this.$route.params.id}/game-days/${dayId}`)
    }
  }
}
</script>

<style scoped>
.card {
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
}
.list-group-item.disabled {
  pointer-events: none;
}
</style>
