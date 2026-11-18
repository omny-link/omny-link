import adapter from '@sveltejs/adapter-static';
import { vitePreprocess } from '@sveltejs/vite-plugin-svelte';

// Determine environment: default to 'dev', allow override via VITE_ENV or NODE_ENV
const env = process.env.VITE_ENV || process.env.NODE_ENV || 'dev';

const config = {
	preprocess: vitePreprocess(),
	kit: { adapter: adapter() },
	vitePlugin: {
		// Expose environment variable to app code
		define: {
			'__APP_ENV__': JSON.stringify(env)
		}
	}
};

export default config;
