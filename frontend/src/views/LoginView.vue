<template>
  <div class="container">
    <div class="row justify-content-center mt-5">
      <div class="col-md-5">
        <div class="card shadow">
          <div class="card-body p-4">
            <h2 class="card-title text-center mb-4">Login</h2>

            <div v-if="errorMessage" class="alert alert-danger" role="alert">
              {{ errorMessage }}
            </div>

            <form @submit.prevent="handleLogin">
              <div class="mb-3">
                <label for="username" class="form-label">Username <span class="text-danger">*</span></label>
                <input
                  type="text"
                  class="form-control"
                  :class="{ 'is-invalid': errors.username }"
                  id="username"
                  v-model="form.username"
                  required
                  autofocus
                  @input="clearError('username')"
                />
                <div v-if="errors.username" class="invalid-feedback">{{ errors.username }}</div>
              </div>

              <div class="mb-3">
                <label for="password" class="form-label">Password <span class="text-danger">*</span></label>
                <input
                  type="password"
                  class="form-control"
                  :class="{ 'is-invalid': errors.password }"
                  id="password"
                  v-model="form.password"
                  required
                  @input="clearError('password')"
                />
                <div v-if="errors.password" class="invalid-feedback">{{ errors.password }}</div>
              </div>

              <button type="submit" class="btn btn-primary w-100" :disabled="loading">
                <span v-if="loading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                {{ loading ? 'Logging in...' : 'Login' }}
              </button>
            </form>

            <div class="text-center mt-3">
              <p class="mb-0">Don't have an account? <router-link to="/signup">Sign up here</router-link></p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapActions } from 'vuex'

export default {
  name: 'LoginView',
  data  () {
    return {
      form: {
        username: '',
        password: ''
      },
      errors: {},
      errorMessage: '',
      loading: false
    }
  },
  methods: {
    ...mapActions('auth', ['login']),

    clearError  (field) {
      if (this.errors[field]) {
        delete this.errors[field]
      }
      this.errorMessage = ''
    },

    validateForm  () {
      this.errors = {}
      let isValid = true

      if (!this.form.username.trim()) {
        this.errors.username = 'Username is required'
        isValid = false
      }

      if (!this.form.password) {
        this.errors.password = 'Password is required'
        isValid = false
      }

      return isValid
    },

    async handleLogin  () {
      this.errorMessage = ''

      if (!this.validateForm()) {
        return
      }

      this.loading = true

      try {
        const response = await this.login({
          username: this.form.username,
          password: this.form.password
        })

        if (response.success) {
          // Redirect to dashboard
          this.$router.push('/dashboard')
        } else {
          this.errorMessage = response.message || 'Login failed'
        }
      } catch (error) {
        this.errorMessage = error.message || 'Invalid username or password'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.card {
  border: none;
  border-radius: 10px;
}

.card-title {
  color: #2c3e50;
  font-weight: 600;
}

.btn-primary {
  background-color: #0d6efd;
  border: none;
  padding: 10px;
  font-weight: 500;
}

.btn-primary:hover {
  background-color: #0b5ed7;
}

.form-label {
  font-weight: 500;
  color: #495057;
}

.text-danger {
  color: #dc3545;
}
</style>
