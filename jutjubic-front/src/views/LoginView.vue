<template>
  <div class="login-page">
    <h2>Login</h2>
    <form @submit.prevent="handleLogin">
      <div class="form-group">
        <label for="email">Email:</label>
        <input
            id="email"
            v-model="email"
            type="email"
            placeholder="Enter your email"
            required
        />
      </div>

      <div class="form-group">
        <label for="password">Password:</label>
        <input
            id="password"
            v-model="password"
            type="password"
            placeholder="Enter your password"
            required
        />
      </div>

      <button type="submit" class="btn">Login</button>
    </form>

    <!-- Prikaz greške -->
    <p v-if="auth.error" class="error-message">{{ auth.error }}</p>

    <!-- Link ka registraciji -->
    <p class="register-link">
      Don't have an account?
      <router-link to="/register">Register here</router-link>
    </p>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';

const auth = useAuthStore();
const router = useRouter();

const email = ref('');
const password = ref('');

const handleLogin = async () => {
  auth.error = null; // resetuj greške pre login-a
  const success = await auth.doLogin({ email: email.value, password: password.value });
  if (success) {
    router.push('/'); // ide na home nakon uspešnog logina
  }
};
</script>

<style scoped>
.login-page {
  max-width: 400px;
  margin: 3rem auto;
  padding: 2rem;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #030303;
}

.form-group {
  margin-bottom: 1rem;
}

label {
  display: block;
  font-weight: 500;
  margin-bottom: 0.3rem;
}

input {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 1rem;
}

.btn {
  width: 100%;
  padding: 0.6rem;
  background-color: #065fd4;
  color: #fff;
  font-weight: 500;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-top: 1rem;
  transition: background-color 0.2s;
}

.btn:hover {
  background-color: #0341a1;
}

.error-message {
  color: red;
  margin-top: 1rem;
  text-align: center;
}

.register-link {
  margin-top: 1rem;
  text-align: center;
  font-size: 0.9rem;
}

.register-link a {
  color: #065fd4;
  text-decoration: none;
  font-weight: 500;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>
