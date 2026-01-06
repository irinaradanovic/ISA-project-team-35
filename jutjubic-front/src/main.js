import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router/index.js'

createApp(App)
    .use(router)
    .mount('#app')
//import router from './router'

const app = createApp(App)
axios.defaults.baseURL = 'http://localhost:8080/api';

app.use(createPinia())
app.use(router)

app.mount('#app')
