import { writable } from 'svelte/store';

export type ColorScheme = 'light' | 'dark';

function createColorSchemeStore() {
  const { subscribe, set } = writable<ColorScheme>('dark');

  return {
    subscribe,
    set,
    init: () => {
      if (typeof window !== 'undefined') {
        const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
        set(mediaQuery.matches ? 'dark' : 'light');
        
        mediaQuery.addEventListener('change', (e) => {
          set(e.matches ? 'dark' : 'light');
        });
      }
    }
  };
}

export const colorSchemeStore = createColorSchemeStore();
