/*import { defineStore } from 'pinia';
import { ref } from 'vue';
import axios from 'axios';
import jwt_decode from "jwt-decode";

// Axios base URL za backend
axios.defaults.baseURL = 'http://localhost/api';

export const useAuthStore = defineStore('auth', () => {
    const user = ref(null);        // čuva podatke o korisniku
    const token = ref(localStorage.getItem('token') || null); // token iz localStorage
    const error = ref(null);
    export const useAuthStore = defineStore('auth', () => {
        const token = ref(localStorage.getItem('token') || null);
        const user = ref(null);
        const error = ref(null);

        // ==========================
        // INIT USER FROM TOKEN
        // ==========================
        if (token.value) {
            try {
                const decoded = jwt_decode(token.value);
                user.value = { email: decoded.sub }; // sub = email iz JWT
                axios.defaults.headers.common['Authorization'] = `Bearer ${token.value}`;
            } catch (err) {
                console.error('Invalid token:', err);
                localStorage.removeItem('token');
                token.value = null;
            }
        }


        // =====================================
    // REGISTRACIJA
    // =====================================
    const doRegister = async (formData) => {
        error.value = null;
        try {
            await axios.post('/auth/register', formData); // backend endpoint
            return true;
        } catch (err) {
            // prikaz poruke greške iz Spring Boot-a
            if (err.response?.data?.message) {
                error.value = err.response.data.message;
            } else if (err.response?.data) {
                error.value = JSON.stringify(err.response.data);
            } else {
                error.value = err.message;
            }
            return false;
        }
    };

    // =====================================
    // LOGIN
    // =====================================
    const doLogin = async (formData) => {
        error.value = null;
        try {
            const response = await axios.post('/auth/login', formData);
            token.value = response.data.token;
            user.value = { email: formData.email }; // za sada samo email
            localStorage.setItem('token', token.value);
            // postavi token u axios default header za buduće requeste
            axios.defaults.headers.common['Authorization'] = `Bearer ${token.value}`;
            return true;
        } catch (err) {
            if (err.response?.data?.message) {
                error.value = err.response.data.message;
            } else if (err.response?.data) {
                error.value = JSON.stringify(err.response.data);
            } else {
                error.value = err.message;
            }
            return false;
        }
    };

    // =====================================
    // LOGOUT
    // =====================================
    const logout = () => {
        token.value = null;
        user.value = null;
        localStorage.removeItem('token');
        delete axios.defaults.headers.common['Authorization'];
    };

    return { user, token, error, doRegister, doLogin, logout };
}); */

import { defineStore } from 'pinia';
import { ref } from 'vue';
import axios from 'axios';
import { jwtDecode } from 'jwt-decode';
 // npm install jwt-decode

axios.defaults.baseURL = 'http://localhost/api';

export const useAuthStore = defineStore('auth', () => {
    const token = ref(localStorage.getItem('token') || null);
    const user = ref(null);
    const error = ref(null);

    // ==========================
    // INIT USER FROM TOKEN
    // ==========================
    if (token.value) {
        try {
            const decoded = jwtDecode(token.value);
            console.log("Sadržaj JWT tokena:", decoded);  //DEBUG
            user.value = { email: decoded.sub }; // sub = email iz JWT
            axios.defaults.headers.common['Authorization'] = `Bearer ${token.value}`;
        } catch (err) {
            console.error('Invalid token:', err);
            localStorage.removeItem('token');
            token.value = null;
        }
    }

    // =====================================
    // REGISTRATION
    // =====================================
    const doRegister = async (formData) => {
        error.value = null;
        try {
            await axios.post('/auth/register', formData);
            return true;
        } catch (err) {
            error.value = err.response?.data?.message || err.message;
            return false;
        }
    };

    // =====================================
    // LOGIN
    // =====================================
    const doLogin = async (formData) => {
        error.value = null;
        try {
            const response = await axios.post('/auth/login', formData);
            token.value = response.data.token;
            const decoded = jwtDecode(token.value);
            user.value = {
                email: decoded.sub,
                username: decoded.username || decoded.sub // Ako nema username, koristi email
            };
            localStorage.setItem('token', token.value);
            axios.defaults.headers.common['Authorization'] = `Bearer ${token.value}`;
            return true;
        } catch (err) {
            error.value = err.response?.data?.message || err.message;
            return false;
        }
    };

    // =====================================
    // LOGOUT
    // =====================================
    const logout = () => {
        token.value = null;
        user.value = null;
        localStorage.removeItem('token');
        delete axios.defaults.headers.common['Authorization'];
    };

    return { user, token, error, doRegister, doLogin, logout };
});

