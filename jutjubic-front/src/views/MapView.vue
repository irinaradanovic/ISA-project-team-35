<template>
  <div id="map-container">
    <div class="filter-overlay">
      <select v-model="selectedTimeRange" @change="onFilterChange" class="time-select">
        <option value="ALL">All time</option>
        <option value="LAST_30_DAYS">Last 30 days</option>
        <option value="THIS_YEAR">This year</option>
      </select>
    </div>
    <div id="map"></div>
  </div>
</template>

<script setup>
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import "leaflet.markercluster/dist/MarkerCluster.css";
import "leaflet.markercluster/dist/MarkerCluster.Default.css";
import "leaflet.markercluster";
import { ref, onMounted } from "vue";
import { fetchVideosForMap } from "@/api/mapApi";
import { debounce } from "lodash";
import { useRouter } from "vue-router";

// -------------------- STATE --------------------
const map = ref(null);
const markerCluster = ref(null);
const router = useRouter();

// S2: Stanje za vremenski filter, default all
const selectedTimeRange = ref("ALL");

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

// -------------------- S2 : FILTER CHANGE --------------------
function onFilterChange() {
  // kada se promeni filter za vreme, kes vise ne vazi
  tileCache.value.clear();
  if (markerCluster.value) {
    markerCluster.value.clearLayers();
  }
  loadTilesForViewport();
}

// -------------------- MAP INIT --------------------
onMounted(() => {
  map.value = L.map("map", {
    center: [44.8, 20.4], // Centrirano na Balkan/Evropu
    zoom: ZOOM_LEVEL
  });

  // Sprečava scroll kod klika na kontrole
  L.DomEvent.disableClickPropagation(document.querySelector('.leaflet-control-zoom'));

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
  
  console.log('🔍 Visible tiles:', visibleTiles);
  console.log('🗺️ Current zoom:', zoom);

  const missingTiles = visibleTiles.filter(tileId => !tileCache.value.has(tileId));
  
  console.log('📥 Missing tiles to fetch:', missingTiles);

  if (missingTiles.length === 0) {
    console.log('✅ Using cache');
    renderFromCache(visibleTiles);
    return;
  }

  console.log('🌐 Fetching from backend...');
  fetchVideosForMap({
    tiles: missingTiles,
    timeRange: selectedTimeRange.value
  })
    .then(res => {
      console.log('📦 Response from backend:', res.data);
      res.data.forEach(tile => {
        console.log('💾 Caching tile:', tile.tileId, tile);
        tileCache.value.set(tile.tileId, tile);
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

  console.log('🎨 Rendering from cache, tileIds:', tileIds);
  markerCluster.value.clearLayers();

  tileIds.forEach(tileId => {
    // Dodaj suffix za traženje u cache-u
    const cacheKey = `${tileId}_${selectedTimeRange.value}`;
    const tile = tileCache.value.get(cacheKey);
    console.log('🔎 Tile from cache:', cacheKey, tile);
    
    if (!tile) {
      console.log('⚠️ Tile not found in cache:', cacheKey);
      return;
    }

    if (tile.type === 'AGGREGATE') {
      console.log('📍 Creating AGGREGATE marker:', tile);
      const clusterMarker = L.circleMarker([tile.centerLat, tile.centerLng], {
        radius: Math.min(30, Math.max(15, Math.log(tile.videoCount) * 3)),
        fillColor: '#ff6b6b',
        fillOpacity: 0.6,
        color: '#c92a2a',
        weight: 2
      });

      clusterMarker.bindPopup(`
        <div style="text-align:center; padding:5px;">
          <b>${tile.videoCount}</b> videos<br>
          <small>Zoom in to see details</small>
        </div>
      `);

      markerCluster.value.addLayer(clusterMarker);
      console.log('✅ AGGREGATE marker added');
    } 
    else if (tile.videos && tile.videos.length > 0) {
      console.log('📍 Creating VIDEO markers:', tile.videos.length);
      tile.videos.forEach(video => {
        if (!video.latitude || !video.longitude) return;
        const marker = L.marker([video.latitude, video.longitude]);
        marker.bindPopup(`
          <div style="min-width: 150px;">
            <img src="http://localhost:8080/${video.thumbnailPath}" style="width:100%; border-radius:4px;"/>
            <br><b>${video.title}</b><br>
            <small>${new Date(video.createdAt).toLocaleDateString()}</small><br>
            <button id="go-to-video-${video.id}" style="margin-top:10px; width:100%; cursor:pointer; background:#f00; color:#fff; border:none; padding:5px; border-radius:4px;">Gledaj</button>
          </div>
        `);
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
    } else {
      console.log('⚠️ Tile has no videos and is not AGGREGATE:', tile);
    }
  });
  
  console.log('✅ Rendering complete');
}
</script>

<style scoped>
/* Sprečava skakanje stranice kod zumiranja */
#map-container .leaflet-control-zoom-in,
#map-container .leaflet-control-zoom-out {
  text-decoration: none;
}

#map-container {
  width: 100%;
  height: 100vh;
  position: relative;
}

#map {
  width: 100%;
  height: 100%;
}

/* Stil za filter iznad mape */
.filter-overlay {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 1000;
  background: white;
  padding: 10px;
  border-radius: 8px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.3);
}

.time-select {
  padding: 8px;
  border-radius: 4px;
  border: 1px solid #ccc;
  font-weight: bold;
  outline: none;
}
</style>