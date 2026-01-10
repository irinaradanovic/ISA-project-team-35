<template>
  <div class="register">
    <h2>Register</h2>
    <form @submit.prevent="handleRegister">
      <div>
        <label>First Name:</label>
        <input v-model="firstName" type="text" required />
      </div>
      <div>
        <label>Last Name:</label>
        <input v-model="lastName" type="text" required />
      </div>
      <div>
        <label>Username:</label>
        <input v-model="username" type="text" required />
      </div>
      <div>
        <label>Email:</label>
        <input v-model="email" type="email" required />
      </div>
      <div>
        <label>Address:</label>
        <input v-model="address" type="text" required />
      </div>
      <div>
        <label>Password:</label>
        <input v-model="password" type="password" required />
      </div>
      <div>
        <label>Confirm Password:</label>
        <input v-model="confirmPassword" type="password" required />
      </div>
      <button type="submit">Register</button>
    </form>
    <p v-if="auth.error" style="color:red">{{ auth.error }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useAuthStore } from '../stores/auth';

const auth = useAuthStore();

const firstName = ref('');
const lastName = ref('');
const username = ref('');
const email = ref('');
const address = ref('');
const password = ref('');
const confirmPassword = ref('');

const handleRegister = async () => {
  const success = await auth.doRegister({
    firstName: firstName.value,
    lastName: lastName.value,
    username: username.value,
    email: email.value,
    address: address.value,
    password: password.value,
    confirmPassword: confirmPassword.value
  });
  if(success) alert('Registration successful! Check your email for activation link.');
};
</script>
