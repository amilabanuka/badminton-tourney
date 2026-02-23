<template>
  <nav class="navbar navbar-expand-lg navbar-light bg-light">
    <div class="container">
      <router-link class="navbar-brand" to="/">
        <i class="bi bi-trophy-fill me-2"></i>Badminton Manager
      </router-link>

      <button
        class="navbar-toggler"
        type="button"
        data-bs-toggle="collapse"
        data-bs-target="#mainNav"
        aria-controls="mainNav"
        aria-expanded="false"
        aria-label="Toggle navigation"
      >
        <span class="navbar-toggler-icon"></span>
      </button>

      <div class="collapse navbar-collapse" id="mainNav">
        <ul class="navbar-nav me-auto">
          <li class="nav-item">
            <router-link class="nav-link" to="/">Home</router-link>
          </li>

          <li v-if="isAuthenticated && !isAdmin" class="nav-item">
            <router-link class="nav-link" to="/tournaments">Tournaments</router-link>
          </li>

          <li v-if="isAdmin" class="nav-item">
            <router-link class="nav-link" to="/admin/tournaments">Admin Panel</router-link>
          </li>
        </ul>

        <ul class="navbar-nav">
          <!-- Guest Navigation -->
          <template v-if="!isAuthenticated">
            <li class="nav-item">
              <router-link class="nav-link" to="/login">Login</router-link>
            </li>
            <li class="nav-item">
              <router-link class="nav-link btn btn-primary text-white ms-2" to="/signup">Sign Up</router-link>
            </li>
          </template>

          <!-- Authenticated Navigation -->
          <template v-else>
            <li class="nav-item">
              <router-link class="nav-link" to="/dashboard">Dashboard</router-link>
            </li>

            <li class="nav-item dropdown">
              <a
                class="nav-link dropdown-toggle"
                href="#"
                id="userDropdown"
                role="button"
                data-bs-toggle="dropdown"
                aria-expanded="false"
              >
                <i class="bi bi-person-circle me-1"></i>
                {{ currentUser?.firstName || 'User' }}
                <span class="badge ms-2" :class="roleBadgeClass">{{ currentUser?.role }}</span>
              </a>
              <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="userDropdown">
                <li><h6 class="dropdown-header">{{ currentUser?.email }}</h6></li>
                <li><hr class="dropdown-divider"></li>
                <li><a class="dropdown-item" href="#" @click.prevent="handleLogout">
                  <i class="bi bi-box-arrow-right me-2"></i>Logout
                </a></li>
              </ul>
            </li>
          </template>
        </ul>
      </div>
    </div>
  </nav>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'

export default {
  name: 'NavBar',
  computed: {
    ...mapGetters('auth', ['currentUser', 'isAuthenticated', 'isAdmin', 'isTournyAdmin']),

    roleBadgeClass  () {
      if (this.isAdmin) return 'bg-danger'
      if (this.isTournyAdmin) return 'bg-primary'
      return 'bg-success'
    }
  },
  methods: {
    ...mapActions('auth', ['logout']),

    handleLogout  () {
      this.logout()
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.navbar-brand {
  font-weight: 600;
  font-size: 1.3rem;
  color: #2c3e50;
}

.navbar-brand:hover {
  color: #0d6efd;
}

.nav-link {
  font-weight: 500;
  color: #495057;
  transition: color 0.3s;
}

.nav-link:hover {
  color: #0d6efd;
}

.nav-link.router-link-exact-active {
  color: #0d6efd;
}

.btn-primary {
  padding: 6px 20px;
}

.badge {
  font-size: 0.7rem;
  padding: 4px 8px;
}

.dropdown-header {
  font-size: 0.9rem;
  color: #6c757d;
}
</style>
