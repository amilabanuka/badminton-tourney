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
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/tournaments/:id',
    name: 'admin-tournament-details',
    component: () => import('../views/admin/AdminTournamentDetailsView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
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

  // Redirect authenticated admins from home/dashboard to admin panel
  if (isAuthenticated && isAdmin && (to.path === '/' || to.path === '/dashboard')) {
    next('/admin/tournaments')
    return
  }

  // Redirect to dashboard if authenticated non-admin user tries to access guest pages
  if (to.meta.guest && isAuthenticated && !isAdmin) {
    next('/dashboard')
    return
  }

  // Check if route requires authentication
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
    return
  }

  // Check if route requires admin role
  if (to.meta.requiresAdmin && !isAdmin) {
    next('/dashboard')
    return
  }

  next()
})

export default router
