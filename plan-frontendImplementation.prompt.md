# Frontend Implementation Plan - Authentication & Tournament Management
## Plan: Build Vue.js Frontend for Sign Up, Login, and Tournament Management
Complete frontend implementation for user authentication (sign up, login) and tournament management with role-based UI for ADMIN and TOURNY_ADMIN users.
## Executive Summary
Build a Vue.js 3 frontend application with the following features:
- User authentication (sign up, login, logout)
- Role-based navigation and UI components
- Tournament creation and management (ADMIN only)
- Player management (ADMIN and TOURNY_ADMIN)
- Admin management (ADMIN only)
- Tournament listing and viewing (all authenticated users)
**Tech Stack:** Vue 3 Composition API, Vue Router, Axios, Bootstrap 5, Vuex (for state management)
## Steps
### 1. Setup Authentication Infrastructure
**1.1 Create Authentication Service** (`src/services/authService.js`)
- Implement API calls for signup and login using Axios
- Handle HTTP Basic Auth encoding
- Store user credentials and role in localStorage
- Provide methods: `signup()`, `login()`, `logout()`, `getCurrentUser()`, `isAuthenticated()`, `hasRole()`
**1.2 Create Vuex Store for Authentication** (`src/store/index.js`)
- State: `user` (username, email, firstName, lastName, role), `isAuthenticated`
- Actions: `signup`, `login`, `logout`, `checkAuth`
- Mutations: `SET_USER`, `CLEAR_USER`
- Getters: `currentUser`, `isAdmin`, `isTournyAdmin`, `isPlayer`, `isAuthenticated`
**1.3 Create Route Guards** (`src/router/index.js`)
- `beforeEach` navigation guard to check authentication
- Redirect to login if not authenticated
- Check role-based access for protected routes
### 2. Build Authentication Pages
**2.1 Sign Up Page** (`src/views/SignUpView.vue`)
- Form fields: username, email, password, confirmPassword, firstName, lastName, role (dropdown)
- Validation: required fields, email format, password min 6 chars, password match
- Role dropdown: PLAYER (default), TOURNY_ADMIN, ADMIN
- Submit to `/api/auth/signup`
- On success: redirect to login with success message
- On error: display error message
- Already have account? Link to login
**2.2 Login Page** (`src/views/LoginView.vue`)
- Form fields: username, password
- Submit to `/api/auth/login`
- On success: store user in Vuex, redirect to dashboard based on role
- On error: display error message
- Don''t have account? Link to sign up
**2.3 Dashboard Page** (`src/views/DashboardView.vue`)
- Welcome message with user''s name and role
- Role-based quick actions:
  - ADMIN: "Create Tournament", "Manage Tournaments", "View All Tournaments"
  - TOURNY_ADMIN: "Manage My Tournaments", "View All Tournaments"
  - PLAYER: "View Tournaments", "My Tournaments"
- Summary cards: Total tournaments, My tournaments (if TOURNY_ADMIN), Upcoming events
### 3. Build Tournament Service Layer
**3.1 Create Tournament Service** (`src/services/tournamentService.js`)
- API methods with HTTP Basic Auth headers:
  - `getAvailableAdmins()` - GET /api/tournaments/admins/available
  - `createTournament(data)` - POST /api/tournaments
  - `addTournamentAdmin(tournamentId, userId)` - POST /api/tournaments/{id}/admins
  - `addTournamentPlayer(tournamentId, userId)` - POST /api/tournaments/{id}/players
  - `removeAdmin(tournamentId, userId)` - DELETE /api/tournaments/{id}/admins/{userId}
  - `removePlayer(tournamentId, userId)` - DELETE /api/tournaments/{id}/players/{userId}
  - `getAllTournaments()` - GET /api/tournaments
  - `getTournamentById(id)` - GET /api/tournaments/{id}
**3.2 Create Vuex Store for Tournaments** (`src/store/modules/tournaments.js`)
- State: `tournaments`, `currentTournament`, `availableAdmins`, `loading`, `error`
- Actions: `fetchTournaments`, `fetchTournamentById`, `createTournament`, `addAdmin`, `addPlayer`, etc.
- Mutations: `SET_TOURNAMENTS`, `SET_CURRENT_TOURNAMENT`, `SET_AVAILABLE_ADMINS`, etc.
- Getters: `allTournaments`, `tournamentById`, `myTournaments` (filter by ownerId)
### 4. Build Tournament Management Pages (ADMIN Only)
**4.1 Create Tournament Page** (`src/views/admin/CreateTournamentView.vue`)
- Access: ADMIN only
- Form fields:
  - Tournament name (text input, required, unique validation)
  - Owner (dropdown populated from available TOURNY_ADMIN users)
  - Enabled (checkbox, default true)
- Load available admins on mount via `getAvailableAdmins()`
- Submit creates tournament
- On success: redirect to tournament details page
- On error: display validation errors
**4.2 Tournament List Page** (`src/views/TournamentListView.vue`)
- Access: All authenticated users
- Table/card view of all tournaments showing:
  - Tournament name
  - Owner name (fetch from users)
  - Status (enabled/disabled badge)
  - Number of admins and players
  - Created date
  - Action buttons (role-based)
- ADMIN actions: Edit, View Details, Manage Admins, Manage Players
- TOURNY_ADMIN actions: View Details, Manage Players (if owner or admin)
- PLAYER actions: View Details
- Search/filter by name, owner, status
- Pagination (10 per page)
**4.3 Tournament Details Page** (`src/views/TournamentDetailsView.vue`)
- Access: All authenticated users
- Display tournament information:
  - Tournament name, owner, status, dates
  - Admins list (names, emails) with remove button (ADMIN only)
  - Players list (names, emails) with remove button (ADMIN/TOURNY_ADMIN)
- Role-based action buttons at top:
  - ADMIN: "Add Admin", "Add Player", "Edit Tournament"
  - TOURNY_ADMIN: "Add Player" (if owner or admin)
- Tabs: Overview, Admins, Players, Statistics (future)
### 5. Build Admin Management Components
**5.1 Add Tournament Admin Modal** (`src/components/tournaments/AddAdminModal.vue`)
- Access: ADMIN only
- Modal with dropdown to select user with TOURNY_ADMIN role
- Fetch available TOURNY_ADMIN users (exclude already added)
- Submit adds admin to tournament
- On success: refresh admins list, close modal
- On error: display error message
**5.2 Admin List Component** (`src/components/tournaments/AdminList.vue`)
- Display list of tournament admins (cards or table)
- Show: avatar/icon, name, email, added date
- Remove button (ADMIN only) with confirmation dialog
- Empty state: "No admins added yet"
### 6. Build Player Management Components
**6.1 Add Tournament Player Modal** (`src/components/tournaments/AddPlayerModal.vue`)
- Access: ADMIN and TOURNY_ADMIN
- Modal with searchable dropdown to select user with PLAYER role
- Fetch available PLAYER users (exclude already added)
- Search functionality for large player lists
- Submit adds player to tournament
- On success: refresh players list, close modal
- On error: display error message
**6.2 Player List Component** (`src/components/tournaments/PlayerList.vue`)
- Display list of tournament players (cards or table)
- Show: avatar/icon, name, email, added date
- Remove button (ADMIN/TOURNY_ADMIN) with confirmation dialog
- Bulk actions: Add multiple players (future enhancement)
- Empty state: "No players added yet. Start by adding players."
### 7. Build Shared Components
**7.1 Navigation Bar Component** (`src/components/layout/NavBar.vue`)
- Replace existing navbar in App.vue
- Logo and app name
- Role-based navigation links:
  - All: Home, Tournaments
  - ADMIN: Create Tournament, Manage Users (future)
  - Authenticated: Dashboard, Profile
  - Guest: Sign Up, Login
- User dropdown menu (when logged in):
  - Display name and role badge
  - Profile, Settings, Logout
- Mobile responsive hamburger menu
**7.2 Auth Guard Component** (`src/components/common/AuthGuard.vue`)
- Wrapper component to check role-based access
- Props: `requiredRoles` (array)
- If user doesn''t have required role, show "Access Denied" message
- Usage: `<AuthGuard :requiredRoles="[''ADMIN'']"><CreateTournament /></AuthGuard>`
**7.3 Loading Spinner Component** (`src/components/common/LoadingSpinner.vue`)
- Reusable loading spinner for API calls
- Overlay and inline variants
- Bootstrap spinner with custom branding
**7.4 Confirmation Dialog Component** (`src/components/common/ConfirmDialog.vue`)
- Reusable confirmation modal
- Props: `title`, `message`, `confirmText`, `cancelText`, `variant` (danger/warning/info)
- Emits: `confirm`, `cancel`
- Usage: Before deleting admin/player
**7.5 Alert/Toast Component** (`src/components/common/AlertMessage.vue`)
- Display success/error/warning messages
- Auto-dismiss after 5 seconds
- Bootstrap alert variants
- Can be triggered from Vuex store
### 8. Update Router Configuration
**8.1 Add Routes** (`src/router/index.js`)
- Public routes:
  - `/signup` - SignUpView
  - `/login` - LoginView
- Protected routes (require authentication):
  - `/dashboard` - DashboardView
  - `/tournaments` - TournamentListView
  - `/tournaments/:id` - TournamentDetailsView
- Admin-only routes:
  - `/admin/tournaments/create` - CreateTournamentView
- Redirect `/` to `/dashboard` if authenticated, else `/login`
**8.2 Navigation Guards**
- Global `beforeEach` guard:
  - Check if route requires auth
  - Check if user is authenticated
  - Check if user has required role
  - Redirect to login if not authenticated
  - Redirect to dashboard if insufficient permissions
- Store last attempted route to redirect after login
### 9. Implement State Management
**9.1 Vuex Store Structure** (`src/store/`)
```
store/
  ├── index.js          (root store with modules)
  ├── modules/
  │   ├── auth.js       (user authentication state)
  │   └── tournaments.js (tournament data state)
```
**9.2 Auth Module** (`src/store/modules/auth.js`)
- Persist auth state to localStorage
- Restore state on app load
- Clear state on logout
- Provide computed role checks
**9.3 Tournaments Module** (`src/store/modules/tournaments.js`)
- Cache tournament list
- Track loading states
- Handle API errors gracefully
- Optimistic updates for better UX
### 10. Styling and UX Enhancements
**10.1 Custom Styles** (`src/assets/styles/main.css`)
- Color scheme: Primary (blue), Success (green), Danger (red), Warning (yellow)
- Role badges: ADMIN (red), TOURNY_ADMIN (blue), PLAYER (green)
- Card styles for tournaments and users
- Form validation states
- Responsive breakpoints
**10.2 Form Validation**
- Client-side validation before API calls
- Real-time validation feedback
- Display server-side validation errors
- Disable submit during API calls
**10.3 UX Improvements**
- Loading states during API calls
- Success notifications after actions
- Confirmation dialogs for destructive actions
- Optimistic UI updates
- Error boundaries for graceful error handling
- Empty states with helpful messages
- Skeleton loaders while fetching data
## File Structure
```
frontend/src/
├── App.vue                           (updated with NavBar)
├── main.js                           (updated with Vuex)
├── router/
│   └── index.js                      (updated with all routes + guards)
├── store/
│   ├── index.js                      (NEW - Vuex root store)
│   └── modules/
│       ├── auth.js                   (NEW - auth state)
│       └── tournaments.js            (NEW - tournament state)
├── services/
│   ├── authService.js                (NEW - auth API calls)
│   ├── tournamentService.js          (NEW - tournament API calls)
│   └── api.js                        (NEW - Axios instance with interceptors)
├── views/
│   ├── SignUpView.vue                (NEW - sign up page)
│   ├── LoginView.vue                 (NEW - login page)
│   ├── DashboardView.vue             (NEW - user dashboard)
│   ├── TournamentListView.vue        (NEW - list all tournaments)
│   ├── TournamentDetailsView.vue     (NEW - tournament details)
│   └── admin/
│       └── CreateTournamentView.vue  (NEW - create tournament - ADMIN only)
├── components/
│   ├── layout/
│   │   └── NavBar.vue                (NEW - navigation bar)
│   ├── common/
│   │   ├── AuthGuard.vue             (NEW - role-based access wrapper)
│   │   ├── LoadingSpinner.vue        (NEW - loading spinner)
│   │   ├── ConfirmDialog.vue         (NEW - confirmation modal)
│   │   └── AlertMessage.vue          (NEW - toast notifications)
│   └── tournaments/
│       ├── AddAdminModal.vue         (NEW - add admin modal)
│       ├── AdminList.vue             (NEW - display admins)
│       ├── AddPlayerModal.vue        (NEW - add player modal)
│       └── PlayerList.vue            (NEW - display players)
├── assets/
│   └── styles/
│       └── main.css                  (NEW - custom styles)
└── utils/
    ├── validators.js                 (NEW - form validators)
    └── constants.js                  (NEW - app constants, API URLs)
```
## API Integration Details
### HTTP Basic Auth Headers
All API calls must include HTTP Basic Auth headers:
```javascript
const auth = btoa(`${username}:${password}`);
headers: { ''Authorization'': `Basic ${auth}` }
```
### Store Credentials Securely
- Store username and password in localStorage (encrypted in production)
- Include auth header in every API request via Axios interceptor
- Clear credentials on logout
### Error Handling
- 401 Unauthorized → Redirect to login, clear stored credentials
- 403 Forbidden → Show "Access Denied" message
- 400 Bad Request → Display validation errors from server
- 500 Server Error → Show generic error message
## User Flows
### Sign Up Flow
1. User visits `/signup`
2. Fills form (username, email, password, confirmPassword, firstName, lastName, role)
3. Client validates input
4. Submit to `/api/auth/signup`
5. On success: Show success message, redirect to `/login`
6. On error: Display error (username/email already exists)
### Login Flow
1. User visits `/login`
2. Enters username and password
3. Submit to `/api/auth/login`
4. On success:
   - Store user data and credentials in Vuex + localStorage
   - Redirect to `/dashboard`
5. On error: Display error (invalid credentials)
### Create Tournament Flow (ADMIN)
1. Admin visits `/admin/tournaments/create`
2. System fetches available TOURNY_ADMIN users
3. Admin fills form (name, owner, enabled)
4. Submit to `/api/tournaments`
5. On success: Redirect to `/tournaments/{id}` (tournament details)
6. On error: Display validation errors
### Add Tournament Admin Flow (ADMIN)
1. Admin on tournament details page clicks "Add Admin"
2. Modal opens with dropdown of TOURNY_ADMIN users
3. Admin selects user and submits
4. API call to `/api/tournaments/{id}/admins`
5. On success: Refresh admins list, close modal, show success toast
6. On error: Display error in modal
### Add Tournament Player Flow (ADMIN/TOURNY_ADMIN)
1. User on tournament details page clicks "Add Player"
2. Modal opens with searchable dropdown of PLAYER users
3. User searches and selects player
4. Submit to `/api/tournaments/{id}/players`
5. On success: Refresh players list, close modal, show success toast
6. On error: Display error in modal
## Component Specifications
### NavBar Component
**Props:** None  
**Computed:**
- `isAuthenticated` - from Vuex
- `currentUser` - from Vuex
- `isAdmin`, `isTournyAdmin` - from Vuex getters
**Features:**
- Conditional rendering based on auth state
- Role-based navigation items
- User dropdown with logout
- Mobile responsive
### CreateTournamentView
**Route:** `/admin/tournaments/create`  
**Guard:** ADMIN only  
**Data:**
- `form: { name, ownerId, enabled }`
- `availableAdmins: []`
- `loading: false`
- `errors: {}`
**Methods:**
- `loadAvailableAdmins()` - Fetch TOURNY_ADMIN users
- `validateForm()` - Client-side validation
- `createTournament()` - Submit form
- `handleError()` - Display errors
### TournamentDetailsView
**Route:** `/tournaments/:id`  
**Guard:** Authenticated  
**Data:**
- `tournament: null`
- `admins: []`
- `players: []`
- `loading: true`
- `activeTab: ''overview''`
**Computed:**
- `canAddAdmin` - user is ADMIN
- `canAddPlayer` - user is ADMIN or TOURNY_ADMIN
- `canRemoveAdmin` - user is ADMIN
- `canRemovePlayer` - user is ADMIN or (TOURNY_ADMIN and is admin of tournament)
**Methods:**
- `loadTournament()` - Fetch tournament data
- `loadAdmins()` - Fetch admin list
- `loadPlayers()` - Fetch player list
- `showAddAdminModal()` - Open add admin modal
- `showAddPlayerModal()` - Open add player modal
- `removeAdmin(userId)` - Remove admin with confirmation
- `removePlayer(userId)` - Remove player with confirmation
### AddPlayerModal Component
**Props:**
- `tournamentId: Number`
- `existingPlayerIds: Array`
**Emits:**
- `player-added` - when player successfully added
**Data:**
- `availablePlayers: []`
- `selectedPlayerId: null`
- `loading: false`
- `searchQuery: ''''`
**Computed:**
- `filteredPlayers` - filter by search query and exclude existing
**Methods:**
- `loadPlayers()` - Fetch all PLAYER users
- `filterPlayers()` - Search functionality
- `addPlayer()` - Submit to API
- `resetForm()` - Clear form and close
## Security Considerations
1. **Client-Side Validation**
   - Validate all inputs before API calls
   - Don''t trust client-side checks alone
2. **Role-Based Rendering**
   - Hide UI elements based on user role
   - Server enforces actual permissions
3. **Secure Credential Storage**
   - Use localStorage with caution (encrypted in production)
   - Consider using HttpOnly cookies for tokens in production
4. **CSRF Protection**
   - Include CSRF token if backend requires it
5. **XSS Prevention**
   - Sanitize all user inputs before rendering
   - Vue automatically escapes {{ }} bindings
## Responsive Design
- **Desktop (≥992px):** Full navigation, side-by-side layouts
- **Tablet (768px-991px):** Stacked layouts, hamburger menu
- **Mobile (<768px):** Single column, simplified UI, bottom navigation
## Accessibility
- Semantic HTML5 elements
- ARIA labels for screen readers
- Keyboard navigation support
- Focus states on interactive elements
- Color contrast ratios meet WCAG AA standards
- Form labels properly associated
## Testing Strategy
### Unit Tests (Jest + Vue Test Utils)
- Test each component in isolation
- Mock API calls and Vuex store
- Test user interactions and events
- Test computed properties and watchers
### Integration Tests
- Test router navigation
- Test Vuex store actions and mutations
- Test API service layer
### E2E Tests (Cypress - Future)
- Test complete user flows
- Test authentication flow
- Test tournament creation and management
## Performance Optimization
1. **Lazy Loading**
   - Route-level code splitting
   - Load components on demand
2. **Caching**
   - Cache tournament list in Vuex
   - Implement smart refresh strategies
3. **Debouncing**
   - Search inputs debounced (300ms)
   - API calls throttled
4. **Pagination**
   - Load tournaments in pages (10 per page)
   - Virtual scrolling for large lists
## Dependencies to Install
```bash
npm install vuex@next          # State management
npm install axios              # Already installed - HTTP client
npm install @vuelidate/core    # Form validation (optional)
npm install @vuelidate/validators
```
## Environment Configuration
**`.env.development`**
```
VUE_APP_API_BASE_URL=http://localhost:8098
VUE_APP_API_TIMEOUT=10000
```
**`.env.production`**
```
VUE_APP_API_BASE_URL=https://api.badminton-manager.com
VUE_APP_API_TIMEOUT=10000
```
## Implementation Order
### Phase 1: Authentication (Days 1-2)
1. Install Vuex
2. Create auth service and Vuex module
3. Build SignUpView and LoginView
4. Update router with guards
5. Build NavBar component
6. Test authentication flow
### Phase 2: Basic Tournament Features (Days 3-4)
7. Create tournament service and Vuex module
8. Build TournamentListView
9. Build TournamentDetailsView
10. Build DashboardView
11. Test tournament viewing
### Phase 3: Tournament Management (Days 5-6)
12. Build CreateTournamentView (ADMIN)
13. Build AddAdminModal and AdminList components
14. Build AddPlayerModal and PlayerList components
15. Implement add/remove functionality
16. Test complete tournament management flow
### Phase 4: Polish & Testing (Day 7)
17. Add loading states and error handling
18. Implement confirmation dialogs
19. Add toast notifications
20. Responsive design refinements
21. Cross-browser testing
22. Write unit tests
## Success Criteria
- ✅ Users can sign up with role selection
- ✅ Users can log in with username/password
- ✅ ADMIN users can create tournaments
- ✅ ADMIN users can add/remove tournament admins
- ✅ ADMIN and TOURNY_ADMIN can add/remove players
- ✅ All authenticated users can view tournaments
- ✅ Navigation is role-based
- ✅ All API calls include proper authentication
- ✅ Error handling is comprehensive
- ✅ UI is responsive across devices
- ✅ No console errors or warnings
## Future Enhancements
1. Profile management page
2. User search and filtering
3. Bulk player import (CSV)
4. Tournament statistics dashboard
5. Real-time updates (WebSocket)
6. Tournament brackets and matches
7. Email notifications
8. Dark mode toggle
9. Internationalization (i18n)
10. PWA features (offline support)
## Notes
- Use Vue 3 Composition API for new components (modern approach)
- Follow Vue.js style guide for consistency
- Use Bootstrap 5 utilities for rapid development
- Keep components small and focused (single responsibility)
- Extract reusable logic into composables
- Document complex logic with comments
- Use TypeScript for better type safety (optional future enhancement)
