<script setup lang="ts">
</script>

<template>
  <header class="header">
    <router-link to="/">
      <img src="@/assets/logo.png" class="logo" />
    </router-link>
  </header>

  <router-view />
</template>

<style>
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

.logo {
  height: 80px;
  cursor: pointer;
}
</style>

