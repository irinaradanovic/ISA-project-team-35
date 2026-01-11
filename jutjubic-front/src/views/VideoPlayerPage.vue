<template>
  <div class="video-page">
    <div class="video-container">
      <h2>{{ video.title }}</h2>
      <p class="username">by {{ video.ownerUsername }}</p>

      <div class="video-section">
        <video
            v-if="video.videoPath"
            controls
            :src="videoUrl"
            :poster="thumbnailUrl"
        ></video>

        <div class="video-meta">
          <div class="like-section">
            <button @click="toggleLike" :class="{ liked: liked }" class="like-btn">
              <i class="fas fa-thumbs-up"></i>
            </button>
            <span class="like-count">{{ likeCount }}</span>
          </div>
          <span class="view-count">
              <i class="fas fa-eye"></i> {{ video.viewCount }} views
            </span>
          <p class="created-at">{{ formatDate(video.createdAt) }}</p>
        </div>
      </div>

      <p class="description">{{ video.description }}</p>

      <div class="extra-info">
        <div v-if="video.location" class="location-badge">
          <i class="fas fa-map-marker-alt"></i> {{ video.location }}
        </div>

        <div class="tags-list">
          <span
              v-for="tag in splitTags(video.tags)"
              :key="tag"
              class="tag-item"
          >
            #{{ tag }}
          </span>
        </div>
      </div>

      <div class="comment-form">
        <input
            v-model="newComment"
            placeholder="Add a comment..."
            @keyup.enter="addComment"
        />
        <button @click="addComment" class="comment-submit-btn">Comment</button>
      </div>

      <div class="comments-section">
        <p class="comment-count">{{ commentCount }} comments</p>

        <div v-for="c in comments" :key="c.id" class="comment">
          <strong>{{ c.authorUsername }}</strong>
          <p>{{ c.content }}</p>
          <span class="comment-date">{{ formatDate(c.createdAt) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from "vue";
import { useRoute } from "vue-router";
import axios from "axios";

export default {
  setup() {
    const route = useRoute();
    const video = ref({});
    const comments = ref([]);
    const likeCount = ref(0);
    const viewCount = ref(0);
    const commentCount = ref(0);
    const liked = ref(false);
    const newComment = ref("");
    const videoUrl = ref("");
    const thumbnailUrl = ref("");
    
    const currentPage = ref(0);
    const pageSize = ref(5);
    const hasMoreComments = ref(true);
    const loadingComments = ref(false);

    const splitTags = (tagsString) => {
      if (!tagsString) return [];
      // Razdvaja string po zarezu i briše prazna mesta
      return tagsString.split(',').map(t => t.trim());
    };

    const videoId = route.params.id;

    const loadVideo = async () => {
      const res = await axios.get(`http://localhost:8080/api/videoPosts/${videoId}`);
      video.value = res.data;
      videoUrl.value = `http://localhost:8080/${res.data.videoPath.replace(/\\/g, '/')}`;
      thumbnailUrl.value = `http://localhost:8080/api/videoPosts/${videoId}/thumbnail`;
      likeCount.value = res.data.likeCount;
      viewCount.value = res.data.viewCount;
      commentCount.value = res.data.commentCount;

      const likedRes = await axios.get(`http://localhost:8080/api/videoPosts/${videoId}/liked`);
      liked.value = likedRes.data;
    };

    const loadComments = async (append = false) => {
      if (loadingComments.value || !hasMoreComments.value) return;
      console.log('Loading comments - page:', currentPage.value, 'append:', append);
      loadingComments.value = true;
      try {
        const res = await axios.get(`http://localhost:8080/api/comments/${videoId}/comments`, {
          params: {
            page: currentPage.value,
            size: pageSize.value
          }
        });
        
        const newComments = res.data.content || res.data;
        
        if (append) {
          comments.value = [...comments.value, ...newComments];
        } else {
          comments.value = newComments;
        }
        
        commentCount.value = res.data.totalElements || comments.value.length;
        hasMoreComments.value = !res.data.last;
        currentPage.value++;
      } catch (err) {
        console.error("Error loading comments:", err);
      } finally {
        loadingComments.value = false;
      }
    };

    const handleScroll = () => {
      const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
      const scrollHeight = document.documentElement.scrollHeight;
      const clientHeight = document.documentElement.clientHeight;
      
      if (scrollTop + clientHeight >= scrollHeight - 200 && !loadingComments.value && hasMoreComments.value) {
        loadComments(true);
      }
    };

    const toggleLike = async () => {
      const res = await axios.post(`http://localhost:8080/api/videoPosts/${videoId}/like`);
      likeCount.value = res.data;
      liked.value = !liked.value;
    };

    const addComment = async () => {
      if (!newComment.value.trim()) return;

      try {
        await axios.post(`http://localhost:8080/api/comments/${videoId}`, {
          content: newComment.value
        });

        // Reset and reload
        currentPage.value = 0;
        hasMoreComments.value = true;
        await loadComments(false);
        newComment.value = "";
      } catch (err) {
        console.error("Error adding comment:", err);
      }

    };

    /* const incrementView = async () => {
       try {
         await axios.post(`http://localhost:8080/api/videoPosts/${videoId}/view`);
       } catch (err) {
         console.error("Error incrementing view count:", err);
       }
     };   */

    const formatDate = (dateStr) => {
      if (!dateStr) return "";
      return new Date(dateStr).toLocaleString();
    };

    onMounted(async () => {

      await loadVideo();
      await loadComments();
      window.addEventListener('scroll', handleScroll);
    });

    onUnmounted(() => {
      window.removeEventListener('scroll', handleScroll);
    });

    return {
      video,
      comments,
      likeCount,
      commentCount,
      liked,
      newComment,
      videoUrl,
      thumbnailUrl,
      loadingComments,
      toggleLike,
      addComment,
      formatDate,
      splitTags,
      location
    };
  },
};
</script>

<style scoped>
.video-page {
  padding: 20px;
  background-color: #f9f9f9;
  min-height: 100vh;
}

.video-container {
  max-width: 1200px;
  margin: 0 auto;
  background-color: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}


h2 {
  margin: 0 0 8px 0;
}

.username {
  font-weight: 600;
  color: #606060;
  margin: 0 0 20px 0;
  font-size: 0.95em;
}

.video-section {
  margin-bottom: 20px;
}

video {
  width: 100%;
  max-width: 800px;
  display: block;
  background-color: #000;
}

.video-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
  padding: 10px 0;
}

.like-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.like-btn {
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 50%;
  width: 48px;
  height: 48px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  padding: 0;
}

.like-btn:hover {
  background-color: #e8e8e8;
  transform: scale(1.05);
  width: 48px;
}

.like-btn i {
  font-size: 20px;
  color: #666;
  transition: color 0.3s ease;
}

.like-btn.liked {
  background-color: #e3f2fd;
  border-color: #2196F3;
}

.like-btn.liked i {
  color: #2196F3;
}

.like-count {
  font-size: 1em;
  font-weight: 600;
  color: #333;
}

.extra-info {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
  margin: 15px 0;
  padding: 10px 0;
  border-top: 1px solid #eee;
}

.location-badge {
  background-color: #f0f0f0;
  padding: 5px 12px;
  border-radius: 15px;
  font-size: 0.85em;
  color: #555;
  display: flex;
  align-items: center;
  gap: 5px;
}

.tags-list {
  display: flex;
  gap: 10px;
}

.tag-item {
  color: #065fd4;
  font-size: 0.9em;
  font-weight: 500;
}

.description {
  color: #333;
  line-height: 1.6;
  margin: 0 0 15px 0;
}

.created-at {
  font-size: 0.85em;
  color: #888;
  margin: 0;
}

.comment-form {
  display: flex;
  gap: 10px;
  margin: 25px 0 20px 0;
  padding-bottom: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.comment-form input {
  flex: 1;
  /* Global input styles will apply */
}

.comment-submit-btn {
  width: auto;
  padding: 10px 24px;
  background-color: #ff3d00;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.95em;
  cursor: pointer;
  transition: background-color 0.2s ease;
  font-weight: 500;
}

.comment-submit-btn:hover {
  background-color: #e63900;
}

.comments-section {
  margin-top: 10px;
}

.comment-count {
  font-weight: 600;
  color: #333;
  margin-bottom: 15px;
  font-size: 1.05em;
}

.comment {
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment:last-child {
  border-bottom: none;
}

.comment strong {
  color: #333;
  font-size: 0.95em;
}

.comment p {
  margin: 8px 0;
  color: #555;
  line-height: 1.5;
}

.comment-date {
  font-size: 0.8em;
  color: #999;
}
</style>

