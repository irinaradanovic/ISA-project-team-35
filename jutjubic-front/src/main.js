import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router/index.js'
import axios from 'axios'
import './assets/global.css'

import 'leaflet/dist/leaflet.css' // za prikaz mape

// Podesimo osnovni URL za backend
axios.defaults.baseURL = 'http://localhost:8080/api'

// Kreiramo aplikaciju
const app = createApp(App)

// Dodajemo Pinia store
app.use(createPinia())


// Postavljamo axios globalno da možeš koristiti preko app.config.globalProperties
app.config.globalProperties.$axios = axios

// ==========================
// OPTIONAL: Global axios interceptor
// ==========================
// Ako store ima token, svaki request šalje Authorization header
import { useAuthStore } from './stores/auth'
const auth = useAuthStore();

axios.interceptors.request.use(config => {
    if (auth.token) {
        config.headers.Authorization = `Bearer ${auth.token}`;
    }
    return config;
});

// Dodajemo router
app.use(router)

// Mount aplikacije
app.mount('#app')
