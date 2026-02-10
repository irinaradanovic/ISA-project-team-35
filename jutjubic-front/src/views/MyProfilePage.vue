<template>
  <div class="profile-container">
      <div v-if="!authStore.token" class="auth-warning">
        <h2>Please log in</h2>
        <p>You must be logged in to view your profile and videos.</p>
      </div>

    <template v-else>
    <div class="profile-header">
      <div class="profile-left">
        <img src="@/assets/profile-picture.png" class="profile-img" />

        <h2>My Profile</h2>
      </div>

      <button class="edit-btn" @click="editing = true">
        <i class="fas fa-user-edit"></i> Edit Profile
      </button>
    </div>


    <div class="profile-card" v-if="user">
      <p><b>Username:</b> {{ user.username }}</p>
      <p><b>Email:</b> {{ user.email }}</p>
      <p><b>Name:</b> {{ user.firstName }} {{ user.lastName }}</p>
      <p><b>Address:</b> {{ user.address }}</p>
    </div>

    <div class="edit-modal" v-if="editing">
      <div class="edit-box">
        <h3>Edit Profile</h3>

        <label>Username</label>
        <input v-model="edit.username">

        <label>First Name</label>
        <input v-model="edit.firstName">

        <label>Last Name</label>
        <input v-model="edit.lastName">

        <label>Address</label>
        <input v-model="edit.address">

        <div class="edit-actions">
          <button @click="saveEdit">Save</button>
          <button class="cancel" @click="editing = false">Cancel</button>
        </div>
      </div>
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
          <button class="delete-btn" @click.stop="deleteVideo(v.id)">
            <i class="fas fa-trash"></i>
          </button>

        </div>

      </div>
    </div>

    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const user = ref(null);
const videos = ref([]);
const editing = ref(false);
const edit = reactive({
  username: '',
  firstName: '',
  lastName: '',
  address: ''
});

const loadProfile = async () => {
  try {
    const { data } = await axios.get('http://localhost/api/users/current-user');
    user.value = data;

    edit.username = data.username;
    edit.firstName = data.firstName;
    edit.lastName = data.lastName;
    edit.address = data.address;
  } catch (e) {
    console.error('Profile load failed', e);
    alert('User not found');
  }
};

const loadVideos = async () => {
  try {
    const { data } = await axios.get('http://localhost/api/videoPosts/my');
    videos.value = data;
  } catch (e) {
    console.error('Failed to load videos', e);
  }
};

const saveEdit = async () => {
  try {
    await axios.put('http://localhost/api/users/current-user', edit);
    editing.value = false;
    await loadProfile();
    alert('Profile updated!');
  } catch (e) {
    alert('Update failed');
  }
};

const deleteVideo = async (id) => {
  if (!confirm('Are you sure you want to delete this video?')) return;

  try {
    await axios.delete(`http://localhost/api/videoPosts/${id}`);
    videos.value = videos.value.filter(v => v.id !== id);
    alert('Video deleted!');
  } catch (e) {
    alert('Failed to delete video');
    console.error(e);
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
  router.push('/video/${id}');
};

onMounted(async () => {
  if (authStore.token) {
    await loadProfile();
    await loadVideos();
  }
});
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



.delete-btn {
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: 18px;
  color: red;
  margin-top: 8px;
}

.delete-btn:hover {
  color: darkred;
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

.edit-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,.6);
  display: flex;
  justify-content: center;
  align-items: center;
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


.edit-box {
  background: white;
  padding: 20px;
  width: 400px;
  border-radius: 12px;
}

.edit-box input {
  width: 100%;
  padding: 6px;
  margin-bottom: 10px;
}

.edit-actions {
  display: flex;
  justify-content: space-between;
}

.cancel {
  background: gray;
  color: white;
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
