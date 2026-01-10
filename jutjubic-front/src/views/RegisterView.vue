<template>
  <div class="register-page">
    <div class="register-card">
      <h2>Register</h2>

      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label>First name</label>
          <input v-model="firstName" type="text" required />
        </div>

        <div class="form-group">
          <label>Last name</label>
          <input v-model="lastName" type="text" required />
        </div>

        <div class="form-group">
          <label>Username</label>
          <input v-model="username" type="text" required />
        </div>

        <div class="form-group">
          <label>Email</label>
          <input v-model="email" type="email" required />
        </div>

        <div class="form-group">
          <label>Address</label>
          <input v-model="address" type="text" required />
        </div>

        <div class="form-group">
          <label>Password</label>
          <input v-model="password" type="password" required />
        </div>

        <div class="form-group">
          <label>Confirm password</label>
          <input v-model="confirmPassword" type="password" required />
        </div>

        <button type="submit" :disabled="loading">
          {{ loading ? 'Registering...' : 'Register' }}
        </button>

        <!-- Frontend validation error -->
        <p v-if="localError" class="error">{{ localError }}</p>

        <!-- Backend error -->
        <p v-if="auth.error" class="error">{{ auth.error }}</p>
      </form>

      <p class="login-link">
        Already have an account?
        <router-link to="/login">Login</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';

const auth = useAuthStore();
const router = useRouter();

const firstName = ref('');
const lastName = ref('');
const username = ref('');
const email = ref('');
const address = ref('');
const password = ref('');
const confirmPassword = ref('');

const localError = ref(null);
const loading = ref(false);

const handleRegister = async () => {
  localError.value = null;
  auth.error = null;

  if (password.value !== confirmPassword.value) {
    localError.value = 'Passwords do not match';
    return;
  }

  loading.value = true;

  const success = await auth.doRegister({
    firstName: firstName.value,
    lastName: lastName.value,
    username: username.value,
    email: email.value,
    address: address.value,
    password: password.value,
    confirmPassword: confirmPassword.value
  });

  loading.value = false;

  if (success) {
    alert('Registration successful! Please log in.');
    router.push('/login');
  }
};
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f9f9f9;
}

.register-card {
  width: 100%;
  max-width: 400px;
  padding: 2rem;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

h2 {
  text-align: center;
  margin-bottom: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  margin-bottom: 1rem;
}

label {
  font-size: 0.9rem;
  margin-bottom: 0.3rem;
}

input {
  padding: 0.5rem;
  border-radius: 4px;
  border: 1px solid #ccc;
}

button {
  width: 100%;
  padding: 0.6rem;
  background-color: #065fd4;
  color: white;
  border: none;
  border-radius: 4px;
  font-weight: bold;
  cursor: pointer;
}

button:disabled {
  background-color: #9bbcf0;
  cursor: not-allowed;
}

.error {
  color: red;
  margin-top: 0.7rem;
  font-size: 0.9rem;
  text-align: center;
}

.login-link {
  text-align: center;
  margin-top: 1rem;
  font-size: 0.9rem;
}
</style>
