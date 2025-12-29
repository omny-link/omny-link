import { writable } from 'svelte/store';

export interface TenantTheme {
  logoUrl?: string;
  iconUrl?: string;
  cssUrl?: string;
}

export interface TenantConfig {
  theme?: TenantTheme;
  [key: string]: any;
}

function createTenantConfigStore() {
  const { subscribe, set, update } = writable<TenantConfig | null>(null);

  return {
    subscribe,
    set,
    update,
    load: async (tenantId: string) => {
      try {
        const response = await fetch(`https://cloud.knowprocess.com/tenants/alife/${tenantId}.json`);
        if (response.ok) {
          const config = await response.json();
          console.log('Tenant config loaded:', config);
          set(config);
          
          // Apply tenant theme
          if (config.theme) {
            // Update favicon
            if (config.theme.iconUrl) {
              let link = document.querySelector("link[rel*='icon']") as HTMLLinkElement;
              if (!link) {
                link = document.createElement('link');
                link.rel = 'icon';
                document.head.appendChild(link);
              }
              link.href = config.theme.iconUrl;
            }
            
            // Load tenant CSS
            if (config.theme.cssUrl) {
              let existingLink = document.querySelector(`link[href="${config.theme.cssUrl}"]`);
              if (!existingLink) {
                const link = document.createElement('link');
                link.rel = 'stylesheet';
                link.href = config.theme.cssUrl;
                document.head.appendChild(link);
              }
            }
          }
        } else {
          console.warn(`Failed to load tenant config for ${tenantId}: ${response.status}`);
        }
      } catch (error) {
        console.error('Error loading tenant config:', error);
      }
    }
  };
}

export const tenantConfigStore = createTenantConfigStore();
