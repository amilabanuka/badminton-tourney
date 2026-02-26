import { shallowMount } from '@vue/test-utils'
import { createRouter, createWebHashHistory } from 'vue-router'
import CreateTournamentView from '@/views/admin/CreateTournamentView.vue'
import { tournamentAPI } from '@/services/api'

jest.mock('@/services/api', () => ({
  tournamentAPI: {
    getAvailableAdmins: jest.fn(),
    createTournament: jest.fn()
  }
}))

const router = createRouter({
  history: createWebHashHistory(),
  routes: [{ path: '/', component: { template: '<div/>' } }]
})

function mountView () {
  tournamentAPI.getAvailableAdmins.mockResolvedValue({
    data: { success: true, users: [{ id: 1, firstName: 'Alice', lastName: 'Admin', email: 'alice@test.com' }] }
  })
  return shallowMount(CreateTournamentView, {
    global: { plugins: [router] }
  })
}

describe('CreateTournamentView', () => {
  beforeEach(() => jest.clearAllMocks())

  it('renders without errors (smoke test)', () => {
    const wrapper = mountView()
    expect(wrapper.exists()).toBe(true)
  })

  it('does not show settings section when no type is selected', () => {
    const wrapper = mountView()
    expect(wrapper.find('#k').exists()).toBe(false)
    expect(wrapper.find('#numberOfRounds').exists()).toBe(false)
  })

  it('shows league settings when type is LEAGUE', async () => {
    const wrapper = mountView()
    await wrapper.setData({ form: { ...wrapper.vm.form, type: 'LEAGUE' } })
    expect(wrapper.find('#k').exists()).toBe(true)
    expect(wrapper.find('#absenteeDemerit').exists()).toBe(true)
    expect(wrapper.find('#numberOfRounds').exists()).toBe(false)
  })

  it('shows one-off settings when type is ONE_OFF', async () => {
    const wrapper = mountView()
    await wrapper.setData({ form: { ...wrapper.vm.form, type: 'ONE_OFF' } })
    expect(wrapper.find('#numberOfRounds').exists()).toBe(true)
    expect(wrapper.find('#maxPoints').exists()).toBe(true)
    expect(wrapper.find('#k').exists()).toBe(false)
  })

  it('clears settings fields when type changes', async () => {
    const wrapper = mountView()
    await wrapper.setData({ form: { ...wrapper.vm.form, type: 'LEAGUE', k: 32, absenteeDemerit: 10 } })
    await wrapper.setData({ form: { ...wrapper.vm.form, type: 'ONE_OFF' } })
    // The watch fires when type changes; simulate it manually
    wrapper.vm.$options.watch['form.type'].call(wrapper.vm)
    expect(wrapper.vm.form.k).toBe('')
    expect(wrapper.vm.form.absenteeDemerit).toBe('')
  })

  it('validates LEAGUE settings — rejects k=0', async () => {
    const wrapper = mountView()
    await wrapper.setData({
      form: {
        name: 'My League',
        ownerId: 1,
        enabled: true,
        type: 'LEAGUE',
        k: 0,
        absenteeDemerit: 5,
        numberOfRounds: '',
        maxPoints: ''
      }
    })
    const valid = wrapper.vm.validateForm()
    expect(valid).toBe(false)
    expect(wrapper.vm.errors.k).toBeTruthy()
    expect(tournamentAPI.createTournament).not.toHaveBeenCalled()
  })

  it('validates LEAGUE settings — rejects negative absenteeDemerit', async () => {
    const wrapper = mountView()
    await wrapper.setData({
      form: {
        name: 'My League',
        ownerId: 1,
        enabled: true,
        type: 'LEAGUE',
        k: 32,
        absenteeDemerit: -1,
        numberOfRounds: '',
        maxPoints: ''
      }
    })
    const valid = wrapper.vm.validateForm()
    expect(valid).toBe(false)
    expect(wrapper.vm.errors.absenteeDemerit).toBeTruthy()
  })

  it('validates ONE_OFF settings — rejects invalid maxPoints', async () => {
    const wrapper = mountView()
    await wrapper.setData({
      form: {
        name: 'My One-off',
        ownerId: 1,
        enabled: true,
        type: 'ONE_OFF',
        k: '',
        absenteeDemerit: '',
        numberOfRounds: 3,
        maxPoints: 30
      }
    })
    const valid = wrapper.vm.validateForm()
    expect(valid).toBe(false)
    expect(wrapper.vm.errors.maxPoints).toBeTruthy()
  })

  it('validates ONE_OFF settings — rejects zero rounds', async () => {
    const wrapper = mountView()
    await wrapper.setData({
      form: {
        name: 'My One-off',
        ownerId: 1,
        enabled: true,
        type: 'ONE_OFF',
        k: '',
        absenteeDemerit: '',
        numberOfRounds: 0,
        maxPoints: 21
      }
    })
    const valid = wrapper.vm.validateForm()
    expect(valid).toBe(false)
    expect(wrapper.vm.errors.numberOfRounds).toBeTruthy()
  })

  it('submits LEAGUE tournament with correct leagueSettings payload', async () => {
    tournamentAPI.createTournament.mockResolvedValue({
      data: { success: true, tournament: { id: 1 } }
    })
    const wrapper = mountView()
    // Set type+name+ownerId first so the watcher fires and resets settings fields
    await wrapper.setData({
      form: { name: 'My League', ownerId: 1, enabled: true, type: 'LEAGUE', k: '', absenteeDemerit: '', numberOfRounds: '', maxPoints: '' }
    })
    // setData awaits nextTick, so the watcher has already run and reset k/absenteeDemerit
    // Now set settings fields directly — watcher won't fire again (type didn't change)
    wrapper.vm.form.k = 32
    wrapper.vm.form.absenteeDemerit = 10
    await wrapper.vm.submitForm()
    expect(tournamentAPI.createTournament).toHaveBeenCalledWith(
      expect.objectContaining({
        name: 'My League',
        type: 'LEAGUE',
        leagueSettings: { rankingLogic: 'MODIFIED_ELO', k: 32, absenteeDemerit: 10 }
      })
    )
    expect(tournamentAPI.createTournament).toHaveBeenCalledWith(
      expect.not.objectContaining({ oneOffSettings: expect.anything() })
    )
  })

  it('submits ONE_OFF tournament with correct oneOffSettings payload', async () => {
    tournamentAPI.createTournament.mockResolvedValue({
      data: { success: true, tournament: { id: 2 } }
    })
    const wrapper = mountView()
    // Set type+name+ownerId first so the watcher fires and resets settings fields
    await wrapper.setData({
      form: { name: 'Weekend Cup', ownerId: 1, enabled: true, type: 'ONE_OFF', k: '', absenteeDemerit: '', numberOfRounds: '', maxPoints: '' }
    })
    // setData awaits nextTick, so the watcher has already run and reset numberOfRounds/maxPoints
    // Now set settings fields directly — watcher won't fire again (type didn't change)
    wrapper.vm.form.numberOfRounds = 3
    wrapper.vm.form.maxPoints = 21
    await wrapper.vm.submitForm()
    expect(tournamentAPI.createTournament).toHaveBeenCalledWith(
      expect.objectContaining({
        name: 'Weekend Cup',
        type: 'ONE_OFF',
        oneOffSettings: { numberOfRounds: 3, maxPoints: 21 }
      })
    )
    expect(tournamentAPI.createTournament).toHaveBeenCalledWith(
      expect.not.objectContaining({ leagueSettings: expect.anything() })
    )
  })
})
