function getApiBase(): string {
  if (process.env.NEXT_PUBLIC_API_URL) return process.env.NEXT_PUBLIC_API_URL;
  if (typeof window !== 'undefined' && window.location.hostname) {
    return `${window.location.protocol}//${window.location.hostname}:8000`;
  }
  return 'http://localhost:8000';
}

const API_BASE = getApiBase();


async function apiFetch(path: string, options: RequestInit = {}) {
  const headers = new Headers(options.headers);
  if (!headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  const token = typeof window !== 'undefined' ? localStorage.getItem('kc_token') : null;
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({ detail: 'Request failed' }));
    throw new Error(err.detail || 'Request failed');
  }
  return res.json();
}

// Auth
export const api = {
  register: (name: string, email: string, password: string) =>
    apiFetch('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify({ name, email, password }),
    }),

  login: (email: string, password: string) => {
    const form = new URLSearchParams();
    form.append('username', email);
    form.append('password', password);
    return apiFetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: form,
    });
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
  searchVideos: (q: string, age?: number, aiScore = false) => {
    const params = new URLSearchParams({ q, ai_score: String(aiScore) });
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

  // AI
  generateCurriculum: (data: { child_age: number; subject: string; goals?: string; num_days?: number }) =>
    apiFetch('/api/ai/curriculum', { method: 'POST', body: JSON.stringify(data) }),
  applyCurriculum: (childId: string, curriculum: any) =>
    apiFetch('/api/ai/curriculum/apply', { method: 'POST', body: JSON.stringify({ child_id: childId, curriculum }) }),
};

export default api;
