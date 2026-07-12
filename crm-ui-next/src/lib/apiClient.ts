/**
 * Centralized API client with JWT authentication
 * Handles token refresh, 401 errors, and automatic retry
 */

import keycloak, { refreshToken, login } from './keycloak';

// Get API base URL from environment or default to production
const getApiBase = (): string => {
  if (typeof window !== 'undefined') {
    // Check if running locally
    if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
      return 'http://localhost:8080';
    }
  }
  return 'https://crm.knowprocess.com';
};

export const API_BASE = getApiBase();

/**
 * Custom error class for API errors
 */
export class ApiError extends Error {
  constructor(
    message: string,
    public status: number,
    public response?: any
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

/**
 * Get authorization headers with current Keycloak token
 */
function getAuthHeaders(): Record<string, string> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  };

  if (keycloak.authenticated && keycloak.token) {
    headers['Authorization'] = `Bearer ${keycloak.token}`;
  }

  return headers;
}

/**
 * Make an authenticated API request with automatic token refresh and retry
 */
export async function apiRequest<T>(
  path: string,
  options: RequestInit = {},
  retryCount = 0
): Promise<T> {
  // Ensure token is fresh before making request
  if (keycloak.authenticated) {
    try {
      await keycloak.updateToken(30); // Refresh if expires in 30 seconds
    } catch (err) {
      console.error('Failed to refresh token before request:', err);
      login(); // Redirect to login if refresh fails
      throw new ApiError('Authentication required', 401);
    }
  }

  const url = path.startsWith('http') ? path : `${API_BASE}${path}`;
  const headers = { ...getAuthHeaders(), ...options.headers };

  try {
    const response = await fetch(url, {
      ...options,
      headers,
      mode: 'cors'
    });

    // Handle 401 Unauthorized
    if (response.status === 401) {
      console.warn('Received 401 Unauthorized response');

      // Try to refresh token once and retry
      if (retryCount === 0 && keycloak.authenticated) {
        console.log('Attempting token refresh and retry...');
        const refreshed = await refreshToken();
        if (refreshed) {
          // Retry request with new token
          return apiRequest<T>(path, options, retryCount + 1);
        }
      }

      // If refresh failed or already retried, redirect to login
      console.error('Token refresh failed, redirecting to login');
      login();
      throw new ApiError('Authentication required', 401);
    }

    // Handle other errors
    if (!response.ok) {
      let errorMessage = `Request failed with status ${response.status}`;
      let errorBody: any;

      try {
        errorBody = await response.json();
        errorMessage = errorBody.message || errorMessage;
      } catch {
        errorMessage = await response.text() || errorMessage;
      }

      throw new ApiError(errorMessage, response.status, errorBody);
    }

    // Handle empty responses (204 No Content, etc.)
    if (response.status === 204 || response.headers.get('content-length') === '0') {
      return null as T;
    }

    // Parse JSON response
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      return await response.json();
    }

    // Return text for non-JSON responses
    return (await response.text()) as any;
  } catch (err) {
    if (err instanceof ApiError) {
      throw err;
    }

    // Network or other errors
    console.error('API request failed:', err);
    throw new ApiError(
      err instanceof Error ? err.message : 'Network request failed',
      0
    );
  }
}

/**
 * GET request
 */
export async function get<T>(path: string, options: RequestInit = {}): Promise<T> {
  return apiRequest<T>(path, { ...options, method: 'GET' });
}

/**
 * POST request
 */
export async function post<T>(
  path: string,
  data?: any,
  options: RequestInit = {}
): Promise<T> {
  return apiRequest<T>(path, {
    ...options,
    method: 'POST',
    body: data ? JSON.stringify(data) : undefined
  });
}

/**
 * PUT request
 */
export async function put<T>(
  path: string,
  data?: any,
  options: RequestInit = {}
): Promise<T> {
  return apiRequest<T>(path, {
    ...options,
    method: 'PUT',
    body: data ? JSON.stringify(data) : undefined
  });
}

/**
 * PATCH request
 */
export async function patch<T>(
  path: string,
  data?: any,
  options: RequestInit = {}
): Promise<T> {
  return apiRequest<T>(path, {
    ...options,
    method: 'PATCH',
    body: data ? JSON.stringify(data) : undefined
  });
}

/**
 * DELETE request
 */
export async function del<T>(path: string, options: RequestInit = {}): Promise<T> {
  return apiRequest<T>(path, { ...options, method: 'DELETE' });
}

/**
 * Check if user is authenticated
 */
export function isAuthenticated(): boolean {
  return keycloak.authenticated || false;
}

/**
 * Get current user's tenant (if available)
 */
export function getCurrentTenant(): string {
  return (window as any).tenant || 'default';
}
