import Keycloak from 'keycloak-js';
import { writable } from 'svelte/store';
import type { UserInfo } from './types';

const keycloak = new Keycloak('/keycloak.json');

// Create stores for reactive state
export const keycloakStore = writable<{
	initialized: boolean;
	authenticated: boolean;
	token: string | null;
	userInfo: UserInfo | null;
}>({
	initialized: false,
	authenticated: false,
	token: null,
	userInfo: null
});

let initPromise: Promise<any> | null = null;

// Initialize keycloak once
export async function initKeycloak() {
	if (initPromise) return initPromise;

	// Check if Web Crypto API is available for PKCE
	const usePKCE = window.crypto && window.crypto.subtle ? 'S256' : false;

	initPromise = keycloak
		.init({
			onLoad: 'check-sso',
			pkceMethod: usePKCE,
			flow: 'standard', // Use standard flow instead of implicit
			checkLoginIframe: false // Disable iframe for better performance
		})
		.then(() => {
			const userInfo: UserInfo | null = keycloak.tokenParsed ? {
				username: keycloak.tokenParsed.preferred_username,
				email: keycloak.tokenParsed.email,
				preferred_username: keycloak.tokenParsed.preferred_username,
				given_name: keycloak.tokenParsed.given_name,
				family_name: keycloak.tokenParsed.family_name,
				name: keycloak.tokenParsed.name,
				email_verified: keycloak.tokenParsed.email_verified,
				sub: keycloak.tokenParsed.sub,
				realm_access: keycloak.tokenParsed.realm_access,
				resource_access: keycloak.tokenParsed.resource_access,
				...keycloak.tokenParsed
			} : null;
			
			keycloakStore.set({
				initialized: true,
				authenticated: keycloak.authenticated || false,
				token: keycloak.token || null,
				userInfo
			});

			// Setup automatic token refresh
			setupTokenRefresh();

			return keycloak;
		})
		.catch((err) => {
			console.error('Keycloak init error:', err);
			throw err;
		});

	return initPromise;
}

/**
 * Setup automatic token refresh
 * Refreshes token when it's about to expire (within 70% of its lifetime)
 */
function setupTokenRefresh() {
	if (!keycloak.authenticated) return;

	// Update token every 5 seconds (checks if refresh is needed)
	keycloak.onTokenExpired = () => {
		console.log('Token expired, refreshing...');
		refreshToken();
	};

	// Proactively refresh token when it's 70% through its lifetime
	setInterval(() => {
		keycloak
			.updateToken(70) // Refresh if token expires in 70 seconds
			.then((refreshed) => {
				if (refreshed) {
					console.log('Token refreshed');
					updateStoreToken();
				}
			})
			.catch((err) => {
				console.error('Failed to refresh token:', err);
				// Token refresh failed, user needs to re-login
				keycloak.login();
			});
	}, 60000); // Check every minute
}

/**
 * Manually refresh the token
 */
export async function refreshToken(): Promise<boolean> {
	try {
		const refreshed = await keycloak.updateToken(-1); // Force refresh
		if (refreshed) {
			updateStoreToken();
			return true;
		}
		return false;
	} catch (err) {
		console.error('Token refresh failed:', err);
		// Redirect to login if refresh fails
		keycloak.login();
		return false;
	}
}

/**
 * Update the store with new token
 */
function updateStoreToken() {
	keycloakStore.update((state) => ({
		...state,
		token: keycloak.token || null
	}));
}

/**
 * Login with Keycloak
 */
export function login() {
	keycloak.login();
}

/**
 * Logout from Keycloak
 */
export function logout() {
	keycloak.logout();
}

// Fetch user account info from Keycloak to get tenant
export async function fetchUserAccount() {
	if (!keycloak.authenticated || !keycloak.token) {
		throw new Error('User not authenticated');
	}

	const authServerUrl = keycloak.authServerUrl || 'https://auth.knowprocess.com/auth/';
	const realm = keycloak.realm || 'knowprocess';
	const url = `${authServerUrl}realms/${realm}/account`;

	try {
		const response = await fetch(url, {
			headers: {
				'Authorization': `Bearer ${keycloak.token}`,
				'Accept': 'application/json'
			}
		});

		if (!response.ok) {
			throw new Error(`Failed to fetch user account: ${response.status}`);
		}

		const userAccount = await response.json();
		const tenant = userAccount.attributes?.tenant?.[0] || userAccount.attributes?.tenantId?.[0] || 'acme';
		
		// Update store with user account info
		keycloakStore.update(state => ({
			...state,
			userInfo: state.userInfo ? {
				...state.userInfo,
				tenant,
				attributes: userAccount.attributes
			} : null
		}));

		return { userAccount, tenant };
	} catch (err) {
		console.error('Error fetching user account:', err);
		// Return default tenant on error
		return { userAccount: null, tenant: 'acme' };
	}
}

export default keycloak;
