import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router/index.js'
import axios from 'axios'
import './assets/global.css'

// Podesimo osnovni URL za backend
axios.defaults.baseURL = 'http://localhost:8080/api'

// Kreiramo aplikaciju
const app = createApp(App)

// Dodajemo Pinia store
app.use(createPinia())

// Dodajemo router
app.use(router)

// Postavljamo axios globalno da možeš koristiti preko app.config.globalProperties
app.config.globalProperties.$axios = axios

// Mount aplikacije
app.mount('#app')
