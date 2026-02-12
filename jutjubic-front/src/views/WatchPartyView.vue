<template>
  <div class="watch-party-room">
    <div class="room-card">
      <h2>Watch Party</h2>
      <p v-if="room" class="room-info">Room: <strong>{{ room.roomName }}</strong></p>
      <p v-if="room" class="room-info">Host: <strong>{{ room.creatorUsername }}</strong></p>
      <p v-if="status" class="status">{{ status }}</p>
      <p v-if="error" class="error">{{ error }}</p>
      <p class="hint">Keep this page open. The video opens when the host starts.</p>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import axios from "axios";
import SockJS from "sockjs-client";
import Stomp from "stompjs";

export default {
  setup() {
    const route = useRoute();
    const router = useRouter();
    const roomId = route.params.id;

    const room = ref(null);
    const status = ref("Waiting for the host to start...");
    const error = ref("");
    let stompClient = null;

    const loadRoom = async () => {
      try {
        const res = await axios.get(`/watch-party/${roomId}`);
        room.value = res.data;

        if (res.data.active && res.data.currentVideoId) {
          router.push({
            path: `/video/${res.data.currentVideoId}`,
            query: { wp: roomId }
          });
          return;
        }
      } catch (err) {
        error.value = "Watch party not found.";
      }
    };

    const connectToWatchParty = () => {
      const socket = new SockJS("http://localhost/socket");
      stompClient = Stomp.over(socket);

      stompClient.connect({}, () => {
        stompClient.subscribe(`/topic/watch-party/${roomId}`, (payload) => {
          const event = JSON.parse(payload.body);
          
          if (event.type === "START" && event.videoId) {
            router.push({
              path: `/video/${event.videoId}`,
              query: { wp: roomId }
            });
          } else if (event.type === "STOP") {
            status.value = "Host stopped the video.";
          }
        });
      }, () => {
        error.value = "Socket connection failed.";
      });
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

    return { room, status, error };
  }
};
</script>

<style scoped>
.watch-party-room {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f7f7f7;
  padding: 20px;
}

.room-card {
  background: #ffffff;
  border-radius: 10px;
  padding: 24px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  text-align: center;
}

.room-card h2 {
  margin-top: 0;
  margin-bottom: 20px;
}

.room-info {
  margin: 12px 0;
  color: #333;
}

.status {
  margin-top: 16px;
  color: #333333;
  font-weight: 500;
}

.error {
  margin-top: 16px;
  color: #c0392b;
}

.hint {
  margin-top: 20px;
  color: #666666;
  font-size: 0.9em;
}
</style>