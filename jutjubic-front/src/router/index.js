import { createRouter, createWebHistory } from 'vue-router'
import VideoPostFormPage from '../views/VideoPostFormPage.vue';
import VideoPlayerPage from '@/views/VideoPlayerPage.vue'
import HomePage from '@/views/HomePage.vue'
import MyProfilePage from '@/views/MyProfilePage.vue'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'

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
      },
      {
          path: '/my-profile',
          name: 'MyProfilePage',
          component: MyProfilePage
      }
  ],
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
        },
        {
            path: '/login',
            name: 'LoginView',
            component: LoginView
        },
        {
            path: '/register',
            name: 'RegisterView',
            component: RegisterView
        }
    ],
})

export default router
