<template>
  <div id="map-container">
    <div id="map"></div>
  </div>
</template>

<script setup>
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import "leaflet.markercluster/dist/MarkerCluster.css";
import "leaflet.markercluster/dist/MarkerCluster.Default.css";
import "leaflet.markercluster";
import {ref, onMounted} from "vue";
import {fetchVideosForMap} from "@/api/mapApi"; // POST /videos/tiles
import {debounce} from "lodash";
import {useRouter} from "vue-router";

// -------------------- STATE --------------------
const map = ref(null);
const markerCluster = ref(null);
const router = useRouter();

// tileId -> VideoPostDto[]
const tileCache = ref(new Map());
const ZOOM_LEVEL = 5;

// -------------------- TILE UTILS --------------------
function long2tile(lon, zoom) {
  return Math.floor(((lon + 180) / 360) * Math.pow(2, zoom));
}

function lat2tile(lat, zoom) {
  const latRad = (lat * Math.PI) / 180;
  return Math.floor(
      ((1 - Math.log(Math.tan(latRad) + 1 / Math.cos(latRad)) / Math.PI) / 2) *
      Math.pow(2, zoom)
  );
}

function getVisibleTileIds(bounds, zoom) {
  const minX = long2tile(bounds.getWest(), zoom);
  const maxX = long2tile(bounds.getEast(), zoom);
  const minY = lat2tile(bounds.getNorth(), zoom);
  const maxY = lat2tile(bounds.getSouth(), zoom);

  const tiles = [];
  for (let x = minX; x <= maxX; x++) {
    for (let y = minY; y <= maxY; y++) {
      tiles.push(`${zoom}_${x}_${y}`);
    }
  }
  return tiles;
}

// -------------------- MAP INIT --------------------
onMounted(() => {
  map.value = L.map("map", {
    center: [48, 19],
    zoom: ZOOM_LEVEL
  });

  L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    attribution: "© OpenStreetMap"
  }).addTo(map.value);

  markerCluster.value = L.markerClusterGroup();
  map.value.addLayer(markerCluster.value);

  loadTilesForViewport();
  map.value.on("moveend", debounce(loadTilesForViewport, 300));
});

// -------------------- CORE LOGIC --------------------
function loadTilesForViewport() {
  if (!map.value) return;

  const bounds = map.value.getBounds();
  const zoom = map.value.getZoom();
  const visibleTiles = getVisibleTileIds(bounds, zoom);

  const missingTiles = visibleTiles.filter(tileId => !tileCache.value.has(tileId));

  if (missingTiles.length === 0) {
    renderFromCache(visibleTiles);
    return;
  }

  fetchVideosForMap({tiles: missingTiles})
      .then(res => {
        res.data.forEach(tile => {
          tileCache.value.set(tile.tileId, tile.videos);
        });
        renderFromCache(visibleTiles);
      })
      .catch(err => {
        console.error("Greška pri učitavanju tile-ova:", err);
      });
}

// -------------------- RENDER MARKERS --------------------
function renderFromCache(tileIds) {
  if (!markerCluster.value) return;

  markerCluster.value.clearLayers();

  tileIds.forEach(tileId => {
    const videos = tileCache.value.get(tileId) || [];

    videos.forEach(video => {
      if (!video.latitude || !video.longitude) return;

      const marker = L.marker([video.latitude, video.longitude]);

      // Bind popup sa dugmetom/linkom
      marker.bindPopup(`
        <div>
          <b>${video.title}</b><br>
          ${video.description || ""}<br>
          <button id="go-to-video-${video.id}" style="margin-top:5px; cursor:pointer;">Pogledaj video</button>
        </div>
      `);

      // Dodaj listener kada se popup otvori
      marker.on("popupopen", () => {
        const btn = document.getElementById(`go-to-video-${video.id}`);
        if (btn) {
          btn.addEventListener("click", () => {
            router.push(`/video/${video.id}`);
          });
        }
      });

      markerCluster.value.addLayer(marker);
    });
  });
}
</script>

<style scoped>
#map-container {
  width: 100%;
  height: 100vh;
}

#map {
  width: 100%;
  height: 100%;
}
</style>
