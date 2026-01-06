<template>
  <div class="video-player-container">
    <h2>{{ video.title }}</h2>
    <video
        v-if="video.videoPath"
        controls
        :src="videoUrl"
        :poster="thumbnailUrl"
    >
    </video>

    <p>{{ video.description }}</p>
    <p><strong>Tags:</strong> {{ video.tags }}</p>
    <p v-if="video.location"><strong>Location:</strong> {{ video.location }}</p>
  </div>
</template>

<script>
import axios from 'axios';
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';

export default {
  setup() {
    const route = useRoute();
    const video = ref({});
    const videoUrl = ref('');
    const thumbnailUrl = ref('');


    onMounted(async () => {
      const videoId = route.params.id;
      try {
        const res = await axios.get(`http://localhost:8080/api/videoPosts/${videoId}`);
        video.value = res.data;
        videoUrl.value = `http://localhost:8080/${res.data.videoPath.replace(/\\/g, '/')}`;
        thumbnailUrl.value =
            `http://localhost:8080/api/videoPosts/${videoId}/thumbnail`;
      } catch (err) {
        console.error(err);
      }
    });

    return { video, videoUrl, thumbnailUrl };

  }
};
</script>

<style scoped>
.video-player-container {
  max-width: 800px;
  margin: 30px auto;
  text-align: center;
}

video {
  width: 100%;
  max-height: 500px;
  margin-bottom: 15px;
}
</style>
