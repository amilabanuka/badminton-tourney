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

  // Update tournament settings (config values only, not ranking logic type)
  updateTournamentSettings (id, data) {
    return apiClient.put(`/api/tournaments/${id}/settings`, data)
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

  // Get available players for a tournament (not yet added)
  getAvailablePlayers (id) {
    return apiClient.get(`/api/tournaments/${id}/players/available`)
  },

  // Enable a player in a tournament
  enablePlayer (id, userId) {
    return apiClient.post(`/api/tournaments/${id}/players/${userId}/enable`)
  },

  // Disable a player in a tournament
  disablePlayer (id, userId) {
    return apiClient.post(`/api/tournaments/${id}/players/${userId}/disable`)
  },

  // Toggle tournament enabled/disabled
  toggleTournament (id) {
    return apiClient.patch(`/api/tournaments/${id}/toggle`)
  },

  // ── League Game Day ──────────────────────────────────────────────

  // Create a new league game day
  createGameDay (tournamentId, data) {
    return apiClient.post(`/api/tournaments/${tournamentId}/game-days`, data)
  },

  // Get all game days for a tournament
  getGameDays (tournamentId) {
    return apiClient.get(`/api/tournaments/${tournamentId}/game-days`)
  },

  // Get a specific game day
  getGameDay (tournamentId, dayId) {
    return apiClient.get(`/api/tournaments/${tournamentId}/game-days/${dayId}`)
  },

  // Start a game day (PENDING → ONGOING)
  startGameDay (tournamentId, dayId) {
    return apiClient.post(`/api/tournaments/${tournamentId}/game-days/${dayId}/start`)
  },

  // Discard (delete) a PENDING game day
  discardGameDay (tournamentId, dayId) {
    return apiClient.delete(`/api/tournaments/${tournamentId}/game-days/${dayId}`)
  },

  // Cancel (delete) a PENDING or ONGOING game day
  cancelGameDay (tournamentId, dayId) {
    return apiClient.delete(`/api/tournaments/${tournamentId}/game-days/${dayId}/cancel`)
  },

  // Submit or overwrite score for a match (ONGOING days only)
  submitMatchScore (tournamentId, dayId, groupId, matchId, data) {
    return apiClient.put(
      `/api/tournaments/${tournamentId}/game-days/${dayId}/groups/${groupId}/matches/${matchId}/score`,
      data
    )
  },

  // Finish a game day (ONGOING → COMPLETED): triggers Modified-ELO rank calculation
  finishGameDay (tournamentId, dayId) {
    return apiClient.post(`/api/tournaments/${tournamentId}/game-days/${dayId}/finish`)
  },

  // Get public rankings for a tournament (no auth required)
  getPublicRankings (tournamentId) {
    return apiClient.get(`/api/tournaments/${tournamentId}/rankings`)
  }
}

export default apiClient
