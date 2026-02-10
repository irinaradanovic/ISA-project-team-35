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
            @input="onLocationInput"
            placeholder="Start typing an address (e.g. Bulevar osl...)"
        />

        <ul v-if="filteredLocations.length" class="location-list">
          <li
              v-for="(loc, index) in filteredLocations"
              :key="index"
              @click="selectLocation(loc)"
          >
            {{ loc.display_name }}
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
import cities from '@/assets/cities.json';


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
      selectedLocation: null,
      cities: cities,
      thumbnailPreview: null,
      videoPreview: null,
      locationInput: '',
      filteredLocations: [],
      searchTimeout: null, // Za debounce
      apiKey: 'pk.5b03a9b9443bea45a4a22ec87c997a55' // token za locationiq



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
      const input = this.locationInput.trim().toLowerCase();

      // Ako je input prazan → prikaži sve
      if (!input) {
        this.filteredLocations = [...this.cities];
        return;
      }

      this.filteredLocations = this.cities.filter(c =>
          c.city.toLowerCase().startsWith(input) ||
          c.country.toLowerCase().startsWith(input)
      );
    },

   /* selectLocation(cityObj) {
      this.selectedLocation = cityObj;
      this.locationInput = `${cityObj.city}, ${cityObj.country}`;
      this.filteredLocations = [];
    }, */


    onLocationInput() {
      // Očisti prethodni tajmer (ako korisnik nastavi da kuca brzo)
      clearTimeout(this.searchTimeout);

      const query = this.locationInput.trim();
      if (query.length < 3) {
        this.filteredLocations = [];
        return;
      }

      // Čekaj 300ms nakon što korisnik prestane da kuca
      this.searchTimeout = setTimeout(async () => {
        try {
          const url = `https://us1.locationiq.com/v1/search.php?key=${this.apiKey}&q=${query}&format=json&addressdetails=1&limit=5`;
          // Koristimo fetch jer on ignoriše Axios presretače (interceptors)
          const response = await fetch(url);

          if (!response.ok) {
            throw new Error('LocationIQ request failed');
          }

          const data = await response.json();
          this.filteredLocations = data;

          console.log("Rezultati sa LocationIQ:", data); // Za debug
        } catch (error) {
          console.error("Geocoding autocomplete failed", error);
          this.filteredLocations = [];
        }
      }, 300);
    },

    selectLocation(loc) {
      // loc.display_name je pun opis lokacije
      this.locationInput = loc.display_name;
      this.filteredLocations = [];

      // LocationIQ vraća podatke u 'address' objektu ako je dodat parametar addressdetails=1
      const addr = loc.address || {};

      this.selectedLocation = {
        // Pokušavamo da nađemo grad, ako nema, uzimamo city_district ili town
        city: loc.address.city || loc.town || loc.village || "",
        country: loc.address.country || "",
        latitude: parseFloat(loc.lat), // LocationIQ vraća lat/lon kao stringove, pretvaramo u brojeve
        longitude: parseFloat(loc.lon),
        address: loc.display_name

      };
      console.log("Selektovana lokacija spremna za slanje:", this.selectedLocation);
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

      if (this.locationInput && !this.selectedLocation) {
        this.message = "Please select a location from the list.";
        this.success = false;
        return;
      }

      const formData = new FormData();
      formData.append('title', this.title);
      formData.append('description', this.description);
      formData.append('tags', this.selectedTags.join(','));


      // Ako je korisnik izabrao lokaciju iz liste
      if (this.selectedLocation) {
        formData.append('city', this.selectedLocation.city);
        formData.append('country', this.selectedLocation.country);
        formData.append('latitude', this.selectedLocation.latitude);
        formData.append('longitude', this.selectedLocation.longitude);
        formData.append('address', this.selectedLocation.address);
      }
      formData.append('thumbnail', this.thumbnail);
      formData.append('video', this.video);

      const confirmed = confirm("Are you sure you want to upload this video?");
      if (!confirmed) return;

      try {
        const res = await axios.post('http://localhost/api/videoPosts', formData, {
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
  position: static;
  z-index: 1000;
  width: 100%;
  list-style: none;
  padding: 0;
  margin-top: 4px;
  border: 1px solid #ccc;
  border-radius: 6px;
  background: white;
  max-height: 180px;
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
