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

    <div v-if="!loading && gameDay" class="row">
      <div class="col-12">

        <!-- Header Card -->
        <div class="card mb-4">
          <div class="card-header bg-light d-flex justify-content-between align-items-center">
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

            <!-- Actions for PENDING status -->
            <div v-if="gameDay.status === 'PENDING'" class="d-flex gap-2">
              <button
                class="btn btn-success"
                @click="startDay"
                :disabled="actionInProgress"
              >
                <span v-if="actionInProgress === 'start'">
                  <i class="bi bi-hourglass-split me-1"></i>Starting...
                </span>
                <span v-else>
                  <i class="bi bi-play-fill me-1"></i>Start Day
                </span>
              </button>
              <button
                class="btn btn-danger"
                @click="discardDay"
                :disabled="actionInProgress"
              >
                <span v-if="actionInProgress === 'discard'">
                  <i class="bi bi-hourglass-split me-1"></i>Discarding...
                </span>
                <span v-else>
                  <i class="bi bi-trash me-1"></i>Discard
                </span>
              </button>
            </div>
          </div>

          <!-- ONGOING banner -->
          <div v-if="gameDay.status === 'ONGOING'" class="card-body">
            <div class="alert alert-primary mb-0">
              <i class="bi bi-broadcast me-2"></i>
              <strong>This day is currently live.</strong> The full live view is coming soon.
            </div>
          </div>

          <!-- COMPLETED banner -->
          <div v-if="gameDay.status === 'COMPLETED'" class="card-body">
            <div class="alert alert-secondary mb-0">
              <i class="bi bi-check-all me-2"></i>This game day has been completed.
            </div>
          </div>
        </div>

        <!-- Group Cards -->
        <div class="row g-3">
          <div
            v-for="group in gameDay.groups"
            :key="group.id"
            class="col-sm-6 col-lg-4"
          >
            <div class="card h-100">
              <div class="card-header bg-primary text-white">
                <h6 class="mb-0">
                  <i class="bi bi-people-fill me-2"></i>Group {{ group.groupNumber }}
                  <span class="badge bg-white text-primary ms-2">{{ group.players.length }} players</span>
                </h6>
              </div>
              <div class="card-body p-0">
                <table class="table table-sm mb-0">
                  <thead class="table-light">
                    <tr>
                      <th>#</th>
                      <th>Player</th>
                      <th class="text-end">Rank Score</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(player, idx) in group.players" :key="player.tournamentPlayerId">
                      <td class="text-muted">{{ idx + 1 }}</td>
                      <td>{{ player.firstName }} {{ player.lastName }}</td>
                      <td class="text-end font-monospace">{{ player.rankScore }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
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
      actionInProgress: null
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

    async startDay () {
      if (!confirm('Start this game day? This will mark it as ONGOING and cannot be undone.')) return

      this.actionInProgress = 'start'
      this.error = null
      try {
        const res = await tournamentAPI.startGameDay(this.tournamentId, this.dayId)
        if (res.data.success) {
          this.gameDay = res.data.gameDay
          this.successMessage = 'Game day started successfully!'
        } else {
          this.error = res.data.message || 'Failed to start game day'
        }
      } catch (err) {
        this.error = err.response?.data?.message || 'Error starting game day'
        console.error('Error starting game day:', err)
      } finally {
        this.actionInProgress = null
      }
    },

    async discardDay () {
      if (!confirm('Discard this game day? This will permanently delete it and cannot be undone.')) return

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
        console.error('Error discarding game day:', err)
      } finally {
        this.actionInProgress = null
      }
    }
  }
}
</script>

<style scoped>
.card {
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
}
.font-monospace {
  font-family: 'Courier New', monospace;
  font-size: 0.85rem;
}
.table-sm {
  font-size: 0.9rem;
}
</style>
