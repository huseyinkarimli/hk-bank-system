import axios, { type AxiosError } from 'axios';
import { unwrapApiData } from './api';

export const apiClient = axios.create({
  baseURL: '',
  headers: { 'Content-Type': 'application/json' },
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('auth_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

function extractErrorMessage(data: unknown): string | null {
  if (data === null || data === undefined) return null;
  if (typeof data === 'string') return data;
  if (typeof data !== 'object') return null;
  const o = data as Record<string, unknown>;
  if (typeof o.message === 'string') return o.message;
  if (typeof o.error === 'string') return o.error;
  if (o.data !== undefined && typeof o.data === 'object' && o.data !== null) {
    const inner = (o.data as Record<string, unknown>).message;
    if (typeof inner === 'string') return inner;
  }
  return null;
}

apiClient.interceptors.response.use(
  (response) => {
    response.data = unwrapApiData(response.data);
    return response;
  },
  (error: AxiosError<unknown>) => {
    const status = error.response?.status;
    const body = error.response?.data;
    const msg =
      extractErrorMessage(body) ??
      (status ? `${status} ${error.response?.statusText ?? ''}`.trim() : null) ??
      error.message;
    return Promise.reject(new Error(msg || 'Xəta baş verdi'));
  }
);
