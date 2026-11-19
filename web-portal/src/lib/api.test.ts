import test, { describe, beforeEach, afterEach } from 'node:test';
import assert from 'node:assert/strict';

// In-memory mock localStorage
class MockLocalStorage {
  private store: Map<string, string> = new Map();

  getItem(key: string): string | null {
    return this.store.has(key) ? this.store.get(key)! : null;
  }

  setItem(key: string, value: string): void {
    this.store.set(key, String(value));
  }

  removeItem(key: string): void {
    this.store.delete(key);
  }

  clear(): void {
    this.store.clear();
  }
}

describe('Next.js API Client — JWT Token Management & Auto-Refresh', () => {
  let originalFetch: typeof globalThis.fetch;
  let mockStorage: MockLocalStorage;

  beforeEach(() => {
    originalFetch = globalThis.fetch;
    mockStorage = new MockLocalStorage();
    (globalThis as any).localStorage = mockStorage;
    (globalThis as any).window = {
      location: {
        protocol: 'http:',
        hostname: 'localhost',
        pathname: '/dashboard',
        href: 'http://localhost:3000/dashboard',
      },
    };
  });

  afterEach(() => {
    globalThis.fetch = originalFetch;
    delete (globalThis as any).localStorage;
    delete (globalThis as any).window;
  });

  test('setTokens and getTokens correctly persist and retrieve tokens', async () => {
    const { setTokens, getAccessToken, getRefreshToken, clearTokens } = await import('./api.ts');

    setTokens('initial_access_123', 'initial_refresh_456');
    assert.equal(getAccessToken(), 'initial_access_123');
    assert.equal(getRefreshToken(), 'initial_refresh_456');

    clearTokens();
    assert.equal(getAccessToken(), null);
    assert.equal(getRefreshToken(), null);
  });

  test('apiFetch automatically includes Authorization header when token is present', async () => {
    const { apiFetch, setTokens } = await import('./api.ts');
    setTokens('my_valid_jwt_token', 'valid_refresh');

    let capturedHeaders: Headers | null = null;
    globalThis.fetch = (async (url: string | URL | Request, init?: RequestInit) => {
      capturedHeaders = new Headers(init?.headers);
      return new Response(JSON.stringify({ status: 'ok' }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      });
    }) as any;

    const res = await apiFetch('/api/children/');
    assert.deepEqual(res, { status: 'ok' });
    assert.equal(capturedHeaders!.get('Authorization'), 'Bearer my_valid_jwt_token');
  });

  test('apiFetch refreshes expired token on 401 and retries the request', async () => {
    const { apiFetch, setTokens, getAccessToken, getRefreshToken } = await import('./api.ts');
    setTokens('expired_access_token', 'valid_refresh_token');

    let requestCount = 0;
    let refreshCount = 0;

    globalThis.fetch = (async (url: string | URL | Request, init?: RequestInit) => {
      const urlStr = url.toString();
      if (urlStr.includes('/api/auth/refresh')) {
        refreshCount++;
        const body = JSON.parse((init?.body as string) || '{}');
        assert.equal(body.refresh_token, 'valid_refresh_token');
        return new Response(
          JSON.stringify({
            access_token: 'newly_minted_access_token',
            refresh_token: 'rotated_refresh_token',
          }),
          { status: 200, headers: { 'Content-Type': 'application/json' } }
        );
      }

      requestCount++;
      const authHeader = new Headers(init?.headers).get('Authorization');
      if (authHeader === 'Bearer expired_access_token') {
        return new Response(JSON.stringify({ detail: 'Signature has expired' }), {
          status: 401,
          headers: { 'Content-Type': 'application/json' },
        });
      }

      if (authHeader === 'Bearer newly_minted_access_token') {
        return new Response(JSON.stringify({ success: true, retried: true }), {
          status: 200,
          headers: { 'Content-Type': 'application/json' },
        });
      }

      return new Response(JSON.stringify({ detail: 'Unauthorized' }), { status: 401 });
    }) as any;

    const result = await apiFetch('/api/children/');
    assert.equal(refreshCount, 1, 'Refresh should have been called exactly once');
    assert.equal(requestCount, 2, 'Original request should have been made twice (initial + retry)');
    assert.deepEqual(result, { success: true, retried: true });
    assert.equal(getAccessToken(), 'newly_minted_access_token');
    assert.equal(getRefreshToken(), 'rotated_refresh_token');
  });

  test('Concurrent requests trigger only ONE refresh call and all succeed (Mutex test)', async () => {
    const { apiFetch, setTokens, getAccessToken } = await import('./api.ts');
    setTokens('expired_token_concurrent', 'valid_refresh_concurrent');

    let refreshCallCount = 0;
    let apiCallCount = 0;

    globalThis.fetch = (async (url: string | URL | Request, init?: RequestInit) => {
      const urlStr = url.toString();
      if (urlStr.includes('/api/auth/refresh')) {
        refreshCallCount++;
        // Simulate minor network delay
        await new Promise((r) => setTimeout(r, 20));
        return new Response(
          JSON.stringify({
            access_token: 'new_concurrent_access_token',
            refresh_token: 'new_concurrent_refresh_token',
          }),
          { status: 200, headers: { 'Content-Type': 'application/json' } }
        );
      }

      apiCallCount++;
      const authHeader = new Headers(init?.headers).get('Authorization');
      if (authHeader === 'Bearer expired_token_concurrent') {
        return new Response(JSON.stringify({ detail: 'Token expired' }), {
          status: 401,
          headers: { 'Content-Type': 'application/json' },
        });
      }

      if (authHeader === 'Bearer new_concurrent_access_token') {
        return new Response(JSON.stringify({ item: urlStr.split('/').pop() }), {
          status: 200,
          headers: { 'Content-Type': 'application/json' },
        });
      }

      return new Response(JSON.stringify({ detail: 'Forbidden' }), { status: 403 });
    }) as any;

    // Fire 4 concurrent requests at once
    const [res1, res2, res3, res4] = await Promise.all([
      apiFetch('/api/children/1'),
      apiFetch('/api/children/2'),
      apiFetch('/api/children/3'),
      apiFetch('/api/children/4'),
    ]);

    assert.equal(refreshCallCount, 1, 'Only ONE refresh call should occur despite 4 concurrent requests');
    assert.equal(apiCallCount, 8, '4 initial calls + 4 retried calls should occur');
    assert.deepEqual(res1, { item: '1' });
    assert.deepEqual(res2, { item: '2' });
    assert.deepEqual(res3, { item: '3' });
    assert.deepEqual(res4, { item: '4' });
    assert.equal(getAccessToken(), 'new_concurrent_access_token');
  });

  test('Refresh failure clears tokens and triggers safe logout', async () => {
    const { apiFetch, setTokens, getAccessToken, getRefreshToken } = await import('./api.ts');
    setTokens('bad_access', 'expired_refresh');

    globalThis.fetch = (async (url: string | URL | Request) => {
      const urlStr = url.toString();
      if (urlStr.includes('/api/auth/refresh')) {
        return new Response(JSON.stringify({ detail: 'Refresh token expired' }), {
          status: 401,
          headers: { 'Content-Type': 'application/json' },
        });
      }
      return new Response(JSON.stringify({ detail: 'Invalid credentials' }), {
        status: 401,
        headers: { 'Content-Type': 'application/json' },
      });
    }) as any;

    await assert.rejects(
      async () => {
        await apiFetch('/api/children/');
      },
      { message: /Session expired/ }
    );

    assert.equal(getAccessToken(), null, 'Access token should be wiped on failed refresh');
    assert.equal(getRefreshToken(), null, 'Refresh token should be wiped on failed refresh');
    assert.equal((globalThis as any).window.location.href, '/', 'Should redirect to root login');
  });
});
