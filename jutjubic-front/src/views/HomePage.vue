<template>
  <div class="home">
    <!-- Header sa login/register -->
    <header class="home-header">
      <h1 class="logo"></h1>


      <div class="auth-buttons">
        <router-link to="/login" class="auth-btn">Login</router-link>
        <router-link to="/register" class="auth-btn">Register</router-link>

      </div>
    </header>

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
            <img class="channel-icon" src="@/assets/logo.png" alt="Channel Icon">
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
  position: sticky;
  top: 0;
  z-index: 1000;
}

.logo {
  font-size: 1.5rem;
  font-weight: bold;
  color: #030303;
  margin: 0;
}

.auth-buttons {
  display: flex;
  gap: 1rem;
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
</style>

<script>
import axios from 'axios'

export default {
  name: 'HomePage',
  data() {
    return {
      videos: [],
      currentPage: 0,
      pageSize: 10,
      totalPages: 0,
      totalElements: 0,
      loading: false,
      hasMore: true,
      columnsPerRow: 4,
      rowsPerSection: 2
    }
  },
  computed: {
    videosPerSection() {
      return this.columnsPerRow * this.rowsPerSection;
    },
    videoSections() {
      const sections = [];
      for (let i = 0; i < this.videos.length; i += this.videosPerSection) {
        sections.push(this.videos.slice(i, i + this.videosPerSection));
      }
      return sections;
    }
  },
  mounted() {
    this.loadPage(0);
    window.addEventListener('scroll', this.handleScroll);
    window.addEventListener('resize', this.calculateColumns);
    this.calculateColumns();
  },
  beforeUnmount() {
    window.removeEventListener('scroll', this.handleScroll);
    window.removeEventListener('resize', this.calculateColumns);
  },
  methods: {
    calculateColumns() {
      const containerWidth = window.innerWidth - 32;
      const minVideoWidth = 250;
      const gap = 16;
      const columns = Math.max(1, Math.floor((containerWidth + gap) / (minVideoWidth + gap)));
      this.columnsPerRow = columns;
    },
    handleScroll() {
      const scrollTop = window.scrollY || document.documentElement.scrollTop;
      const windowHeight = window.innerHeight;
      const docHeight = document.documentElement.scrollHeight;
      if (scrollTop + windowHeight >= docHeight - 200) {
        this.loadMoreVideos();
      }
    },
    checkIfScrollable() {
      const isScrollable = document.documentElement.scrollHeight > window.innerHeight;
      if (!isScrollable && this.hasMore && !this.loading) {
        this.loadMoreVideos();
      }
    },
    loadMoreVideos() {
      if (this.loading || !this.hasMore) return;
      const nextPage = this.currentPage + 1;
      if (nextPage < this.totalPages) {
        this.loadPage(nextPage);
      }
    },
    loadPage(page) {
      if (this.loading) return;
      this.loading = true;
      axios.get(`http://localhost:8080/api/videoPosts?page=${page}&size=${this.pageSize}`)
          .then(response => {
            const newVideos = response.data.content;
            if (page === 0) {
              this.videos = newVideos;
            } else {
              this.videos = [...this.videos, ...newVideos];
            }
            this.currentPage = response.data.number;
            this.totalPages = response.data.totalPages;
            this.totalElements = response.data.totalElements;
            this.hasMore = this.currentPage < this.totalPages - 1;
            this.loading = false;
            this.$nextTick(() => {
              this.checkIfScrollable();
            });
          })
          .catch(error => {
            console.error('There was an error fetching the videos!', error);
            this.loading = false;
          });
    },
    formatDate(dateString) {
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
    },
    thumbnailUrl(video) {
      if (video.thumbnailPath) {
        return `http://localhost:8080/${video.thumbnailPath}`;
      }
      return `http://localhost:8080/api/videoPosts/${video.id}/thumbnail`;
    }
  }
}
</script>
