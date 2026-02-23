import authService from '@/services/authService'

const state = {
  user: authService.getCurrentUser(),
  isAuthenticated: authService.isAuthenticated()
}

const getters = {
  currentUser: (state) => state.user,
  isAuthenticated: (state) => state.isAuthenticated,
  isAdmin: (state) => state.user?.role === 'ADMIN',
  isTournyAdmin: (state) => state.user?.role === 'TOURNY_ADMIN',
  isPlayer: (state) => state.user?.role === 'PLAYER',
  userRole: (state) => state.user?.role || null
}

const mutations = {
  SET_USER (state, user) {
    state.user = user
    state.isAuthenticated = !!user
  },
  CLEAR_USER (state) {
    state.user = null
    state.isAuthenticated = false
  }
}

const actions = {
  async signup ({ commit }, userData) {
    const response = await authService.signup(userData)
    return response
  },

  async login ({ commit }, credentials) {
    const response = await authService.login(credentials)
    if (response.success && response.user) {
      commit('SET_USER', response.user)
    }
    return response
  },

  logout ({ commit }) {
    authService.logout()
    commit('CLEAR_USER')
  },

  checkAuth ({ commit }) {
    const user = authService.getCurrentUser()
    if (user) {
      commit('SET_USER', user)
    } else {
      commit('CLEAR_USER')
    }
  }
}

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
}
