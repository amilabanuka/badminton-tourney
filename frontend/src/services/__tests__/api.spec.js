import apiClient, { tournamentAPI } from '@/services/api'

jest.mock('axios', () => {
  const mockAxios = {
    create: jest.fn(() => mockAxios),
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    patch: jest.fn(),
    delete: jest.fn(),
    interceptors: {
      request: { use: jest.fn() },
      response: { use: jest.fn() }
    }
  }
  return mockAxios
})

describe('tournamentAPI', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  describe('updateTournamentSettings', () => {
    it('calls PUT /api/tournaments/:id/settings with the given payload', () => {
      const mockPut = jest.spyOn(apiClient, 'put').mockResolvedValue({ data: { success: true } })
      const payload = { k: 32, absenteeDemerit: 10 }

      tournamentAPI.updateTournamentSettings(5, payload)

      expect(mockPut).toHaveBeenCalledWith('/api/tournaments/5/settings', payload)
    })

    it('calls PUT with ONE_OFF settings payload', () => {
      const mockPut = jest.spyOn(apiClient, 'put').mockResolvedValue({ data: { success: true } })
      const payload = { numberOfRounds: 3, maxPoints: 21 }

      tournamentAPI.updateTournamentSettings(12, payload)

      expect(mockPut).toHaveBeenCalledWith('/api/tournaments/12/settings', payload)
    })
  })
})
