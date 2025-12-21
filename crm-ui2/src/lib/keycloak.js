import Keycloak from 'keycloak-js';
import { writable } from 'svelte/store';

const keycloak = new Keycloak('/keycloak.json');

// Create stores for reactive state
export const keycloakStore = writable({
	initialized: false,
	authenticated: false,
	token: null,
	tokenParsed: null,
	tenant: null,
	userAttributes: null
});

let initPromise = null;

// Initialize keycloak once
export async function initKeycloak() {
	if (initPromise) return initPromise;

	// Check if Web Crypto API is available for PKCE
	const usePKCE = window.crypto && window.crypto.subtle ? 'S256' : false;

	initPromise = keycloak
		.init({
			onLoad: 'check-sso',
			pkceMethod: usePKCE,
			flow: 'standard' // Use standard flow instead of implicit
		})
		.then(() => {
			keycloakStore.set({
				initialized: true,
				authenticated: keycloak.authenticated,
				token: keycloak.token,
				tokenParsed: keycloak.tokenParsed
			});
			return keycloak;
		})
		.catch((err) => {
			console.error('Keycloak init error:', err);
			throw err;
		});

	return initPromise;
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
			tenant,
			userAttributes: userAccount.attributes
		}));

		return { userAccount, tenant };
	} catch (err) {
		console.error('Error fetching user account:', err);
		// Return default tenant on error
		return { userAccount: null, tenant: 'acme' };
	}
}

export default keycloak;
