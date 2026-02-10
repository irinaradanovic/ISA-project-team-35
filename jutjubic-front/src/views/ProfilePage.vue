<template>
  <div class="profile-container">
    <div class="profile-header">
      <div class="profile-left">
        <img src="@/assets/profile-picture.png" class="profile-img" />

        <h2 v-if="user">{{ user.username }}'s Profile</h2> <!-- Mora if kako bi se loadala stranica prvo -->
      </div>
    </div>


    <div class="profile-card" v-if="user">
      <p><b>Username:</b> {{ user.username }}</p>
      <p><b>Email:</b> {{ user.email }}</p>
      <p><b>Name:</b> {{ user.firstName }} {{ user.lastName }}</p>
      <p><b>Address:</b> {{ user.address }}</p>
    </div>


    <h3 class="videos-title">My Uploaded Videos</h3>

    <div class="videos-list">
      <div class="video-item"
           v-for="v in videos"
           :key="v.id"
           @click="goToVideo(v.id)">

        <img
            :src="`http://localhost/api/videoPosts/${v.id}/thumbnail`"
            class="thumb"
        >

        <div class="video-info">
          <h4>{{ v.title }}</h4>
          <p>{{ v.description }}</p>
          <p>Views: {{ v.viewCount }} | Likes: {{ v.likeCount }}</p>
          <p>Posted: {{ formatDate(v.createdAt) }}</p>

        </div>

      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';

const route = useRoute();
const router = useRouter();

const user = ref(null);
const videos = ref([]);

const loadProfile = async (id) => {
  try {
    const { data } = await axios.get(`http://localhost/api/users/${id}`);
    user.value = data;
  } catch (e) {
    console.error('Profile load failed', e);
    alert('User not found');
  }
};

const loadVideos = async (id) => {
  try {
    const { data } = await axios.get(`http://localhost/api/videoPosts/user/${id}`);
    videos.value = data;
  } catch (e) {
    console.error('Failed to load videos', e);
  }
};

const formatDate = (date) => {
  return new Date(date).toLocaleString('en-GB', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const goToVideo = (id) => {
  router.push(`/video/${id}`);
};

const refresh = async () => {
  const id = route.params.userId;
  if (!id) return;
  await Promise.all([loadProfile(id), loadVideos(id)]);
};

onMounted(refresh);
watch(() => route.params.userId, refresh);

</script>

<style scoped>
.profile-container {
  width: 900px;
  margin: auto;
  padding: 20px;
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.edit-btn {
  background: indianred;
  border: none;
  font-size: 16px;
  padding: 6px 8px;
  width : 120px;
  cursor: pointer;
  display: flex;
  align-items: center;
}


.profile-card {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 10px;
  margin-top: 10px;
}

.videos-title {
  margin-top: 30px;
}

.videos-list {
  margin-top: 10px;
}

.video-item {
  background: #eeeeee;
  padding: 10px;
  border-radius: 10px;
  margin-bottom: 10px;
}


.profile-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.profile-img {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  object-fit: cover;
}


.videos-list {
  margin-top: 10px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.video-item {
  background: #f3f3f3;
  border-radius: 10px;
  padding: 10px;
  display: flex;
  gap: 10px;
  cursor: pointer;
  transition: 0.2s;
}

.video-item:hover {
  background: #e5e5e5;
}

.thumb {
  width: 180px;
  height: 110px;
  border-radius: 10px;
  object-fit: cover;
}

.video-info {
  flex: 1;
}

</style>
