import axios, { type AxiosResponse, type InternalAxiosRequestConfig } from 'axios';
import { toast } from 'sonner';
import { axiosLoadingDone, axiosLoadingStart } from '@/lib/axios-loading';
import { playError, playTransfer } from '@/lib/sounds';

const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

export const api = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

export function setAuthToken(token: string | null) {
  if (token) {
    api.defaults.headers.common.Authorization = `Bearer ${token}`;
  } else {
    delete api.defaults.headers.common.Authorization;
  }
}

function shouldPlayTransferSuccess(res: AxiosResponse): boolean {
  const method = (res.config.method || 'get').toLowerCase();
  if (!['post', 'put', 'patch'].includes(method)) return false;
  const u = `${res.config.baseURL || ''}${res.config.url || ''}`.toLowerCase();
  return (
    u.includes('/transfer') ||
    u.includes('/payment') ||
    u.includes('/transaction') ||
    u.includes('/pay/')
  );
}

api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    axiosLoadingStart();
    return config;
  },
  (error) => {
    axiosLoadingDone();
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => {
    axiosLoadingDone();
    if (shouldPlayTransferSuccess(response)) {
      playTransfer();
    }
    return response;
  },
  (error) => {
    axiosLoadingDone();

    const status = error.response?.status as number | undefined;
    const code = error.code as string | undefined;

    if (code === 'ERR_CANCELED') {
      return Promise.reject(error);
    }

    playError();

    if (status === 401) {
      localStorage.removeItem('auth_token');
      localStorage.removeItem('auth_user');
      toast.error('Sessiya bitdi, yenidən daxil olun');
      window.location.href = '/auth/login';
    } else if (status === 403) {
      toast.error('Bu əməliyyat üçün icazəniz yoxdur');
    } else if (status != null && status >= 500) {
      toast.error('Server xətası, bir az sonra yenidən cəhd edin');
    } else if (!error.response) {
      toast.error('Şəbəkə xətası, bağlantınızı yoxlayın');
    }

    return Promise.reject(error);
  }
);
