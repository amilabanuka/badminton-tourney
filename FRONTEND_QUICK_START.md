# Frontend Quick Start Guide

## 🚀 Running the Application

### Prerequisites
- Node.js installed
- Backend server running on port 8098
- MySQL database configured

### Start Development Server
```bash
cd E:\learn\badminton-app\frontend
npm run serve
```

Application will be available at: **http://localhost:8080**

---

## 🔑 Default Test Users

Create these users for testing different roles:

### ADMIN User
```json
{
  "username": "admin",
  "email": "admin@test.com",
  "password": "password",
  "firstName": "Admin",
  "lastName": "User"
}
```
*(Manually set role to ADMIN in database)*

### TOURNY_ADMIN User
```json
{
  "username": "organizer",
  "email": "organizer@test.com",
  "password": "password",
  "firstName": "Tournament",
  "lastName": "Organizer"
}
```
*(Manually set role to TOURNY_ADMIN in database)*

### PLAYER User
```json
{
  "username": "player",
  "email": "player@test.com",
  "password": "password",
  "firstName": "John",
  "lastName": "Player"
}
```
*(Automatically assigned PLAYER role on signup)*

---

## 📱 Available Routes

### Public Routes
- `/` - Home page
- `/signup` - User registration
- `/login` - User login
- `/about` - About page

### Protected Routes (Require Authentication)
- `/dashboard` - User dashboard
- `/tournaments` - Tournament list
- `/tournaments/:id` - Tournament details

### Admin Routes (Require ADMIN Role)
- `/admin/tournaments/create` - Create new tournament

---

## 🎨 UI Features

### Role Badges
- **ADMIN** - Red badge
- **TOURNY_ADMIN** - Blue badge
- **PLAYER** - Green badge

### Navigation
- Role-based menu items
- User dropdown with logout
- Mobile responsive hamburger menu

### Forms
- Real-time validation
- Loading states
- Error messages
- Success messages

---

## 🔧 Development Tips

### Vuex Store
```javascript
// Access current user
this.$store.getters['auth/currentUser']

// Check if admin
this.$store.getters['auth/isAdmin']

// Login
await this.$store.dispatch('auth/login', { username, password })

// Logout
this.$store.dispatch('auth/logout')
```

### Router Navigation
```javascript
// Programmatic navigation
this.$router.push('/dashboard')

// With router-link
<router-link to="/tournaments">Tournaments</router-link>
```

### API Calls
```javascript
import apiClient from '@/services/api'

// All requests automatically include HTTP Basic Auth
const response = await apiClient.get('/api/tournaments')
```

---

## 🐛 Troubleshooting

### Issue: "Cannot connect to backend"
**Solution:** Ensure backend is running on http://localhost:8098

### Issue: "401 Unauthorized"
**Solution:** Check credentials in localStorage or try logging in again

### Issue: "Route not found"
**Solution:** Use hash-based routing: `http://localhost:8080/#/signup`

### Issue: "Bootstrap icons not showing"
**Solution:** Check internet connection (icons loaded from CDN)

---

## 📂 Project Structure

```
frontend/src/
├── services/          # API services
│   ├── api.js        # Axios instance
│   └── authService.js # Auth API calls
├── store/            # Vuex store
│   ├── index.js      # Root store
│   └── modules/
│       └── auth.js   # Auth state
├── router/           # Vue Router
│   └── index.js      # Route definitions
├── views/            # Page components
│   ├── SignUpView.vue
│   ├── LoginView.vue
│   ├── DashboardView.vue
│   └── admin/
│       └── CreateTournamentView.vue
├── components/       # Reusable components
│   └── layout/
│       └── NavBar.vue
└── assets/          # Static assets
```

---

## 🔄 Phase Status

- ✅ **Phase 1:** Authentication (COMPLETE)
- ⏳ **Phase 2:** Tournament Viewing (PENDING)
- ⏳ **Phase 3:** Tournament Management (PENDING)
- ⏳ **Phase 4:** Polish & Testing (PENDING)

---

## 📞 Need Help?

Check documentation files:
- `FRONTEND_PHASE1_COMPLETE.md` - Phase 1 summary
- `plan-frontendImplementation.prompt.md` - Complete plan
- Backend docs in `TOURNAMENT_API_DOCUMENTATION.md`

---

*Last Updated: February 23, 2026*

