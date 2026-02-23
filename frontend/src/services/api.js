import axios from 'axios'

// Create axios instance with base configuration
const apiClient = axios.create({
  baseURL: process.env.VUE_APP_API_BASE_URL || 'http://localhost:8098',
  timeout: process.env.VUE_APP_API_TIMEOUT || 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor to add auth header
apiClient.interceptors.request.use(
  (config) => {
    const credentials = localStorage.getItem('credentials')
    if (credentials) {
      const { username, password } = JSON.parse(credentials)
      const auth = btoa(`${username}:${password}`)
      config.headers.Authorization = `Basic ${auth}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor to handle errors
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Clear credentials and redirect to login
      localStorage.removeItem('credentials')
      localStorage.removeItem('user')
      window.location.href = '/#/login'
    }
    return Promise.reject(error)
  }
)

// Tournament API methods
export const tournamentAPI = {
  // Get all tournaments
  getTournaments () {
    return apiClient.get('/api/tournaments')
  },

  // Get tournament by ID
  getTournamentById (id) {
    return apiClient.get(`/api/tournaments/${id}`)
  },

  // Create tournament
  createTournament (data) {
    return apiClient.post('/api/tournaments', data)
  },

  // Get available tournament admins
  getAvailableAdmins () {
    return apiClient.get('/api/tournaments/admins/available')
  },

  // Add tournament admin
  addTournamentAdmin (id, data) {
    return apiClient.post(`/api/tournaments/${id}/admins`, data)
  },

  // Remove tournament admin
  removeTournamentAdmin (id, userId) {
    return apiClient.delete(`/api/tournaments/${id}/admins/${userId}`)
  },

  // Add tournament player
  addTournamentPlayer (id, data) {
    return apiClient.post(`/api/tournaments/${id}/players`, data)
  },

  // Toggle tournament enabled/disabled
  toggleTournament (id) {
    return apiClient.patch(`/api/tournaments/${id}/toggle`)
  },

  // Remove tournament player
  removeTournamentPlayer (id, userId) {
    return apiClient.delete(`/api/tournaments/${id}/players/${userId}`)
  }
}

export default apiClient
