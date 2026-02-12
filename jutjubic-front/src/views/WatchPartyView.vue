<template>
  <div class="watch-party-page">
    <div class="container">
      <div class="info-card">
        <div class="top-row">
          <div class="room-details">
            <h1>Watch Party</h1>
            <p v-if="room"><strong>{{ room.roomName }}</strong></p>
          </div>
          <button class="start-btn" @click="startWatchParty" :disabled="!room || !room.currentVideoId">
            ▶ Start
          </button>
        </div>

        <div class="link-row">
          <button class="copy-btn" @click="copyJoinLink">Copy Link</button>
        </div>

        <p v-if="status" class="status">{{ status }}</p>
        <p v-if="error" class="error">{{ error }}</p>
      </div>

      <div class="video-container" v-if="videoUrl">
        <video ref="videoPlayer" controls :src="videoUrl" :poster="thumbnailUrl"></video>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, nextTick } from "vue";
import { useRoute } from "vue-router";
import axios from "axios";
import SockJS from "sockjs-client";
import Stomp from "stompjs";
import { useAuthStore } from "@/stores/auth";

export default {
  setup() {
    const route = useRoute();
    const roomId = route.params.id;

    const auth = useAuthStore();
    const room = ref(null);
    const error = ref("");
    const status = ref("");
    const videoUrl = ref("");
    const thumbnailUrl = ref("");
    const videoPlayer = ref(null);
    const joinLink = `${window.location.origin}/watch-party/${roomId}`;

    let stompClient = null;
    const copyJoinLink = async () => {
      await navigator.clipboard.writeText(joinLink); 
      alert("Join link copied to clipboard!");
    };

    const loadRoom = async () => {
      try {
        const res = await axios.get(`/watch-party/${roomId}`);
        room.value = res.data;

        if (res.data.currentVideoId) {
          await loadVideo(res.data.currentVideoId);
        }
      } catch (err) {
        error.value = "Watch party not found.";
      }
    };

    const loadVideo = async (videoId) => {
      const res = await axios.get(`/videoPosts/${videoId}`);
      videoUrl.value = `http://localhost/${res.data.videoPath.replace(/\\/g, '/')}`;
      thumbnailUrl.value = `http://localhost/api/videoPosts/${videoId}/thumbnail`;
    };

    const connectToWatchParty = () => {
      const socket = new SockJS("http://localhost/socket");
      stompClient = Stomp.over(socket);

      stompClient.connect({}, () => {
        stompClient.subscribe(`/topic/watch-party/${roomId}`, (payload) => {
          const event = JSON.parse(payload.body);

          if (event.type === "START" && event.videoId) {
            status.value = "Host started the video.";
            if (String(room.value.currentVideoId) !== String(event.videoId)) {
              room.value.currentVideoId = event.videoId;
              loadVideo(event.videoId);
            }
            nextTick(() => {
              if (videoPlayer.value) {
                videoPlayer.value.play().catch(() => {});
              }
            });
          }

          if (event.type === "STOP") {
            status.value = "Host stopped the video.";
            if (videoPlayer.value) {
              videoPlayer.value.pause();
            }
          }
        });
      }, () => {
        error.value = "Socket connection failed.";
      });
    };

    const startWatchParty = () => {
      if (!room.value || !room.value.currentVideoId) return;
      if (!stompClient || !stompClient.connected) {
        error.value = "Socket is not connected.";
        return;
      }

      const sender = auth.user?.username || auth.user?.email || "unknown";
      stompClient.send(
        `/app/watch-party.start/${roomId}`,
        {},
        JSON.stringify({ videoId: room.value.currentVideoId, sender })
      );
    };

    onMounted(async () => {
      await loadRoom();
      if (!error.value) {
        connectToWatchParty();
      }
    });

    onUnmounted(() => {
      if (stompClient) {
        stompClient.disconnect();
      }
    });

    return {
      room,
      status,
      error,
      videoUrl,
      thumbnailUrl,
      videoPlayer,
      startWatchParty,
      joinLink, 
      copyJoinLink
    };
  }
};
</script>

<style scoped>
.watch-party-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20px;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
}

.info-card {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.top-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.room-details p {
  margin: 4px 0;
  color: #555;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 600px;
}
.room-details p {
  margin: 4px 0;
  color: #555;
}

.host-name {
  font-size: 0.9rem;
  color: #888;
}

.start-btn {
  background: #ff4500;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
}

.start-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.link-row {
  display: flex;
  gap: 8px;
  width: 100%;
}

.link-input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.9rem;
  min-width: 0;
  color: #000 !important;
  background: #fff !important;
}

.copy-btn {
  background: #ff4500;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  font-weight: 600;
  cursor: pointer;
  flex-shrink: 0;
}
.status {
  margin: 12px 0 0 0;
  color: #27ae60;
  font-weight: 500;
}

.error {
  margin: 12px 0 0 0;
  color: #e74c3c;
}

.video-container {
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
}

.video-container video {
  width: 100%;
  display: block;
}
</style>