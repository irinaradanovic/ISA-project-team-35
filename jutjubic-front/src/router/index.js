import { createRouter, createWebHistory } from 'vue-router'
import VideoPostFormPage from '../views/VideoPostFormPage.vue';
import VideoPlayerPage from '@/views/VideoPlayerPage.vue'
import HomePage from '@/views/HomePage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
      {
      path: '/create-post',
      name: 'VideoPostFormPage',
      component: VideoPostFormPage,
    },
      {
          path: '/video/:id',
          name: 'VideoPlayerPage',
          component: VideoPlayerPage
      },
      {
        path: '/',
        name: 'HomePage',
        component: HomePage
      }
  ],
})

export default router
