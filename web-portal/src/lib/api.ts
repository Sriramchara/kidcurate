function getApiBase(): string {
  if (process.env.NEXT_PUBLIC_API_URL) return process.env.NEXT_PUBLIC_API_URL;
  if (typeof window !== 'undefined' && window.location.hostname) {
    return `${window.location.protocol}//${window.location.hostname}:8000`;
  }
  return 'http://localhost:8000';
}

export const API_BASE = getApiBase();

// Storage keys
export const ACCESS_TOKEN_KEY = 'kc_token';
export const REFRESH_TOKEN_KEY = 'kc_refresh_token';
export const USER_KEY = 'kc_user';

export function getAccessToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function getRefreshToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem(REFRESH_TOKEN_KEY);
}

export function setTokens(accessToken: string, refreshToken?: string | null): void {
  if (typeof window === 'undefined') return;
  localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
  if (refreshToken) {
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  }
}

export function clearTokens(): void {
  if (typeof window === 'undefined') return;
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

export function safeLogout(): void {
  clearTokens();
  if (typeof window !== 'undefined' && window.location.pathname !== '/') {
    // eslint-disable-next-line @next/next/no-location-assign-relative-destination
    window.location.href = '/';
  }
}

// In-flight refresh promise mutex for handling concurrent requests
let refreshPromise: Promise<string> | null = null;

export async function refreshAccessToken(): Promise<string> {
  // If a refresh is already underway, await that exact promise
  if (refreshPromise) {
    return refreshPromise;
  }

  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    clearTokens();
    throw new Error('No refresh token available');
  }

  refreshPromise = (async () => {
    try {
      const res = await fetch(`${API_BASE}/api/auth/refresh`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ refresh_token: refreshToken }),
      });

      if (!res.ok) {
        clearTokens();
        throw new Error('Session expired. Please log in again.');
      }

      const data = await res.json();
      if (!data.access_token) {
        clearTokens();
        throw new Error('Invalid token response from server');
      }

      setTokens(data.access_token, data.refresh_token || refreshToken);
      return data.access_token as string;
    } catch (err) {
      clearTokens();
      throw err;
    } finally {
      refreshPromise = null;
    }
  })();

  return refreshPromise;
}

export async function apiFetch(path: string, options: RequestInit = {}): Promise<any> {
  const isAuthRoute =
    path === '/api/auth/login' ||
    path === '/api/auth/register' ||
    path === '/api/auth/refresh';

  const makeRequest = async (token: string | null) => {
    const headers = new Headers(options.headers);
    if (!headers.has('Content-Type') && !(options.body instanceof FormData) && !(options.body instanceof URLSearchParams)) {
      headers.set('Content-Type', 'application/json');
    }
    if (token) {
      headers.set('Authorization', `Bearer ${token}`);
    }

    return fetch(`${API_BASE}${path}`, {
      ...options,
      credentials: 'include',
      headers,
    });
  };

  const initialToken = getAccessToken();
  let res = await makeRequest(initialToken);

  // If 401 Unauthorized and not an auth route, attempt token refresh once
  if (res.status === 401 && !isAuthRoute) {
    try {
      const newToken = await refreshAccessToken();
      res = await makeRequest(newToken);
    } catch {
      safeLogout();
      throw new Error('Session expired. Please log in again.');
    }
  }

  if (!res.ok) {
    const err = await res.json().catch(() => ({ detail: 'Request failed' }));
    throw new Error(err.detail || 'Request failed');
  }

  // Handle empty or 204 responses
  if (res.status === 204) return null;
  const contentType = res.headers.get('content-type');
  if (contentType && contentType.includes('application/json')) {
    return res.json();
  }
  return res.text();
}

// Auth
export const api = {
  register: async (name: string, email: string, password: string) => {
    const res = await apiFetch('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify({ name, email, password }),
    });
    if (res && res.access_token) {
      setTokens(res.access_token, res.refresh_token);
    }
    return res;
  },

  login: async (email: string, password: string) => {
    const form = new URLSearchParams();
    form.append('username', email);
    form.append('password', password);
    const res = await apiFetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: form,
    });
    if (res && res.access_token) {
      setTokens(res.access_token, res.refresh_token);
    }
    return res;
  },

  logout: () => {
    safeLogout();
  },

  me: () => apiFetch('/api/auth/me'),

  updateProfile: (data: {
    name?: string;
    email?: string;
    current_password?: string;
    new_password?: string;
  }) =>
    apiFetch('/api/auth/profile', {
      method: 'PUT',
      body: JSON.stringify(data),
    }),

  deleteAccount: () =>
    apiFetch('/api/auth/account', {
      method: 'DELETE',
    }),

  forgotPassword: (email: string) =>
    apiFetch('/api/auth/forgot-password', {
      method: 'POST',
      body: JSON.stringify({ email }),
    }),

  resetPassword: (email: string, code: string, new_password: string) =>
    apiFetch('/api/auth/reset-password', {
      method: 'POST',
      body: JSON.stringify({ email, code, new_password }),
    }),

  verifyParentPassword: (email: string, password: string) =>
    apiFetch('/api/auth/verify-parent-password', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    }),

  // Children
  listChildren: () => apiFetch('/api/children/'),
  verifyChildPin: (childId: string, pin: string) =>
    apiFetch('/api/children/verify-pin', {
      method: 'POST',
      body: JSON.stringify({ child_id: childId, pin }),
    }),
  resetChildPin: (childId: string, parentEmail: string, parentPassword: string, newPin: string) =>
    apiFetch('/api/children/reset-pin', {
      method: 'POST',
      body: JSON.stringify({
        child_id: childId,
        parent_email: parentEmail,
        parent_password: parentPassword,
        new_pin: newPin,
      }),
    }),
  createChild: (data: object) => apiFetch('/api/children/', { method: 'POST', body: JSON.stringify(data) }),
  updateChild: (id: string, data: object) => apiFetch(`/api/children/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteChild: (id: string) => apiFetch(`/api/children/${id}`, { method: 'DELETE' }),

  // Videos
  searchVideos: (q: string, age?: number) => {
    const params = new URLSearchParams({ q });
    if (age) params.append('age', String(age));
    return apiFetch(`/api/videos/search?${params}`);
  },

  // Playlists
  listPlaylists: (childId?: string, date?: string) => {
    const params = new URLSearchParams();
    if (childId) params.append('child_id', childId);
    if (date) params.append('scheduled_date', date);
    return apiFetch(`/api/playlists/?${params}`);
  },
  createPlaylist: (data: object) => apiFetch('/api/playlists/', { method: 'POST', body: JSON.stringify(data) }),
  getPlaylist: (id: string) => apiFetch(`/api/playlists/${id}`),
  updatePlaylist: (id: string, data: object) => apiFetch(`/api/playlists/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deletePlaylist: (id: string) => apiFetch(`/api/playlists/${id}`, { method: 'DELETE' }),
  addVideoToPlaylist: (playlistId: string, video: object) =>
    apiFetch(`/api/playlists/${playlistId}/items`, { method: 'POST', body: JSON.stringify(video) }),
  removeVideoFromPlaylist: (playlistId: string, itemId: string) =>
    apiFetch(`/api/playlists/${playlistId}/items/${itemId}`, { method: 'DELETE' }),
  reorderPlaylist: (playlistId: string, orderedIds: string[]) =>
    apiFetch(`/api/playlists/${playlistId}/reorder`, { method: 'PUT', body: JSON.stringify(orderedIds) }),

  getTodaysPlaylist: (childId: string) => apiFetch(`/api/playlists/child/${childId}/today`),
  reportWatchProgress: (data: { child_id: string; youtube_id: string; playlist_item_id?: string; watched_seconds: number; completed: boolean }) =>
    apiFetch('/api/progress/update', { method: 'POST', body: JSON.stringify(data) }),

  // Progress
  getChildProgress: (childId: string) => apiFetch(`/api/progress/child/${childId}/summary`),
  getWatchHistory: (childId: string) => apiFetch(`/api/progress/child/${childId}/history`),
};

export default api;
