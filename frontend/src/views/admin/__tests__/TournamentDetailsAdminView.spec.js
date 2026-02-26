import { shallowMount } from '@vue/test-utils'
import { createStore } from 'vuex'
import { createRouter, createWebHashHistory } from 'vue-router'
import TournamentDetailsAdminView from '@/views/admin/TournamentDetailsAdminView.vue'
import { tournamentAPI } from '@/services/api'

jest.mock('@/services/api', () => ({
  tournamentAPI: {
    getTournamentById: jest.fn(),
    getAvailableAdmins: jest.fn(),
    updateTournamentSettings: jest.fn()
  }
}))

const router = createRouter({
  history: createWebHashHistory(),
  routes: [{ path: '/admin/tournaments/:id', component: { template: '<div/>' } }]
})

function makeStore (isTournyAdmin = false) {
  return createStore({
    modules: {
      auth: {
        namespaced: true,
        getters: {
          isTournyAdmin: () => isTournyAdmin
        }
      }
    }
  })
}

function leagueTournament (overrides = {}) {
  return {
    id: 1,
    name: 'Test League',
    type: 'LEAGUE',
    enabled: true,
    ownerId: 10,
    adminIds: [],
    playerIds: [],
    admins: [],
    players: [],
    settings: { rankingLogic: 'MODIFIED_ELO', k: 32, absenteeDemerit: 10 },
    createdAt: null,
    updatedAt: null,
    ...overrides
  }
}

function oneOffTournament (overrides = {}) {
  return {
    id: 2,
    name: 'Weekend Cup',
    type: 'ONE_OFF',
    enabled: true,
    ownerId: 10,
    adminIds: [],
    playerIds: [],
    admins: [],
    players: [],
    settings: { numberOfRounds: 3, maxPoints: 21 },
    createdAt: null,
    updatedAt: null,
    ...overrides
  }
}

async function mountWithTournament (tournament, isTournyAdmin = false) {
  tournamentAPI.getTournamentById.mockResolvedValue({
    data: { success: true, tournament }
  })
  tournamentAPI.getAvailableAdmins.mockResolvedValue({
    data: { success: true, users: [] }
  })

  await router.push(`/admin/tournaments/${tournament.id}`)

  const wrapper = shallowMount(TournamentDetailsAdminView, {
    global: {
      plugins: [router, makeStore(isTournyAdmin)]
    }
  })

  // Wait for mounted() to resolve
  await wrapper.vm.$nextTick()
  await new Promise(resolve => setTimeout(resolve, 0))

  return wrapper
}

describe('TournamentDetailsAdminView', () => {
  beforeEach(() => jest.clearAllMocks())

  it('renders without errors (smoke test)', async () => {
    const wrapper = await mountWithTournament(leagueTournament())
    expect(wrapper.exists()).toBe(true)
  })

  it('shows settings card for LEAGUE tournament with k and absenteeDemerit', async () => {
    const wrapper = await mountWithTournament(leagueTournament())
    await wrapper.setData({ tournament: leagueTournament(), loading: false })
    const html = wrapper.html()
    expect(html).toContain('Tournament Settings')
    expect(html).toContain('K Factor')
    expect(html).toContain('32')
    expect(html).toContain('Absentee Demerit')
    expect(html).toContain('10')
  })

  it('shows settings card for ONE_OFF tournament with numberOfRounds and maxPoints', async () => {
    const wrapper = await mountWithTournament(oneOffTournament())
    await wrapper.setData({ tournament: oneOffTournament(), loading: false })
    const html = wrapper.html()
    expect(html).toContain('Tournament Settings')
    expect(html).toContain('Number of Rounds')
    expect(html).toContain('3')
    expect(html).toContain('Max Points')
    expect(html).toContain('21')
  })

  it('does not show settings card when tournament.settings is null', async () => {
    const t = leagueTournament({ settings: null })
    const wrapper = await mountWithTournament(t)
    await wrapper.setData({ tournament: t, loading: false })
    const html = wrapper.html()
    expect(html).not.toContain('Tournament Settings')
  })

  it('opens edit settings modal when Edit button is clicked', async () => {
    const wrapper = await mountWithTournament(leagueTournament())
    await wrapper.setData({ tournament: leagueTournament(), loading: false })
    expect(wrapper.vm.showEditSettingsModal).toBe(false)
    wrapper.vm.openEditSettingsModal()
    expect(wrapper.vm.showEditSettingsModal).toBe(true)
    expect(wrapper.vm.editSettings.k).toBe(32)
    expect(wrapper.vm.editSettings.absenteeDemerit).toBe(10)
  })

  it('saveSettings calls updateTournamentSettings with LEAGUE payload', async () => {
    const updatedTournament = leagueTournament({ settings: { rankingLogic: 'MODIFIED_ELO', k: 20, absenteeDemerit: 5 } })
    tournamentAPI.updateTournamentSettings.mockResolvedValue({
      data: { success: true, tournament: updatedTournament }
    })
    const wrapper = await mountWithTournament(leagueTournament())
    await wrapper.setData({
      tournament: leagueTournament(),
      loading: false,
      showEditSettingsModal: true,
      editSettings: { k: 20, absenteeDemerit: 5, numberOfRounds: null, maxPoints: null }
    })

    await wrapper.vm.saveSettings()

    expect(tournamentAPI.updateTournamentSettings).toHaveBeenCalledWith(1, { k: 20, absenteeDemerit: 5 })
    expect(wrapper.vm.showEditSettingsModal).toBe(false)
    expect(wrapper.vm.successMessage).toContain('updated')
  })

  it('saveSettings shows validation error for invalid k', async () => {
    const wrapper = await mountWithTournament(leagueTournament())
    await wrapper.setData({
      tournament: leagueTournament(),
      loading: false,
      editSettings: { k: 0, absenteeDemerit: 5, numberOfRounds: null, maxPoints: null }
    })

    await wrapper.vm.saveSettings()

    expect(tournamentAPI.updateTournamentSettings).not.toHaveBeenCalled()
    expect(wrapper.vm.editSettingsErrors.k).toBeTruthy()
  })

  it('saveSettings calls updateTournamentSettings with ONE_OFF payload', async () => {
    const updatedTournament = oneOffTournament({ settings: { numberOfRounds: 5, maxPoints: 15 } })
    tournamentAPI.updateTournamentSettings.mockResolvedValue({
      data: { success: true, tournament: updatedTournament }
    })
    const wrapper = await mountWithTournament(oneOffTournament())
    await wrapper.setData({
      tournament: oneOffTournament(),
      loading: false,
      editSettings: { k: null, absenteeDemerit: null, numberOfRounds: 5, maxPoints: 15 }
    })

    await wrapper.vm.saveSettings()

    expect(tournamentAPI.updateTournamentSettings).toHaveBeenCalledWith(2, { numberOfRounds: 5, maxPoints: 15 })
    expect(wrapper.vm.showEditSettingsModal).toBe(false)
  })
})
