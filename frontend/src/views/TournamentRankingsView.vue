<template>
  <div class="container mt-4">
    <!-- Loading -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
      <p class="mt-3 text-muted">Loading rankings...</p>
    </div>

    <!-- Error -->
    <div v-if="error && !loading" class="alert alert-danger">
      <i class="bi bi-exclamation-triangle me-2"></i>{{ error }}
    </div>

    <div v-if="!loading && tournament">
      <!-- Header -->
      <div class="row mb-4 align-items-center">
        <div class="col">
          <h2 class="mb-1">
            <i class="bi bi-trophy-fill me-2 text-warning"></i>{{ tournament.name }}
          </h2>
          <p class="text-muted mb-0">Current Player Rankings</p>
        </div>
        <div class="col-auto">
          <span class="badge bg-secondary fs-6">{{ rankedPlayers.length }} players</span>
        </div>
      </div>

      <!-- Rankings table -->
      <div class="card shadow-sm">
        <div class="card-body p-0">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th class="text-center ps-3" style="width:70px">Rank</th>
                <th>Player</th>
                <th class="text-end pe-4" style="width:160px">Score</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(player, index) in rankedPlayers" :key="player.tournamentPlayerId">
                <td class="text-center ps-3">
                  <span v-if="index === 0" class="fs-5" title="1st place">🥇</span>
                  <span v-else-if="index === 1" class="fs-5" title="2nd place">🥈</span>
                  <span v-else-if="index === 2" class="fs-5" title="3rd place">🥉</span>
                  <span v-else class="text-muted fw-semibold">{{ index + 1 }}</span>
                </td>
                <td>
                  <span class="fw-semibold">{{ player.firstName }} {{ player.lastName }}</span>
                </td>
                <td class="text-end pe-4">
                  <span class="badge bg-primary-subtle text-primary-emphasis border border-primary-subtle px-3 py-2 font-monospace fs-6">
                    {{ Number(player.rankScore).toFixed(2) }}
                  </span>
                </td>
              </tr>
              <tr v-if="rankedPlayers.length === 0">
                <td colspan="3" class="text-center text-muted py-5">
                  <i class="bi bi-inbox fs-3 d-block mb-2"></i>No players found.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <p class="text-muted small mt-3 text-center">
        Rankings updated after each completed game day.
      </p>
    </div>
  </div>
</template>

<script>
import { tournamentAPI } from '@/services/api'

export default {
  name: 'TournamentRankingsView',

  data () {
    return {
      tournament: null,
      loading: true,
      error: null
    }
  },

  computed: {
    rankedPlayers () {
      if (!this.tournament || !this.tournament.players) return []
      return [...this.tournament.players]
        .filter(p => p.status === 'ENABLED')
        .sort((a, b) => {
          const diff = parseFloat(b.rankScore) - parseFloat(a.rankScore)
          if (diff !== 0) return diff
          return (a.id || 0) - (b.id || 0)
        })
    }
  },

  async mounted () {
    const id = this.$route.params.id
    try {
      const res = await tournamentAPI.getPublicRankings(id)
      if (res.data.success) {
        this.tournament = res.data.tournament
      } else {
        this.error = res.data.message || 'Failed to load rankings'
      }
    } catch (err) {
      this.error = err.response?.data?.message || 'Error loading rankings'
      console.error('Error loading rankings:', err)
    } finally {
      this.loading = false
    }
  }
}
</script>

<style scoped>
.font-monospace {
  font-family: 'Courier New', monospace;
}
</style>
