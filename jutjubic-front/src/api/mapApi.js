import axios from "axios";

// Destrukturiramo i tiles i timeRange iz objekta koji saljemo
export function fetchVideosForMap({ tiles, timeRange }) {
    return axios.post("http://localhost/api/map/videos/tiles", {
        tiles,
        timeRange // S2: salje se i filter za vreme
    });
}
