/**
 * Environment configuration
 * Provides environment-specific settings
 */

export interface EnvConfig {
  apiBaseUrl: string;
  keycloakUrl: string;
  environment: 'development' | 'production';
}

/**
 * Get environment configuration based on current context
 */
export function getEnvConfig(): EnvConfig {
  const isDevelopment =
    typeof window !== 'undefined' &&
    (window.location.hostname === 'localhost' ||
      window.location.hostname === '127.0.0.1' ||
      window.location.hostname.includes('192.168'));

  return {
    apiBaseUrl: isDevelopment ? 'http://localhost:8080' : 'https://crm.knowprocess.com',
    keycloakUrl: 'https://auth.knowprocess.com/auth/',
    environment: isDevelopment ? 'development' : 'production'
  };
}

/**
 * Get API base URL
 */
export function getApiBaseUrl(): string {
  return getEnvConfig().apiBaseUrl;
}

/**
 * Check if running in development mode
 */
export function isDevelopment(): boolean {
  return getEnvConfig().environment === 'development';
}

/**
 * Check if running in production mode
 */
export function isProduction(): boolean {
  return getEnvConfig().environment === 'production';
}
