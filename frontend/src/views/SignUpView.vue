<template>
  <div class="container">
    <div class="row justify-content-center mt-5">
      <div class="col-md-6">
        <div class="card shadow">
          <div class="card-body p-4">
            <h2 class="card-title text-center mb-4">Sign Up</h2>

            <div v-if="successMessage" class="alert alert-success" role="alert">
              {{ successMessage }}
            </div>

            <div v-if="errorMessage" class="alert alert-danger" role="alert">
              {{ errorMessage }}
            </div>

            <form @submit.prevent="handleSignup">
              <div class="mb-3">
                <label for="username" class="form-label">Username <span class="text-danger">*</span></label>
                <input
                  type="text"
                  class="form-control"
                  :class="{ 'is-invalid': errors.username }"
                  id="username"
                  v-model="form.username"
                  required
                  @input="clearError('username')"
                />
                <div v-if="errors.username" class="invalid-feedback">{{ errors.username }}</div>
              </div>

              <div class="mb-3">
                <label for="email" class="form-label">Email <span class="text-danger">*</span></label>
                <input
                  type="email"
                  class="form-control"
                  :class="{ 'is-invalid': errors.email }"
                  id="email"
                  v-model="form.email"
                  required
                  @input="clearError('email')"
                />
                <div v-if="errors.email" class="invalid-feedback">{{ errors.email }}</div>
              </div>

              <div class="row">
                <div class="col-md-6 mb-3">
                  <label for="firstName" class="form-label">First Name <span class="text-danger">*</span></label>
                  <input
                    type="text"
                    class="form-control"
                    :class="{ 'is-invalid': errors.firstName }"
                    id="firstName"
                    v-model="form.firstName"
                    required
                    @input="clearError('firstName')"
                  />
                  <div v-if="errors.firstName" class="invalid-feedback">{{ errors.firstName }}</div>
                </div>

                <div class="col-md-6 mb-3">
                  <label for="lastName" class="form-label">Last Name <span class="text-danger">*</span></label>
                  <input
                    type="text"
                    class="form-control"
                    :class="{ 'is-invalid': errors.lastName }"
                    id="lastName"
                    v-model="form.lastName"
                    required
                    @input="clearError('lastName')"
                  />
                  <div v-if="errors.lastName" class="invalid-feedback">{{ errors.lastName }}</div>
                </div>
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
                <small class="form-text text-muted">Minimum 6 characters</small>
              </div>

              <div class="mb-3">
                <label for="confirmPassword" class="form-label">Confirm Password <span class="text-danger">*</span></label>
                <input
                  type="password"
                  class="form-control"
                  :class="{ 'is-invalid': errors.confirmPassword }"
                  id="confirmPassword"
                  v-model="form.confirmPassword"
                  required
                  @input="clearError('confirmPassword')"
                />
                <div v-if="errors.confirmPassword" class="invalid-feedback">{{ errors.confirmPassword }}</div>
              </div>

              <div class="alert alert-info">
                <small><i class="bi bi-info-circle"></i> You will be registered as a Player</small>
              </div>

              <button type="submit" class="btn btn-primary w-100" :disabled="loading">
                <span v-if="loading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                {{ loading ? 'Signing up...' : 'Sign Up' }}
              </button>
            </form>

            <div class="text-center mt-3">
              <p class="mb-0">Already have an account? <router-link to="/login">Login here</router-link></p>
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
  name: 'SignUpView',
  data () {
    return {
      form: {
        username: '',
        email: '',
        password: '',
        confirmPassword: '',
        firstName: '',
        lastName: ''
      },
      errors: {},
      errorMessage: '',
      successMessage: '',
      loading: false
    }
  },
  methods: {
    ...mapActions('auth', ['signup']),

    clearError (field) {
      if (this.errors[field]) {
        delete this.errors[field]
      }
      this.errorMessage = ''
    },

    validateForm () {
      this.errors = {}
      let isValid = true

      // Username validation
      if (!this.form.username.trim()) {
        this.errors.username = 'Username is required'
        isValid = false
      }

      // Email validation
      if (!this.form.email.trim()) {
        this.errors.email = 'Email is required'
        isValid = false
      } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.form.email)) {
        this.errors.email = 'Invalid email format'
        isValid = false
      }

      // First name validation
      if (!this.form.firstName.trim()) {
        this.errors.firstName = 'First name is required'
        isValid = false
      }

      // Last name validation
      if (!this.form.lastName.trim()) {
        this.errors.lastName = 'Last name is required'
        isValid = false
      }

      // Password validation
      if (!this.form.password) {
        this.errors.password = 'Password is required'
        isValid = false
      } else if (this.form.password.length < 6) {
        this.errors.password = 'Password must be at least 6 characters'
        isValid = false
      }

      // Confirm password validation
      if (!this.form.confirmPassword) {
        this.errors.confirmPassword = 'Please confirm your password'
        isValid = false
      } else if (this.form.password !== this.form.confirmPassword) {
        this.errors.confirmPassword = 'Passwords do not match'
        isValid = false
      }

      return isValid
    },

    async handleSignup  () {
      this.errorMessage = ''
      this.successMessage = ''

      if (!this.validateForm()) {
        return
      }

      this.loading = true

      try {
        const response = await this.signup({
          username: this.form.username,
          email: this.form.email,
          password: this.form.password,
          firstName: this.form.firstName,
          lastName: this.form.lastName
        })

        if (response.success) {
          this.successMessage = 'Registration successful! Redirecting to login...'
          setTimeout(() => {
            this.$router.push('/login')
          }, 2000)
        } else {
          this.errorMessage = response.message || 'Sign up failed'
        }
      } catch (error) {
        this.errorMessage = error.message || 'Sign up failed. Please try again.'
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
