import Keycloak from 'keycloak-js';
import { writable } from 'svelte/store';

const keycloak = new Keycloak('/keycloak.json');

// Create stores for reactive state
export const keycloakStore = writable({
	initialized: false,
	authenticated: false,
	token: null,
	tokenParsed: null
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

export default keycloak;
