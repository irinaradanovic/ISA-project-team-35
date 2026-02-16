<template>
  <div class="video-page">
    <div :class="['main-layout', { 'streaming-layout': video.isStreaming }]">

      <div class="video-content">
        <div class="video-container">
          <h2>{{ video.title }}</h2>

          <p class="username">by {{ video.ownerUsername }}</p>

          <div class="video-section">
            <div v-if="isWaiting" class="scheduled-overlay">
              <div class="countdown-box">
                <i class="fas fa-clock"></i>
                <h3>Video starts in:</h3>
                <div class="timer">{{ countdownText }}</div>
                <p>Scheduled for: {{ formatDate(video.scheduledAt) }}</p>
              </div>
            </div>

            <video
                v-if="video.videoPath"
                v-show="!isWaiting"
                ref="videoPlayer"
                controls
                :src="videoUrl"
                :poster="thumbnailUrl"
                @play="onManualPlay"
                @ended="handleVideoEnded"
                @timeupdate="checkVideoProgress"
            ></video>


            <div class="video-meta">
              <div class="like-section">
                <button @click="toggleLike" :class="{ liked: liked }" class="like-btn">
                  <i class="fas fa-thumbs-up"></i>
                </button>
                <span class="like-count">{{ likeCount }}</span>
                <p v-if="likeError" class="error-message">{{ likeError }}</p>
              </div>
              <span class="view-count">
                <i class="fas fa-eye"></i> {{ video.viewCount }} views
              </span>
              <p class="created-at">{{ formatDate(video.createdAt) }}</p>
            </div>
          </div>

          <p class="description">{{ video.description }}</p>

          <div v-if="!video.isStreaming" class="start-wp-button">
            <button 
              v-if="!watchPartyRoomId"
              class="wp-start-btn primary"
              @click="createAndStartWatchParty"
            >
              <i class="fas fa-users"></i> Start Watch Party
            </button>
            <button 
              v-else
              class="wp-start-btn success"
              @click="startWatchParty"
            >
              <i class="fas fa-play"></i> Start Watch Party
            </button>
          </div>

          <div class="extra-info">
            <div v-if="video.city || video.country" class="location-badge">
              <i class="fas fa-map-marker-alt"></i>
              {{ video.city }}, {{ video.country }}
            </div>

            <div class="tags-list">
              <span v-for="tag in splitTags(video.tags)" :key="tag" class="tag-item">
                #{{ tag }}
              </span>
            </div>
          </div>

          <div v-if="!video.isStreaming">
            <div class="comment-form">
              <input
                  v-model="newComment"
                  placeholder="Add a comment..."
                  @keyup.enter="addComment"
              />
              <button @click="addComment" class="comment-submit-btn">Comment</button>
            </div>
            <p v-if="commentError" class="error-message">{{ commentError }}</p>
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
      </div>

      <div v-if="video.isStreaming" class="chat-sidebar">
        <div class="chat-header">Live Chat</div>
        <div class="chat-messages" ref="chatWindow">
          <div v-for="(msg, index) in chatMessages" :key="index" class="chat-bubble">
            <span class="chat-user">{{ msg.sender }}:</span>
            <span class="chat-text">{{ msg.content }}</span>
          </div>
        </div>
        <div class="chat-input-wrapper">
          <input
              v-model="chatInput"
              @keyup.enter="sendChatMessage"
              placeholder="Say something..."
          />
          <button @click="sendChatMessage"><i class="fas fa-paper-plane"></i></button>
        </div>
        <p v-if="chatError" class="chat-error-message">{{ chatError }}</p>
      </div>

    </div>
  </div>
</template>



<script>
import { ref, onMounted, onUnmounted, nextTick } from "vue";
import { useRoute, useRouter } from "vue-router";
import axios from "axios";
import { useAuthStore } from "@/stores/auth";
import SockJS from "sockjs-client";
import Stomp from 'stompjs';

export default {
  setup() {
    const route = useRoute();
    const router = useRouter();
    const video = ref({});
    const comments = ref([]);
    const likeCount = ref(0);
    const likeError = ref("");
    const viewCount = ref(0);
    const commentCount = ref(0);
    const liked = ref(false);
    const newComment = ref("");
    const commentError = ref("");
    const videoUrl = ref("");
    const thumbnailUrl = ref("");
    const auth = useAuthStore();

    //za zakazani prikaz
    const videoPlayer = ref(null);


    const currentPage = ref(0);
    const pageSize = ref(5);
    const hasMoreComments = ref(true);
    const loadingComments = ref(false);
    const chatWindow = ref(null);
    const chatError = ref("");

    //za chat streaming
    const chatMessages = ref([]);
    const chatInput = ref("");
    let stompClient = null;


    //za watch party
    const watchPartyRoomId = ref(route.query.wp || null);
    const watchPartyError = ref("");

    //za zakazani režim
    const isWaiting = ref(false);
    const countdownText = ref("");
    let countdownTimer = null;
    const isEnding = ref(false);

    const startCountdown = (scheduledDate) => {
      // 1. Osiguraj se da je scheduledDate ispravan Date objekat
      const targetDate = new Date(scheduledDate);

      // 2. Odmah očisti stari tajmer ako postoji
      if (countdownTimer) clearInterval(countdownTimer);

      const updateTimer = () => {
        const now = new Date();
        const diff = targetDate.getTime() - now.getTime();

        // 3. Ako je vreme isteklo
        if (diff <= 0) {
          isWaiting.value = false;
          countdownText.value = "00:00:00";
          clearInterval(countdownTimer);
          loadVideo(); // Ponovo učitaj video da krene plejer

          if (video.value.isStreaming && (!stompClient || !stompClient.connected)) {
            connectToChat();
          }

          return;
        }

        // 4. Izračunaj sate, minute i sekunde
        const hours = Math.floor(diff / (1000 * 60 * 60));
        const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
        const seconds = Math.floor((diff % (1000 * 60)) / 1000);

        // 5. Formatiranje nula (da piše 05:09 umesto 5:9)
        const hDisplay = hours.toString().padStart(2, '0');
        const mDisplay = minutes.toString().padStart(2, '0');
        const sDisplay = seconds.toString().padStart(2, '0');

        // 6. AŽURIRANJE PROMENLJIVE KOJA SE VIDI NA EKRANU
        countdownText.value = `${hDisplay}:${mDisplay}:${sDisplay}`;
      };

      // Pokreni odmah jednom da se ne čeka prva sekunda
      updateTimer();
      // Postavi interval
      countdownTimer = setInterval(updateTimer, 1000);
    };

    const handleVideoEnded = async () => {
      // AKO JE VEĆ KRENULO GAŠENJE, NE RADI NIŠTA VIŠE
      if (isEnding.value) return;



      // Koristimo podatak direktno iz reaktivnog objekta
      if (video.value && video.value.isStreaming) {
        isEnding.value = true; // ODMAH POSTAVI NA TRUE
        console.log("Video je završen. Šaljem sistemsku poruku...");

        // 1. Slanje poruke
        if (stompClient && stompClient.connected) {
          const closingMessage = {
            videoId: video.value.id, // Sigurniji pristup ID-u
            sender: "SYSTEM",
            content: "Stream has ended. Converting to video... Thank you for watching!",
            timestamp: new Date()
          };
          stompClient.send(`/app/chat.send/${video.value.id}`, {}, JSON.stringify(closingMessage));
        }

        // 2. Tajmer za tranziciju
        setTimeout(async () => {
          try {
            console.log("Ažuriram backend status za video:", video.value.id);
            await axios.post(`http://localhost/api/videoPosts/${video.value.id}/end-stream`);

            // KLJUČNO: Menjamo status koji kontroliše v-if i klase
            video.value.isStreaming = false;

            // 3. Čistimo socket
            if (stompClient) {
              stompClient.disconnect();
              stompClient = null;
            }

            // 4. Forsirano učitavanje komentara za VOD mod
            currentPage.value = 0;
            hasMoreComments.value = true;
            comments.value = [];
            await loadComments(false);

            console.log("Layout uspešno prebačen u VOD mod.");
          } catch (err) {
            console.error("Greška pri gašenju streama:", err);
            // Čak i ako backend fejlira, prebaci UI lokalno da korisnik ne ostane zaglavljen
            video.value.isStreaming = false;
          }
          finally {
            isEnding.value = false; // Resetuj za svaki slučaj na kraju
          }
        }, 5000);
      }
    };

    const checkVideoProgress = () => {
      const player = videoPlayer.value;
      if (!player) return;

      // Ako je ostalo manje od pola sekunde do kraja, a još uvek smo u isStreaming modu
      if (player.duration > 0 && (player.duration - player.currentTime < 0.5)) {
        if (video.value.isStreaming) {
          console.log("TimeUpdate detektovao kraj videa pre @ended događaja!");
          handleVideoEnded();
        }
      }
    };



    const splitTags = (tagsString) => {
      if (!tagsString) return [];
      // Razdvaja string po zarezu i briše prazna mesta
      return tagsString.split(',').map(t => t.trim());
    };

    const videoId = route.params.id;

    // --------CHAT STREAMING--------
    const connectToChat = () => {
      console.log("Inicijalizacija chata za video:", videoId);
      // Povezujemo se na endpoint koji si definisao u WebSocketConfig (/socket)
      const socket = new SockJS("http://localhost/socket");
      stompClient = Stomp.over(socket);

      stompClient.connect({}, (frame) => {
        console.log("Connected to chat: " + frame);

        // Pretplata na topic za specifican video
        stompClient.subscribe(`/topic/chat/${videoId}`, (sdkEvent) => {
          onMessageReceived(sdkEvent);
        });
      }, (error) => {
        console.error("Socket error:", error);
      });
    };

    const onMessageReceived = (payload) => {
      const message = JSON.parse(payload.body);
      chatMessages.value.push(message);

      // Auto-scroll na dno
      nextTick(() => {
        if (chatWindow.value) {
          chatWindow.value.scrollTop = chatWindow.value.scrollHeight;
        }
      });
    };

    const sendChatMessage = () => {
      //  Provera da li je korisnik ulogovan
      chatError.value =  "";
      const currentUser = auth.user;
      const username = currentUser.username
      if (!auth.token || !username) {
        chatError.value = "You must be logged in to participate in the chat.";
        setTimeout(() => { chatError.value = "" }, 3000);
        return;
      }
      if (chatInput.value.trim() && stompClient && stompClient.connected) {
        const chatMessage = {
          videoId: videoId,
          sender: username,
          content: chatInput.value,
          timestamp: new Date()
        };

        // Slanje na @MessageMapping("/chat.send/{videoId}")
        console.log("Slanje poruke:", chatMessage);
        stompClient.send(`/app/chat.send/${videoId}`, {}, JSON.stringify(chatMessage));
        chatInput.value = "";
        chatError.value = ""; // Resetuj grešku ako je postojala
      }
    };
    //---------------------------------

    /*const loadVideo = async () => {
      const res = await axios.get(`http://localhost/api/videoPosts/${videoId}`);
      video.value = res.data;
      videoUrl.value = `http://localhost/${res.data.videoPath.replace(/\\/g, '/')}`;
      thumbnailUrl.value = `http://localhost/api/videoPosts/${videoId}/thumbnail`;
      likeCount.value = res.data.likeCount;
      viewCount.value = res.data.viewCount;
      commentCount.value = res.data.commentCount;

   try {
          const likedRes = await axios.get(`http://localhost/api/videoPosts/${videoId}/liked`);
          liked.value = likedRes.data;
        } catch (err) {
          liked.value = false;
        }
    };*/

    //za zakazani režim
    const loadVideo = async () => {
      try {
        // 1. Resetuj stanje pre učitavanja
        isWaiting.value = false;
        if (countdownTimer) clearInterval(countdownTimer);

        // 2. Poziv API-ja
        const res = await axios.get(`http://localhost/api/videoPosts/${videoId}/play`);
        const data = res.data;
        video.value = data;

        // 3. Podešavanje putanja
        //videoUrl.value = `http://localhost/${data.videoPath.replace(/\\/g, '/')}`;
        const normalizedPath = data.videoPath.replace(/\\/g, '/');
        videoUrl.value = `http://localhost/${normalizedPath}`;


        thumbnailUrl.value = `http://localhost/api/videoPosts/${videoId}/thumbnail`;

        // 4. Ažuriranje brojača
        likeCount.value = data.likeCount;
        viewCount.value = data.viewCount;
        commentCount.value = data.commentCount;

        // 5. UČITAVANJE KOMENTARA (Ovo je nedostajalo ili bagovalo)
        currentPage.value = 0;
        hasMoreComments.value = true;
        comments.value = [];
        await loadComments(false);

        // 6. LOGIKA ZA ZAKAZANI PRIKAZ

        const now = new Date().getTime();
        const scheduledAtTime = data.scheduledAt ? new Date(data.scheduledAt).getTime() : 0;


        if (scheduledAtTime > now) {
          // VIDEO JE U BUDUĆNOSTI
          isWaiting.value = true;
          console.log("Video je zakazan za:", new Date(scheduledAtTime).toLocaleString());
          startCountdown(new Date(scheduledAtTime));
        } else {
          // VIDEO JE DOSTUPAN ILI JE STREAM U TOKU
          isWaiting.value = false;

          nextTick(() => {
            if (videoPlayer.value) {
              console.log("Trajanje videa:", videoPlayer.value.duration);
              console.log("Trenutno vreme (offset):", videoPlayer.value.currentTime);

              // Postavi offset ako postoji (za simulaciju striminga)
              if (data.offsetSeconds > 0) {
                videoPlayer.value.currentTime = data.offsetSeconds;
              }

              // Automatski Play (Browseri dozvoljavaju ako je korisnik kliknuo na video u listi)
              videoPlayer.value.play().catch(err => {
                console.warn("Autoplay blocked. User must interact with the page first.", err);
              });
            }
          });
        }

        // 7. Provera lajka
        if (auth.token) {
          try {
            const likedRes = await axios.get(`http://localhost/api/videoPosts/${videoId}/liked`);
            liked.value = likedRes.data;
          } catch (err) {
            liked.value = false;
          }
        }

      } catch (err) {
        console.error("Greška pri učitavanju videa:", err);
        setTimeout(loadVideo, 5000); // Ako pukne skroz, probaj opet za 5s
      }
    };









    const loadComments = async (append = false) => {
      if (loadingComments.value || !hasMoreComments.value) return;
      console.log('Loading comments - page:', currentPage.value, 'append:', append);
      loadingComments.value = true;
      try {
        const res = await axios.get(`http://localhost/api/comments/${videoId}/comments`, {
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
      likeError.value = "";
      if (!auth.token) {
        likeError.value = "You must be logged in to like videos.";
        return;
      }

    try {
      const res = await axios.post(`http://localhost/api/videoPosts/${videoId}/like`);
      likeCount.value = res.data;
      liked.value = !liked.value;
    } catch (err) {
      console.error("Error toggling like:", err);
      if (err.response?.status === 401) {
        likeError.value = "Your session has expired. Please log in again.";
      } else {
        likeError.value = "Failed to like video. Please try again.";
      }
    }
    };

const addComment = async () => {
  commentError.value = "";

  if (!auth.token) {
    commentError.value = "You must be logged in to comment.";
    return;
  }

  if (!newComment.value.trim()) return;

  try {
    await axios.post(`http://localhost/api/comments/${videoId}`, { content: newComment.value });
    currentPage.value = 0;
    hasMoreComments.value = true;
    await loadComments(false);
    newComment.value = "";
  } catch (err) {
    console.error("Error adding comment:", err);
    if (err.response?.status === 403) {
      commentError.value = "You must be logged in to comment.";
    } else if (err.response?.status === 401) {
      commentError.value = "Your session has expired. Please log in again.";
    } else if (err.response?.status === 400) {
      commentError.value = err.response.data?.message || "Invalid comment.";
    } else {
      commentError.value = "Failed to post comment. Please try again.";
    }
  }
};
const createAndStartWatchParty = async () => {
  watchPartyError.value = "";
  if (!auth.token) {
    watchPartyError.value = "You must be logged in to create a watch party.";
    return;
  }

  try {
    const res = await axios.post("/watch-party", {
      videoId: Number(videoId)
    });
    watchPartyRoomId.value = res.data.id;

    router.push({
      path: `/watch-party/${res.data.id}`
    });
  } catch (err) {
    watchPartyError.value = "Failed to create watch party.";
  }
};

const startWatchParty = () => {
  if (!watchPartyRoomId.value) return;

  router.push({
    path: `/watch-party/${watchPartyRoomId.value}`
  });
};


    /* const incrementView = async () => {
       try {
         await axios.post(`http://localhost/api/videoPosts/${videoId}/view`);
       } catch (err) {
         console.error("Error incrementing view count:", err);
       }
     };   */

    const formatDate = (dateStr) => {
      if (!dateStr) return "";
      return new Date(dateStr).toLocaleString();
    };

    onMounted(async () => {

      window.addEventListener('scroll', handleScroll);

      await loadVideo();

      console.log("Status streaminga nakon loada:", video.value.isStreaming);

      if (video.value.isStreaming) {
        console.log("Pokrećem konekciju na chat...");
        connectToChat();
      }

      await loadComments();
      //window.addEventListener('scroll', handleScroll);
      /*if (video.value.isStreaming) { //STREAMING CHAT DOZVOLJEN SAMO KADA JE VIDEO U STREAM MODU
        console.log("Video je streaming, povezujem se na chat...");
        connectToChat();
      }*/
    });

    onUnmounted(() => {
      window.removeEventListener('scroll', handleScroll);
      if (stompClient !== null) {
        stompClient.disconnect();
      }
      if (countdownTimer) {
        clearInterval(countdownTimer);
      }
    });

    return {
      video,
      comments,
      likeCount,
      commentCount,
      liked,
      likeError,
      newComment,
      commentError,
      videoUrl,
      thumbnailUrl,
      loadingComments,
      toggleLike,
      addComment,
      formatDate,
      splitTags,
      chatMessages,
      chatInput,
      chatWindow,
      sendChatMessage,
      chatError,

      watchPartyRoomId,
      watchPartyError,
      createAndStartWatchParty,
      startWatchParty,

      videoPlayer,
      isWaiting,
      countdownText,
      startCountdown,
      handleVideoEnded,
      checkVideoProgress,
      isEnding,

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

.scheduled-overlay {
  width: 100%;
  aspect-ratio: 16 / 9; /* Da prati dimenzije videa */
  background: #1a1a1a;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  border-radius: 8px;
  text-align: center;
}

.countdown-box i {
  font-size: 3rem;
  color: #ff3d00;
  margin-bottom: 15px;
}

.timer {
  font-size: 2.5rem;
  font-weight: bold;
  font-family: 'Courier New', Courier, monospace;
  margin: 10px 0;
  color: #ff3d00;
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
.error-message { 
  color: #d32f2f;
  background-color: #ffebee;
  border: 1px solid #ef5350;
  border-radius: 6px;
  padding: 10px 14px;
  margin: 10px 0 20px 0;
  font-size: 0.9em;
  font-weight: 500;
}

.main-layout {
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-width: 1300px;
  margin: 0 auto;
}
.streaming-layout {
  flex-direction: row;
  align-items: flex-start;
}

.video-content { flex: 3; min-width: 0; } /* Video zauzima više mesta */

/* Dodaj ovo u <style scoped> */
.chat-bubble.system-msg {
  background-color: #fff3e0 !important; /* Svetlo narandžasta */
  border: 1px solid #ff9800 !important;
  color: #e65100 !important;
  padding: 12px !important;
  margin: 10px 0;
  border-radius: 8px;
  font-weight: bold;
  text-align: center;
  animation: pulse 1.5s infinite; /* Blago blinkanje da privuče pažnju */
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.7; }
  100% { opacity: 1; }
}

.chat-sidebar {
  flex: 1;
  min-width: 350px;
  max-width: 400px;
  height: 600px; /* Fiksna visina chat-a */
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 20px; /* Chat prati skrol */
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}


.chat-header {
  padding: 15px;
  border-bottom: 1px solid #eee;
  font-weight: bold;
  text-align: center;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  background-color: #fcfcfc;
}

.chat-bubble {
  font-size: 0.95rem;
  line-height: 1.4;
}
.chat-user {
  font-weight: 700;
  color: #555;
  margin-right: 8px;
}

.chat-text {
  color: #333;
}

.chat-input-wrapper {
  padding: 15px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 10px;
}

.chat-input-wrapper input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 20px;
  outline: none;
}

.chat-input-wrapper button {
  background: none;
  border: none;
  color: #065fd4;
  font-size: 1.2rem;
  cursor: pointer;
}
</style>

