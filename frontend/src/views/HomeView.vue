<template>
  <div class="home">
    <div v-if="!isAuthenticated" class="text-center py-5">
      <img alt="Badminton logo" src="../assets/logo.png" style="max-width: 200px;">
      <h1 class="mt-4 mb-3">Welcome to Badminton Manager</h1>
      <p class="lead mb-4">Manage your badminton tournaments with ease</p>

      <div class="d-flex justify-content-center gap-3">
        <router-link to="/signup" class="btn btn-primary btn-lg">
          Get Started
        </router-link>
        <router-link to="/login" class="btn btn-outline-primary btn-lg">
          Login
        </router-link>
      </div>

      <div class="mt-5">
        <h3>Features</h3>
        <div class="row mt-4">
          <div class="col-md-4">
            <i class="bi bi-trophy fs-1 text-primary"></i>
            <h5 class="mt-3">Tournament Management</h5>
            <p class="text-muted">Create and manage badminton tournaments</p>
          </div>
          <div class="col-md-4">
            <i class="bi bi-people fs-1 text-success"></i>
            <h5 class="mt-3">Player Registration</h5>
            <p class="text-muted">Easily register and manage players</p>
          </div>
          <div class="col-md-4">
            <i class="bi bi-gear fs-1 text-info"></i>
            <h5 class="mt-3">Admin Controls</h5>
            <p class="text-muted">Comprehensive admin and organizer tools</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'HomeView',
  computed: {
    ...mapGetters('auth', ['isAuthenticated'])
  },
  mounted  () {
    // Redirect to dashboard if already authenticated
    if (this.isAuthenticated) {
      this.$router.push('/dashboard')
    }
  }
}
</script>

<style scoped>
.gap-3 {
  gap: 1rem;
}
</style>
