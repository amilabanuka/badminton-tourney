<template>
  <div class="container">
    <div class="row">
      <div class="col-12">
        <h1 class="mb-4">Tournaments</h1>

        <div class="alert alert-info">
          <i class="bi bi-info-circle me-2"></i>
          Tournament list will be implemented in Phase 2
        </div>

        <div v-if="isAdmin" class="mb-4">
          <router-link to="/admin/tournaments/create" class="btn btn-primary">
            <i class="bi bi-plus-circle me-2"></i>Create New Tournament
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'TournamentListView',
  computed: {
    ...mapGetters('auth', ['isAdmin'])
  }
}
</script>
