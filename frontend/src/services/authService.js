import apiClient from './api'

const authService = {
  /**
   * Sign up a new user (always with PLAYER role)
   */
  async signup (userData) {
    try {
      const response = await apiClient.post('/api/auth/signup', {
        ...userData,
        role: 'PLAYER' // Always set to PLAYER
      })
      return response.data
    } catch (error) {
      throw error.response?.data || { success: false, message: 'Sign up failed' }
    }
  },

  /**
   * Login user
   */
  async login (credentials) {
    try {
      const { username, password } = credentials
      const auth = btoa(`${username}:${password}`)

      const response = await apiClient.post('/api/auth/login', credentials, {
        headers: {
          Authorization: `Basic ${auth}`
        }
      })

      if (response.data.success) {
        // Store credentials and user data
        localStorage.setItem('credentials', JSON.stringify({ username, password }))
        localStorage.setItem('user', JSON.stringify(response.data.user))
      }

      return response.data
    } catch (error) {
      throw error.response?.data || { success: false, message: 'Login failed' }
    }
  },

  /**
   * Logout user
   */
  logout () {
    localStorage.removeItem('credentials')
    localStorage.removeItem('user')
  },

  /**
   * Get current user from localStorage
   */
  getCurrentUser () {
    const userStr = localStorage.getItem('user')
    return userStr ? JSON.parse(userStr) : null
  },

  /**
   * Check if user is authenticated
   */
  isAuthenticated () {
    return !!localStorage.getItem('credentials')
  },

  /**
   * Check if user has specific role
   */
  hasRole (role) {
    const user = this.getCurrentUser()
    return user?.role === role
  }
}

export default authService
