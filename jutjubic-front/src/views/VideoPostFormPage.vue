<template>
  <div class="video-form-container">
    <h2>Create new video post</h2>
    <form @submit.prevent="submitPost" enctype="multipart/form-data">
      <div class="form-group">
        <label>Title:</label>
        <input type="text" v-model="title" required />
      </div>

      <div class="form-group">
        <label>Description:</label>
        <textarea v-model="description" required></textarea>
      </div>

      <div class="form-group">
        <label>Tags:</label>

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

        <input type="file" ref="thumbnailInput" @change="onThumbnailSelected" accept="image/*" />

        <div v-if="thumbnailPreview" class="preview-container">
          <img :src="thumbnailPreview" class="thumbnail-preview" />
          <button type="button" class="remove-btn" @click="removeThumbnail">
            Ukloni
          </button>
        </div>
      </div>


      <div class="form-group">
        <label>Video (mp4, max 200MB):</label>

        <input type="file"ref="videoInput" @change="onVideoSelected" accept="video/mp4" />

        <div v-if="videoPreview" class="preview-container">
          <video
              :src="videoPreview"
              controls
              muted
              class="video-preview"
          ></video>

          <button type="button" class="remove-btn" @click="removeVideo">
            Ukloni video
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


      <button type="submit" class="submit-btn">Post</button>
    </form>

    <div v-if="message" :class="{ success: success, error: !success }" class="message">
      {{ message }}
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import { useRouter } from 'vue-router';

export default {
  setup() {
    const router = useRouter();
    return { router };
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
      if (!this.thumbnail || !this.video) {
        this.message = "Molimo izaberite video i thumbnail!";
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

      try {
        const res = await axios.post('http://localhost:8080/api/videoPosts', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });

        this.message = "Video postavljen uspešno!";
        this.success = true;

        // Nakon uspešnog postovanja, ide na stranicu za prikaz videa
        const videoId = res.data.id;
        this.router.push(`/video/${videoId}`);
      } catch (err) {
        console.error(err);
        this.message = "Greška pri upload-u: " + (err.response?.data || err.message);
        this.success = false;
      }
    }
  }
};
</script>

<style scoped>
* {
  font-family: 'Inter', sans-serif;
}

.video-form-container {
  max-width: 600px;
  margin: 30px auto;
  padding: 20px;
  background: #f7f7f7;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.video-preview {
  width: 100%;
  max-height: 260px;
  border-radius: 8px;
  margin-top: 10px;
  border: 1px solid #ddd;
}
.tag:hover {
  background: #ccc;
}
input:focus,
textarea:focus {
  outline: none;
  border-color: #ff3d00;
  box-shadow: 0 0 0 2px rgba(255,61,0,0.15);
}
.preview-container {
  margin-top: 10px;
  position: relative;
  display: inline-block;
}

.thumbnail-preview {
  width: 200px;
  height: auto;
  border-radius: 8px;
  border: 1px solid #ddd;
}

.remove-btn {
  margin-top: 6px;
  padding: 6px 12px;
  background: #eee;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
}

.remove-btn:hover {
  background: #ddd;
}


h2 {
  text-align: center;
  margin-bottom: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  margin-bottom: 15px;
}

input[type="text"],
textarea,
input[type="file"] {
  padding: 8px;
  font-size: 14px;
  border-radius: 4px;
  border: 1px solid #ccc;
}

textarea {
  resize: vertical;
  min-height: 80px;
}

.submit-btn {
  width: 100%;
  padding: 10px;
  background-color: #ff3d00;
  color: white;
  font-size: 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.submit-btn:hover {
  background-color: #e63900;
}

.message {
  margin-top: 15px;
  text-align: center;
}

.success { color: green; }
.error { color: red; }

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


.tag {
  padding: 6px 12px;
  border-radius: 16px;
  background: #ddd;
  cursor: pointer;
  font-size: 14px;
}

.tag.selected {
  background: #ff3d00;
  color: white;
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
