<template>
  <div class="home">
    <!-- Header sa login/register -->
    <!--header class="home-header">
      <h1 class="logo"></h1>

      <div class="auth-buttons">
        <template v-if="auth.token">
          <router-link to="/my-profile" class="auth-btn">My Profile</router-link>
          <router-link to="/create-post" class="auth-btn">Create Post</router-link>
          <button @click="handleLogout" class="auth-btn logout-btn">Logout</button>
        </template>
        <template v-else>
          <router-link to="/login" class="auth-btn">Login</router-link>
          <router-link to="/register" class="auth-btn">Register</router-link>
        </template>
      </div>
    </header> -->

    <!-- Videos -->
    <div class="videos">
      <section v-for="(section, sectionIndex) in videoSections" :key="sectionIndex" class="video-section">
        <h3 v-if="sectionIndex > 0" class="section-header">More Videos</h3>
        <article v-for="video in section" :key="video.id" class="video-container">
          <router-link :to="`/video/${video.id}`" class="thumbnail">
            <img
                class="thumbnail-image"
                :src="thumbnailUrl(video)"
                :alt="video.title"
            />
          </router-link>

          <div class="video-bottom-section">
            <router-link :to="`/user/${video.ownerId}`" class="channel-icon-link">
              <img class="channel-icon" src="@/assets/profile-picture.png" alt="Channel Icon">
            </router-link>
            <div class="video-info">
              <router-link :to="`/video/${video.id}`" class="video-title">{{ video.title }}</router-link>
              <a href="#" class="channel-name">{{ video.ownerUsername }}</a>
              <div class="video-stats">
                <span>{{ formatDate(video.createdAt) }}</span>
                <span v-if="video.location"> • {{ video.viewCount }}</span>
              </div>
            </div>
          </div>
        </article>
      </section>

      <div v-if="loading" class="loading">Loading more videos</div>
      <div v-else-if="!hasMore" class="no-more">No more videos</div>
    </div>
  </div>
</template>

<style scoped>
/* --- Header --- */
.home-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background-color: #ffffff;
  border-bottom: 1px solid #e0e0e0;
  /*position: sticky;
  top: 0;
  z-index: 1000;
  */
}

.logo {
  font-size: 1.5rem;
  font-weight: bold;
  color: #030303;
  margin: 0;
}

.auth-buttons {
  position: fixed;
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

/* --- Videos --- */
body {
  font-family: Arial, sans-serif;
  margin: 0;
  padding: 0;
  background-color: #f9f9f9;
}

.channel-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  cursor: pointer;
}

.video-container {
  display: flex;
  flex-direction: column;
}

.thumbnail {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  background-color: #000;
  overflow: hidden;
  border-radius: 8px;
}

.thumbnail-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumbnail::before {
  content: attr(video-duration);
  position: absolute;
  background-color: rgba(0, 0, 0, .85);
  color: white;
  right: 5px;
  bottom: 5px;
  padding: .1em .3em;
  border-radius: .3em;
  font-size: .9rem;
}

.video-section {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 2rem 1rem;
  padding: 2rem 0rem;
  margin : 0 1rem;
  border-top: 1px solid #e0e0e0;
}

.video-section:first-child {
  border-top: none;
}

.section-header {
  grid-column: 1 / -1;
  margin: 0 0 1rem 0;
  color: #030303;
  font-size: 1.2rem;
  font-weight: 600;
}

.video-bottom-section {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.video-info {
  flex: 1;
}

.video-title {
  font-weight: 500;
  color: #030303;
  text-decoration: none;
  font-size: 0.95rem;
  display: block;
  margin-bottom: 0.25rem;
}

.video-title:hover {
  color: #065fd4;
}

.channel-name {
  color: #606060;
  text-decoration: none;
  font-size: 0.85rem;
}

.video-stats {
  color: #606060;
  font-size: 0.85rem;
}

.loading, .no-more {
  text-align: center;
  padding: 2rem 0;
  color: #606060;
  font-size: 0.95rem;
}

.logout-btn {
  background: none;
  cursor: pointer;
  font-family: inherit;
  font-size: inherit;
}
</style>

<script>
import { ref, computed, onMounted, onUnmounted, nextTick } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";
import { useAuthStore } from "@/stores/auth";

export default {
  setup() {
    const router = useRouter();
    const auth = useAuthStore();
    
    const videos = ref([]);
    const currentPage = ref(0);
    const pageSize = ref(10);
    const totalPages = ref(0);
    const totalElements = ref(0);
    const loading = ref(false);
    const hasMore = ref(true);
    const columnsPerRow = ref(4);
    const rowsPerSection = ref(2);

    const videosPerSection = computed(() => {
      return columnsPerRow.value * rowsPerSection.value;
    });

    const videoSections = computed(() => {
      const sections = [];
      for (let i = 0; i < videos.value.length; i += videosPerSection.value) {
        sections.push(videos.value.slice(i, i + videosPerSection.value));
      }
      return sections;
    });

    const handleLogout = () => {
      auth.logout();
      router.push('/');
    };

    const calculateColumns = () => {
      const containerWidth = window.innerWidth - 32;
      const minVideoWidth = 250;
      const gap = 16;
      const columns = Math.max(1, Math.floor((containerWidth + gap) / (minVideoWidth + gap)));
      columnsPerRow.value = columns;
    };

    const checkIfScrollable = () => {
      const isScrollable = document.documentElement.scrollHeight > window.innerHeight;
      if (!isScrollable && hasMore.value && !loading.value) {
        loadMoreVideos();
      }
    };

    const loadPage = (page) => {
      if (loading.value) return;
      loading.value = true;
      
      axios.get(`http://localhost:8080/api/videoPosts?page=${page}&size=${pageSize.value}`)
        .then(response => {
          const newVideos = response.data.content;
          if (page === 0) {
            videos.value = newVideos;
          } else {
            videos.value = [...videos.value, ...newVideos];
          }
          currentPage.value = response.data.number;
          totalPages.value = response.data.totalPages;
          totalElements.value = response.data.totalElements;
          hasMore.value = currentPage.value < totalPages.value - 1;
          loading.value = false;
          
          nextTick(() => {
            checkIfScrollable();
          });
        })
        .catch(error => {
          console.error('There was an error fetching the videos!', error);
          loading.value = false;
        });
    };

    const loadMoreVideos = () => {
      if (loading.value || !hasMore.value) return;
      const nextPage = currentPage.value + 1;
      if (nextPage < totalPages.value) {
        loadPage(nextPage);
      }
    };

    const handleScroll = () => {
      const scrollTop = window.scrollY || document.documentElement.scrollTop;
      const windowHeight = window.innerHeight;
      const docHeight = document.documentElement.scrollHeight;
      if (scrollTop + windowHeight >= docHeight - 200) {
        loadMoreVideos();
      }
    };

    const formatDate = (dateString) => {
      if (!dateString) return '';
      const date = new Date(dateString);
      const now = new Date();
      const diffTime = Math.abs(now - date);
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      if (diffDays === 0) return 'Today';
      if (diffDays === 1) return 'Yesterday';
      if (diffDays < 7) return `${diffDays} days ago`;
      if (diffDays < 30) return `${Math.floor(diffDays / 7)} weeks ago`;
      if (diffDays < 365) return `${Math.floor(diffDays / 30)} months ago`;
      return `${Math.floor(diffDays / 365)} years ago`;
    };

    const thumbnailUrl = (video) => {
      if (video.thumbnailPath) {
        return `http://localhost:8080/${video.thumbnailPath}`;
      }
      return `http://localhost:8080/api/videoPosts/${video.id}/thumbnail`;
    };

    onMounted(() => {
      loadPage(0);
      window.addEventListener('scroll', handleScroll);
      window.addEventListener('resize', calculateColumns);
      calculateColumns();
    });

    onUnmounted(() => {
      window.removeEventListener('scroll', handleScroll);
      window.removeEventListener('resize', calculateColumns);
    });

    return {
      auth,
      videos,
      videoSections,
      loading,
      hasMore,
      handleLogout,
      formatDate,
      thumbnailUrl
    };
  }
};
</script>
