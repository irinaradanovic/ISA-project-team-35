<template>
  <div class="video-form-container">
    <div v-if="!authStore.token" class="auth-warning">
      <h2>Please log in</h2>
      <p>You must be logged in to upload videos to Jutjubić.</p>
    </div>
    <template v-else>
    <h2>Create new video post</h2>
    <form @submit.prevent="submitPost" enctype="multipart/form-data">
      <div class="form-group">
        <label>Title</label>
        <small class="hint">
          Required, max 100 characters, short but descriptive
        </small>

        <input
            type="text"
            v-model="title"
            maxlength="100"
            required
        />

        <div class="char-counter">
          {{ title.length }}/100
        </div>
      </div>


      <div class="form-group">
        <label>Description</label>
        <small class="hint">
          Required, max 500 characters, briefly explain what the video is about
        </small>

        <textarea
            v-model="description"
            maxlength="500"
            required
        ></textarea>

        <div class="char-counter">
          {{ description.length }}/500
        </div>
      </div>


      <div class="form-group">
        <label>Tags:</label>
        <small class="hint">
          Required, Choose one or more tag to describe your video
        </small>

        <div class="tag-list">
    <span
        v-for="tag in availableTags"
        :key="tag"
        class="tag"
        :class="{ selected: selectedTags.includes(tag) }"
        @click="toggleTag(tag)"
    >
      {{ tag }}
    </span>
        </div>
      </div>


      <div class="form-group">
        <label>Thumbnail:</label>
        <small class="hint">
          Required, choose a photo that catches viewers attention
        </small>

        <input type="file" ref="thumbnailInput" @change="onThumbnailSelected" accept="image/*" />

        <div v-if="thumbnailPreview" class="preview-container">
          <img :src="thumbnailPreview" class="thumbnail-preview" />
          <button type="button" class="remove-btn" @click="removeThumbnail">
            Remove
          </button>
        </div>
      </div>


      <div class="form-group">
        <label>Video:</label>
        <small class="hint">
          Required, only .mp4 files, max 200MB
        </small>

        <input type="file"ref="videoInput" @change="onVideoSelected" accept="video/mp4" />

        <div v-if="videoPreview" class="preview-container">
          <video
              :src="videoPreview"
              controls
              muted
              class="video-preview"
          ></video>

          <button type="button" class="remove-btn" @click="removeVideo">
            Remove video
          </button>
        </div>
      </div>


      <div class="form-group">
        <label>Location(optional):</label>
        <input
            type="text"
            v-model="locationInput"
            @input="filterLocations"
            placeholder="Enter the city name..."
        />

        <ul v-if="filteredLocations.length" class="location-list">
          <li
              v-for="loc in filteredLocations"
              :key="loc"
              @click="selectLocation(loc)"
          >
            {{ loc }}
          </li>
        </ul>
      </div>

      <!-- ogranicava korisnika da mora da unese ova polja-->
      <button
          type="submit"
          class="submit-btn"
          :disabled="isDisabled"
          :title="isDisabled ? disabledReason : ''"
      >
        Post
      </button>

    </form>
    </template>

    <div v-if="message" :class="{ success: success, error: !success }" class="message">
      {{ message }}
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

export default {
  setup() {
    const router = useRouter();
    const authStore = useAuthStore();
    return { router, authStore };
  },
  data() {
    return {
      title: '',
      description: '',
      tags: '',
      location: '',
      thumbnail: null,
      video: null,
      message: '',
      success: false,
      availableTags: [
        'Music',
        'Vlog',
        'Travel',
        'Food',
        'Education',
        'Gaming',
        'Comedy',
        'Fitness'
      ],
      selectedTags: [],
      locationInput: '',
      locations: [
        'Beograd',
        'Novi Sad',
        'Niš',
        'Kragujevac',
        'Subotica',
        'Zagreb',
        'Sarajevo',
        'Split',
        'Ljubljana'
      ],
      filteredLocations: [],
      thumbnailPreview: null,
      videoPreview: null,



    };
  },
  computed:{
    disabledReason(){
      return "Please fill out all required fields"
    },
    isDisabled() {
      return !this.title || !this.description || !this.video || !this.thumbnail || this.selectedTags.length === 0;
    },
  },
  methods: {
    onThumbnailSelected(event) {
      const file = event.target.files[0];
      if (!file) return;

      this.thumbnail = file;
      this.thumbnailPreview = URL.createObjectURL(file);
    },

    removeThumbnail() {
      this.thumbnail = null;
      this.thumbnailPreview = null;
      //resetuje input na prazno ako korisnik ukloni thumbnail
      if (this.$refs.thumbnailInput) {
        this.$refs.thumbnailInput.value = '';
      }
    },


    onVideoSelected(event) {
      const file = event.target.files[0];
      if (!file) return;

      // validate size
      if (file.size > 200 * 1024 * 1024) {
        this.message = "Video is too large. Maximum allowed size is 200MB.";
        this.success = false;
        this.$refs.videoInput.value = "";
        this.video = null;
        this.videoPreview = null;
        return;
      }

      // validate type
      if (file.type !== "video/mp4") {
        this.message = "Only MP4 videos are allowed.";
        this.success = false;
        this.$refs.videoInput.value = "";
        this.video = null;
        this.videoPreview = null;
        return;
      }

      this.message = "";
      this.success = true;

      this.video = file;
      this.videoPreview = URL.createObjectURL(file);
    },


    removeVideo() {
      this.video = null;
      this.videoPreview = null;
//resetuje input na prazno ako korisnik ukloni video
      if (this.$refs.videoInput) {
        this.$refs.videoInput.value = '';
      }
    },


    toggleTag(tag) {
      if (this.selectedTags.includes(tag)) {
        this.selectedTags = this.selectedTags.filter(t => t !== tag);
      } else {
        this.selectedTags.push(tag);
      }
    },
    filterLocations() {
      const input = this.locationInput.toLowerCase();
      this.filteredLocations = this.locations.filter(loc =>
          loc.toLowerCase().startsWith(input)
      );
    },

    selectLocation(loc) {
      this.locationInput = loc;
      this.filteredLocations = [];
    },

    async submitPost() {

      if (!this.title.trim()) {
        this.message = "Title is required.";
        this.success = false;
        return;
      }

      if (!this.description.trim()) {
        this.message = "Description is required.";
        this.success = false;
        return;
      }

      if (this.selectedTags.length === 0) {
        this.message = "Please select at least one tag.";
        this.success = false;
        return;
      }

      if (!this.thumbnail || !this.video) {
        this.message = "Please upload both thumbnail and video.";
        this.success = false;
        return;
      }

      if (this.title.length > 100) {
        this.message = "Title cannot exceed 100 characters.";
        this.success = false;
        return;
      }

      if (this.description.length > 500) {
        this.message = "Description cannot exceed 500 characters.";
        this.success = false;
        return;
      }


      const formData = new FormData();
      formData.append('title', this.title);
      formData.append('description', this.description);
      formData.append('tags', this.selectedTags.join(','));
      formData.append('location', this.locationInput);
      formData.append('thumbnail', this.thumbnail);
      formData.append('video', this.video);

      const confirmed = confirm("Are you sure you want to upload this video?");
      if (!confirmed) return;

      try {
        const res = await axios.post('http://localhost:8080/api/videoPosts', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });

        alert("Video posted successfully!");
        const videoId = res.data.id;
        this.router.push(`/video/${videoId}`);

      } catch (err) {
        alert("Upload failed. Rolling back...");
        this.resetForm();
      }
    },

    resetForm() {
      this.title = "";
      this.description = "";
      this.selectedTags = [];
      this.thumbnail = null;
      this.video = null;
      this.thumbnailPreview = null;
      this.videoPreview = null;
      this.locationInput = "";
      this.filteredLocations = [];

      if (this.$refs.videoInput) this.$refs.videoInput.value = "";
      if (this.$refs.thumbnailInput) this.$refs.thumbnailInput.value = "";
    }

  }
};
</script>

<style scoped>

.video-form-container {
  max-width: 900px;
  margin: 40px auto;
  padding: 32px;
  background: white;
  border-radius: 14px;
  box-shadow: 0 15px 35px rgba(0,0,0,0.08);
}

.video-preview {
  width: 100%;
  max-height: 260px;
  border-radius: 8px;
  margin-top: 10px;
  border: 1px solid #ddd;
}

.preview-container {
  margin-top: 10px;
  position: relative;
  display: inline-block;
}


.char-counter {
  text-align: right;
  font-size: 12px;
  color: #666;
}


.thumbnail-preview {
  width: 200px;
  height: auto;
  border-radius: 8px;
  border: 1px solid #ddd;
}

.remove-btn {

  padding: 5px 10px;
  background: #d4cbcb;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
}

.remove-btn:hover {
  background: #bfb4b4;
}


.form-group {
  display: flex;
  flex-direction: column;
  margin-bottom: 15px;
}


.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.video-form-container {
  max-width: 650px;
  margin: 40px auto;
  padding: 28px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.08);
}


.location-list {
  list-style: none;
  padding: 0;
  margin: 4px 0 0;
  border: 1px solid #ccc;
  border-radius: 4px;
  background: white;
  max-height: 150px;
  overflow-y: auto;
}

.location-list li {
  padding: 8px;
  cursor: pointer;
}

.location-list li:hover {
  background: #eee;
}



</style>
