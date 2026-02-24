import axios from 'axios'

// Using esgoo.net API for Vietnam Administrative Units
const API_URL = 'https://esgoo.net/api-tinhthanh'

export interface Province {
  id: string
  name: string
}

export interface District {
  id: string
  name: string
}

export interface Ward {
  id: string
  name: string
}

class LocationService {
  async getProvinces(): Promise<Province[]> {
    try {
      const response = await axios.get(`${API_URL}/1/0.htm`)
      if (response.data.error === 0) {
        return response.data.data.map((item: any) => ({
          id: item.id,
          name: item.full_name,
        }))
      }
      return []
    } catch (error) {
      console.error('Error fetching provinces', error)
      return []
    }
  }

  async getDistricts(provinceId: string): Promise<District[]> {
    try {
      const response = await axios.get(`${API_URL}/2/${provinceId}.htm`)
      if (response.data.error === 0) {
        return response.data.data.map((item: any) => ({
          id: item.id,
          name: item.full_name,
        }))
      }
      return []
    } catch (error) {
      console.error('Error fetching districts', error)
      return []
    }
  }

  async getWards(districtId: string): Promise<Ward[]> {
    try {
      const response = await axios.get(`${API_URL}/3/${districtId}.htm`)
      if (response.data.error === 0) {
        return response.data.data.map((item: any) => ({
          id: item.id,
          name: item.full_name,
        }))
      }
      return []
    } catch (error) {
      console.error('Error fetching wards', error)
      return []
    }
  }
}

export default new LocationService()
