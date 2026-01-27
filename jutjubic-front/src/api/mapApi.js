import axios from 'axios'

export function fetchMapTiles(params) {
    return axios.get('/api/map/videos', { params })
}
