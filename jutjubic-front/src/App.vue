<script setup lang="ts">
</script>

<template>
  <div id="app-container">
    <header class="main-navigation">
      <div class="header-content">
        <router-link to="/" class="logo-container">
          <img src="@/assets/logo.png" class="logo" />
        </router-link>

        <div class="auth-buttons">
          <template v-if="auth.token">
            <router-link to="/create-post" class="auth-btn">Create Post</router-link>
            <router-link to="/my-profile" class="auth-btn">My Profile</router-link>
            <button @click="handleLogout" class="auth-btn logout-btn">Logout</button>
          </template>
          <template v-else>
            <router-link to="/login" class="auth-btn">Login</router-link>
            <router-link to="/register" class="auth-btn">Register</router-link>
          </template>
        </div>
      </div>
    </header>

    <main class="content">
      <router-view />
    </main>
  </div>
</template>


<script setup>
import { useAuthStore } from "@/stores/auth";
import { useRouter } from "vue-router";

const auth = useAuthStore();
const router = useRouter();

const handleLogout = () => {
  if (!confirm("Are you sure you want to log out?")) return;
  auth.logout();
  router.push('/');
};
</script>

<style>
.content {
  padding-top: 90px;
  padding-left: 20px;
  padding-right: 20px;
  min-height: 100vh;
  background-color: #f9f9f9;
}
.main-navigation {
  position: fixed;
  top: 0;
  width: 100%;
  height: 70px;
  background: white;
  border-bottom: 1px solid #e0e0e0;
  z-index: 2000;
  display: flex;
  align-items: center;
}

.header-content {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}
.header {
  position: sticky;
  top: 0;
  left: 0;

  height: 70px;
  background: white;
  display: flex;
  align-items: center;
  padding-left: 25px;
  z-index: 1000;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);

}


.auth-buttons {
  //position: fixed;
  top: 1rem;
  right: 2rem;
  display: flex;
  gap: 1rem;
  z-index: 2000;
}
.auth-btn {
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  border: 1px solid #065fd4;
  color: #065fd4;
  font-weight: 500;
  transition: all 0.2s;
}

.auth-btn:hover {
  background-color: #065fd4;
  color: #ffffff;
}

.logo {
  height: 80px;
  cursor: pointer;
}

.logout-btn {
  background: none;
  cursor: pointer;
  font-family: inherit;
  font-size: inherit;
}
</style>

