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

              <!-- Tournament Type -->
              <div class="mb-3">
                <label for="type" class="form-label">Tournament Type <span class="text-danger">*</span></label>
                <select
                  id="type"
                  v-model="form.type"
                  class="form-select"
                  :class="{ 'is-invalid': errors.type }"
                  @change="validateType"
                >
                  <option value="">-- Select a Tournament Type --</option>
                  <option value="LEAGUE">League</option>
                  <option value="ONE_OFF">One-off</option>
                </select>
                <div v-if="errors.type" class="invalid-feedback d-block">
                  {{ errors.type }}
                </div>
              </div>

              <!-- League Settings -->
              <div v-if="showLeagueSettings" class="mb-3 p-3 border rounded bg-light">
                <h6 class="mb-3">League Settings</h6>
                <div class="mb-3">
                  <label class="form-label">Ranking Logic</label>
                  <p class="form-control-plaintext fw-bold mb-0">Modified ELO</p>
                </div>
                <div class="mb-3">
                  <label for="k" class="form-label">K Factor <span class="text-danger">*</span></label>
                  <input
                    id="k"
                    v-model.number="form.k"
                    type="number"
                    min="1"
                    class="form-control"
                    :class="{ 'is-invalid': errors.k }"
                    placeholder="e.g. 32"
                  >
                  <div class="form-text">Controls how much a single match affects rankings. Typical value: 32.</div>
                  <div v-if="errors.k" class="invalid-feedback d-block">{{ errors.k }}</div>
                </div>
                <div class="mb-0">
                  <label for="absenteeDemerit" class="form-label">Absentee Demerit <span class="text-danger">*</span></label>
                  <input
                    id="absenteeDemerit"
                    v-model.number="form.absenteeDemerit"
                    type="number"
                    min="0"
                    class="form-control"
                    :class="{ 'is-invalid': errors.absenteeDemerit }"
                    placeholder="e.g. 10"
                  >
                  <div class="form-text">Points deducted when a player is absent. Use 0 for no penalty.</div>
                  <div v-if="errors.absenteeDemerit" class="invalid-feedback d-block">{{ errors.absenteeDemerit }}</div>
                </div>
              </div>

              <!-- One-off Settings -->
              <div v-if="showOneOffSettings" class="mb-3 p-3 border rounded bg-light">
                <h6 class="mb-3">One-off Settings</h6>
                <div class="mb-3">
                  <label for="numberOfRounds" class="form-label">Number of Rounds <span class="text-danger">*</span></label>
                  <input
                    id="numberOfRounds"
                    v-model.number="form.numberOfRounds"
                    type="number"
                    min="1"
                    class="form-control"
                    :class="{ 'is-invalid': errors.numberOfRounds }"
                    placeholder="e.g. 3"
                  >
                  <div v-if="errors.numberOfRounds" class="invalid-feedback d-block">{{ errors.numberOfRounds }}</div>
                </div>
                <div class="mb-0">
                  <label for="maxPoints" class="form-label">Max Points Per Game <span class="text-danger">*</span></label>
                  <select
                    id="maxPoints"
                    v-model.number="form.maxPoints"
                    class="form-select"
                    :class="{ 'is-invalid': errors.maxPoints }"
                  >
                    <option value="">-- Select --</option>
                    <option :value="15">15</option>
                    <option :value="21">21</option>
                  </select>
                  <div v-if="errors.maxPoints" class="invalid-feedback d-block">{{ errors.maxPoints }}</div>
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
        enabled: true,
        type: '',
        k: '',
        absenteeDemerit: '',
        numberOfRounds: '',
        maxPoints: ''
      },
      availableAdmins: [],
      errors: {},
      error: null,
      successMessage: null,
      loading: false,
      loadingAdmins: false
    }
  },
  computed: {
    showLeagueSettings () {
      return this.form.type === 'LEAGUE'
    },
    showOneOffSettings () {
      return this.form.type === 'ONE_OFF'
    }
  },
  watch: {
    'form.type' () {
      this.form.k = ''
      this.form.absenteeDemerit = ''
      this.form.numberOfRounds = ''
      this.form.maxPoints = ''
      this.errors.k = null
      this.errors.absenteeDemerit = null
      this.errors.numberOfRounds = null
      this.errors.maxPoints = null
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

    validateType () {
      this.errors.type = ''
      if (!this.form.type) {
        this.errors.type = 'Tournament type is required'
      }
    },

    validateSettings () {
      let isValid = true
      if (this.form.type === 'LEAGUE') {
        if (this.form.k === '' || this.form.k === null || this.form.k <= 0) {
          this.errors.k = 'K factor must be a positive integer'
          isValid = false
        }
        if (this.form.absenteeDemerit === '' || this.form.absenteeDemerit === null || this.form.absenteeDemerit < 0) {
          this.errors.absenteeDemerit = 'Absentee demerit must be 0 or greater'
          isValid = false
        }
      } else if (this.form.type === 'ONE_OFF') {
        if (this.form.numberOfRounds === '' || this.form.numberOfRounds === null || this.form.numberOfRounds <= 0) {
          this.errors.numberOfRounds = 'Number of rounds must be a positive integer'
          isValid = false
        }
        if (this.form.maxPoints !== 15 && this.form.maxPoints !== 21) {
          this.errors.maxPoints = 'Max points must be 15 or 21'
          isValid = false
        }
      }
      return isValid
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

      if (!this.form.type) {
        this.errors.type = 'Tournament type is required'
        isValid = false
      }

      if (!this.validateSettings()) {
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
        const payload = {
          name: this.form.name.trim(),
          ownerId: parseInt(this.form.ownerId),
          enabled: this.form.enabled,
          type: this.form.type
        }
        if (this.form.type === 'LEAGUE') {
          payload.leagueSettings = {
            rankingLogic: 'MODIFIED_ELO',
            k: parseInt(this.form.k),
            absenteeDemerit: parseInt(this.form.absenteeDemerit)
          }
        } else if (this.form.type === 'ONE_OFF') {
          payload.oneOffSettings = {
            numberOfRounds: parseInt(this.form.numberOfRounds),
            maxPoints: parseInt(this.form.maxPoints)
          }
        }
        const response = await tournamentAPI.createTournament(payload)

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
