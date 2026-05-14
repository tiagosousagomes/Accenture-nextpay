/// <reference types="vite/client" />

import axios from 'axios';

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const userString = localStorage.getItem('marketpay_user');
    if (userString) {
      const user = JSON.parse(userString);
      if (user.token) {
        config.headers['Authorization'] = `Bearer ${user.token}`;
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      console.error('Não autorizado - redirecionando para login');
    }
    return Promise.reject(error);
  }
);

export default api;
