/*import axios from 'axios'

export function fetchMapTiles(params) {
    return axios.get('/api/map/videos', { params })
}*/

// api/mapApi.js
import axios from "axios";

export function fetchVideosForMap({ tiles }) {
    return axios.post("http://localhost:8080/api/map/videos/tiles", { tiles });
}
