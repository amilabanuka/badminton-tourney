<template>
  <div class="container mt-4">
    <div class="row">
      <div class="col-12">
        <div class="welcome-section mb-4">
          <h1 class="display-5">Welcome, {{ currentUser?.firstName || 'User' }}!</h1>
          <p class="lead">
            <span class="badge" :class="roleBadgeClass">{{ currentUser?.role }}</span>
          </p>
        </div>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="row mb-4">
      <div class="col-12">
        <h4 class="mb-3">Quick Actions</h4>
      </div>

      <!-- ADMIN Actions -->
      <div v-if="isAdmin" class="col-md-4 mb-3">
        <div class="card action-card h-100" @click="$router.push('/admin/tournaments/create')">
          <div class="card-body text-center">
            <i class="bi bi-plus-circle fs-1 text-primary mb-3"></i>
            <h5 class="card-title">Create Tournament</h5>
            <p class="card-text text-muted">Create and set up a new tournament</p>
          </div>
        </div>
      </div>

      <div v-if="isAdmin" class="col-md-4 mb-3">
        <div class="card action-card h-100" @click="$router.push('/tournaments')">
          <div class="card-body text-center">
            <i class="bi bi-gear fs-1 text-success mb-3"></i>
            <h5 class="card-title">Manage Tournaments</h5>
            <p class="card-text text-muted">Manage all tournaments and participants</p>
          </div>
        </div>
      </div>

      <!-- TOURNY_ADMIN Actions -->
      <div v-if="isTournyAdmin" class="col-md-4 mb-3">
        <div class="card action-card h-100" @click="$router.push('/tournaments')">
          <div class="card-body text-center">
            <i class="bi bi-trophy fs-1 text-info mb-3"></i>
            <h5 class="card-title">My Tournaments</h5>
            <p class="card-text text-muted">Manage tournaments you administer</p>
          </div>
        </div>
      </div>

      <!-- Common Action for all -->
      <div class="col-md-4 mb-3">
        <div class="card action-card h-100" @click="$router.push('/tournaments')">
          <div class="card-body text-center">
            <i class="bi bi-list-ul fs-1 text-secondary mb-3"></i>
            <h5 class="card-title">View All Tournaments</h5>
            <p class="card-text text-muted">Browse all available tournaments</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Summary Cards -->
    <div class="row">
      <div class="col-12">
        <h4 class="mb-3">Summary</h4>
      </div>

      <div class="col-md-4 mb-3">
        <div class="card summary-card">
          <div class="card-body">
            <h6 class="card-subtitle mb-2 text-muted">Total Tournaments</h6>
            <h2 class="card-title mb-0">-</h2>
            <small class="text-muted">Coming soon</small>
          </div>
        </div>
      </div>

      <div v-if="isTournyAdmin" class="col-md-4 mb-3">
        <div class="card summary-card">
          <div class="card-body">
            <h6 class="card-subtitle mb-2 text-muted">My Tournaments</h6>
            <h2 class="card-title mb-0">-</h2>
            <small class="text-muted">Coming soon</small>
          </div>
        </div>
      </div>

      <div class="col-md-4 mb-3">
        <div class="card summary-card">
          <div class="card-body">
            <h6 class="card-subtitle mb-2 text-muted">Upcoming Events</h6>
            <h2 class="card-title mb-0">-</h2>
            <small class="text-muted">Coming soon</small>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'DashboardView',
  computed: {
    ...mapGetters('auth', ['currentUser', 'isAdmin', 'isTournyAdmin', 'isPlayer']),

    roleBadgeClass () {
      if (this.isAdmin) return 'bg-danger'
      if (this.isTournyAdmin) return 'bg-primary'
      return 'bg-success'
    }
  }
}
</script>

<style scoped>
.welcome-section {
  padding: 20px 0;
  border-bottom: 2px solid #e9ecef;
}

.badge {
  font-size: 0.9rem;
  padding: 8px 16px;
  font-weight: 500;
}

.action-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.action-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.summary-card {
  border: none;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.summary-card .card-body {
  padding: 1.5rem;
}

.summary-card h2 {
  color: #2c3e50;
  font-weight: 600;
}
</style>
