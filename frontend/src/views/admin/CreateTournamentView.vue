<template>
  <div class="container mt-4">
    <div class="row">
      <div class="col-lg-8 offset-lg-2">
        <div class="card">
          <div class="card-header bg-light">
            <h3 class="mb-0">Create New Tournament</h3>
          </div>
          <div class="card-body">
            <!-- Error Alert -->
            <div v-if="error" class="alert alert-danger alert-dismissible fade show" role="alert">
              <i class="bi bi-exclamation-triangle me-2"></i>
              {{ error }}
              <button type="button" class="btn-close" @click="error = null"></button>
            </div>

            <!-- Success Alert -->
            <div v-if="successMessage" class="alert alert-success alert-dismissible fade show" role="alert">
              <i class="bi bi-check-circle me-2"></i>
              {{ successMessage }}
              <button type="button" class="btn-close" @click="successMessage = null"></button>
            </div>

            <!-- Form -->
            <form @submit.prevent="submitForm">
              <!-- Tournament Name -->
              <div class="mb-3">
                <label for="name" class="form-label">Tournament Name <span class="text-danger">*</span></label>
                <input
                  id="name"
                  v-model="form.name"
                  type="text"
                  class="form-control"
                  :class="{ 'is-invalid': errors.name }"
                  placeholder="Enter tournament name"
                  @blur="validateName"
                >
                <div v-if="errors.name" class="invalid-feedback d-block">
                  {{ errors.name }}
                </div>
              </div>

              <!-- Tournament Owner -->
              <div class="mb-3">
                <label for="owner" class="form-label">Tournament Owner <span class="text-danger">*</span></label>
                <select
                  id="owner"
                  v-model="form.ownerId"
                  class="form-select"
                  :class="{ 'is-invalid': errors.ownerId }"
                  @change="validateOwner"
                >
                  <option value="">-- Select a Tournament Admin --</option>
                  <option v-for="admin in availableAdmins" :key="admin.id" :value="admin.id">
                    {{ admin.firstName }} {{ admin.lastName }} ({{ admin.email }})
                  </option>
                </select>
                <div v-if="errors.ownerId" class="invalid-feedback d-block">
                  {{ errors.ownerId }}
                </div>
              </div>

              <!-- Enabled Status -->
              <div class="mb-3">
                <div class="form-check form-switch">
                  <input
                    id="enabled"
                    v-model="form.enabled"
                    class="form-check-input"
                    type="checkbox"
                  >
                  <label for="enabled" class="form-check-label">
                    Enable tournament immediately
                  </label>
                </div>
              </div>

              <!-- Buttons -->
              <div class="d-flex gap-2">
                <button type="submit" class="btn btn-primary" :disabled="loading">
                  <span v-if="loading">
                    <i class="bi bi-hourglass-split me-2"></i>Creating...
                  </span>
                  <span v-else>
                    <i class="bi bi-check-circle me-2"></i>Create Tournament
                  </span>
                </button>
                <router-link to="/admin/tournaments" class="btn btn-secondary">
                  Cancel
                </router-link>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { tournamentAPI } from '@/services/api'

export default {
  name: 'CreateTournamentView',
  data () {
    return {
      form: {
        name: '',
        ownerId: '',
        enabled: true
      },
      availableAdmins: [],
      errors: {},
      error: null,
      successMessage: null,
      loading: false,
      loadingAdmins: false
    }
  },
  mounted () {
    this.loadAvailableAdmins()
  },
  methods: {
    async loadAvailableAdmins () {
      this.loadingAdmins = true
      try {
        const response = await tournamentAPI.getAvailableAdmins()
        if (response.data.success) {
          this.availableAdmins = response.data.users || []
        } else {
          this.error = response.data.message || 'Failed to load admins'
        }
      } catch (err) {
        this.error = err.response?.data?.message || 'Error loading tournament admins'
        console.error('Error loading admins:', err)
      } finally {
        this.loadingAdmins = false
      }
    },

    validateName () {
      this.errors.name = ''
      if (!this.form.name.trim()) {
        this.errors.name = 'Tournament name is required'
      } else if (this.form.name.trim().length < 3) {
        this.errors.name = 'Tournament name must be at least 3 characters'
      }
    },

    validateOwner () {
      this.errors.ownerId = ''
      if (!this.form.ownerId) {
        this.errors.ownerId = 'Tournament owner is required'
      }
    },

    validateForm () {
      this.errors = {}
      let isValid = true

      if (!this.form.name.trim()) {
        this.errors.name = 'Tournament name is required'
        isValid = false
      } else if (this.form.name.trim().length < 3) {
        this.errors.name = 'Tournament name must be at least 3 characters'
        isValid = false
      }

      if (!this.form.ownerId) {
        this.errors.ownerId = 'Tournament owner is required'
        isValid = false
      }

      return isValid
    },

    async submitForm () {
      if (!this.validateForm()) {
        return
      }

      this.loading = true
      this.error = null
      try {
        const response = await tournamentAPI.createTournament({
          name: this.form.name.trim(),
          ownerId: parseInt(this.form.ownerId),
          enabled: this.form.enabled
        })

        if (response.data.success) {
          this.successMessage = 'Tournament created successfully!'
          // Redirect after a short delay
          setTimeout(() => {
            this.$router.push('/admin/tournaments')
          }, 1500)
        } else {
          this.error = response.data.message || 'Failed to create tournament'
        }
      } catch (err) {
        this.error = err.response?.data?.message || 'Error creating tournament'
        console.error('Error creating tournament:', err)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.card {
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
}

h3 {
  color: #2c3e50;
  font-weight: 600;
}

.form-label {
  font-weight: 500;
}

.text-danger {
  color: #dc3545;
}
</style>
