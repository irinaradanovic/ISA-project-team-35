<script>
import L from 'leaflet'
import { fetchMapTiles } from '@/api/mapApi'

export default {
  name: 'VideoMap',
  data() {
    return {
      map: null,
      layer: null
    }
  },
  mounted() {
    this.initMap()
  },
  methods: {
    initMap() {
      this.map = L.map('video-map').setView([48.5, 17.0], 5)

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 18
      }).addTo(this.map)

      this.layer = L.layerGroup().addTo(this.map)

      this.map.on('moveend', this.onMapChanged)
      this.map.on('zoomend', this.onMapChanged)

      this.onMapChanged()
    },

    async onMapChanged() {
      const bounds = this.map.getBounds()

      const params = {
        north: bounds.getNorth(),
        south: bounds.getSouth(),
        east: bounds.getEast(),
        west: bounds.getWest(),
        zoom: this.map.getZoom()
      }

      try {
        const res = await fetchMapTiles(params)
        this.renderTiles(res.data)
      } catch (e) {
        console.error('Failed to load map tiles', e)
      }
    },

    renderTiles(tiles) {
      this.layer.clearLayers()

      tiles.forEach(t => {
        if (t.type === 'VIDEO') {
          L.marker([t.lat, t.lon])
              .bindPopup(`<b>${t.title}</b>`)
              .addTo(this.layer)
        }

        if (t.type === 'AGGREGATE') {
          L.circleMarker([t.lat, t.lon], {
            radius: 18,
            fillOpacity: 0.6
          })
              .bindPopup(`${t.count} videos`)
              .addTo(this.layer)
        }
      })
    }
  }
}
</script>

<template>
  <div id="video-map"></div>
</template>

<style scoped>
#video-map {
  height: 600px;
  width: 100%;
}
</style>
>