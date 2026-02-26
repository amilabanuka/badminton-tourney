import { createRouter, createWebHashHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import store from '../store'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/about',
    name: 'about',
    component: () => import('../views/AboutView.vue')
  },
  {
    path: '/signup',
    name: 'signup',
    component: () => import('../views/SignUpView.vue'),
    meta: { guest: true }
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { guest: true }
  },
  {
    path: '/dashboard',
    name: 'dashboard',
    component: () => import('../views/DashboardView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/tournaments',
    name: 'tournaments',
    component: () => import('../views/TournamentListView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/tournaments/:id',
    name: 'tournament-details',
    component: () => import('../views/TournamentDetailsView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin/tournaments',
    name: 'admin-tournaments',
    component: () => import('../views/admin/AdminTournamentsListView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/tournaments/create',
    name: 'create-tournament',
    component: () => import('../views/admin/CreateTournamentView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true, requiresSuperAdmin: true }
  },
  {
    path: '/admin/tournaments/:id',
    name: 'admin-tournament-details',
    component: () => import('../views/admin/TournamentDetailsAdminView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/tournaments/:id/game-days/new',
    name: 'create-league-day',
    component: () => import('../views/admin/CreateLeagueDayView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/tournaments/:id/game-days/:dayId',
    name: 'league-day-view',
    component: () => import('../views/admin/LeagueDayView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/tournaments/:id/rankings',
    name: 'tournament-rankings',
    component: () => import('../views/TournamentRankingsView.vue')
    // no meta.requiresAuth — publicly accessible without login
  },
  {
    path: '/tournaments/:id/player-view',
    name: 'player-tournament-view',
    component: () => import('../views/PlayerTournamentView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/tournaments/:id/game-days/:dayId',
    name: 'player-league-day-view',
    component: () => import('../views/PlayerLeagueDayView.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// Navigation guard
router.beforeEach((to, from, next) => {
  // Check authentication from store
  store.dispatch('auth/checkAuth')

  const isAuthenticated = store.getters['auth/isAuthenticated']
  const isAdmin = store.getters['auth/isAdmin']
  const isTournyAdmin = store.getters['auth/isTournyAdmin']
  const isPlayer = store.getters['auth/isPlayer']

  // Redirect authenticated admins and tourny admins from home/dashboard to admin panel
  if (isAuthenticated && (isAdmin || isTournyAdmin) && (to.path === '/' || to.path === '/dashboard')) {
    next('/admin/tournaments')
    return
  }

  // Redirect authenticated players from home/dashboard to tournaments list
  if (isAuthenticated && isPlayer && (to.path === '/' || to.path === '/dashboard')) {
    next('/tournaments')
    return
  }

  // Redirect authenticated non-admin users away from guest pages
  if (to.meta.guest && isAuthenticated && !isAdmin && !isTournyAdmin) {
    next('/dashboard')
    return
  }

  // Check if route requires authentication
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
    return
  }

  // Superadmin-only routes (e.g. create tournament) - TOURNY_ADMIN not allowed
  if (to.meta.requiresSuperAdmin && !isAdmin) {
    next('/admin/tournaments')
    return
  }

  // Check if route requires admin role (ADMIN or TOURNY_ADMIN) — block players
  if (to.meta.requiresAdmin && !isAdmin && !isTournyAdmin) {
    next('/tournaments')
    return
  }

  next()
})

export default router
