<script lang="ts">
  import { onMount } from 'svelte';
  import '../app.css';
  import { applyBootstrapTheme } from '$lib/theme';
  import keycloak, { initKeycloak, keycloakStore, fetchUserAccount } from '$lib/keycloak';
  import { tenantConfigStore } from '$lib/tenantConfig';
  import Sidebar from '$lib/components/Sidebar.svelte';
  import { colorSchemeStore } from '$lib/colorScheme';
  import type { UserInfo } from '$lib/types';

  let authenticated: boolean = false;
  let username: string = '';
  let userEmail: string = '';
  let sidebarCollapsed: boolean = false;
  let sidebar: any;
  let colorScheme: 'light' | 'dark' = 'dark';
  let loading: boolean = true;
  let error: string | null = null;

  function toggleSidebar(): void {
    if (sidebar) {
      sidebar.toggle();
      sidebarCollapsed = !sidebarCollapsed;
    }
  }

  // Subscribe to color scheme changes
  colorSchemeStore.subscribe(scheme => {
    colorScheme = scheme;
    if (typeof document !== 'undefined') {
      document.documentElement.classList.remove('light-mode', 'dark-mode');
      document.documentElement.classList.add(`${colorScheme}-mode`);
    }
  });

  onMount(async () => {
    colorSchemeStore.init();
    applyBootstrapTheme();

    try {
      await initKeycloak();

      // Subscribe to keycloak store for reactive updates
      keycloakStore.subscribe(state => {
        authenticated = state.authenticated;
        username = state.userInfo?.username || state.userInfo?.preferred_username || '';
        userEmail = state.userInfo?.email || username;
      });

      // Globally fetch user account and load tenant config
      if (keycloak.authenticated) {
        try {
          const { tenant } = await fetchUserAccount();
          await tenantConfigStore.load(tenant);
        } catch (err) {
          console.warn('Failed to load tenant config, using defaults:', err);
        }
      }
      
      loading = false;
      
      // Signal that app is fully ready (after tenant config loaded)
      setTimeout(() => {
        window.dispatchEvent(new CustomEvent('app:ready'));
      }, 100);
    } catch (err) {
      console.error('Keycloak init error:', err);
      error = 'Failed to initialize authentication. Please refresh the page.';
      loading = false;
      
      // Still signal ready even on error so user isn't stuck
      window.dispatchEvent(new CustomEvent('app:ready'));
    }
  });
</script>

{#if loading}
  <div class="d-flex justify-content-center align-items-center" style="height: 100vh;">
    <div class="text-center">
      <div class="spinner-border text-primary mb-3" role="status" style="width: 3rem; height: 3rem;">
        <span class="visually-hidden">Loading...</span>
      </div>
      <p class="text-muted">Initializing application...</p>
    </div>
  </div>
{:else if error}
  <div class="d-flex justify-content-center align-items-center" style="height: 100vh;">
    <div class="alert alert-danger" role="alert">
      <h4 class="alert-heading">Authentication Error</h4>
      <p>{error}</p>
      <hr>
      <button class="btn btn-primary" on:click={() => window.location.reload()}>
        Reload Page
      </button>
    </div>
  </div>
{:else}
  <Sidebar 
    bind:this={sidebar}
    bind:collapsed={sidebarCollapsed}
    {authenticated}
    {username}
    {userEmail}
  />

  <main id="content" class="{sidebarCollapsed ? 'sidebar-collapsed' : ''} container mt-4 {colorScheme === 'dark' ? 'text-light bg-dark' : 'text-dark bg-light'} p-4 rounded">
    <slot />
  </main>
{/if}


<style>
  #content {
    margin-left: auto;
    margin-right: auto;
  }

  @media (min-width: 769px) {
    #content {
      margin-left: 295px;
      margin-right: auto;
      padding-left: 2rem;
      padding-right: 2rem;
      transition: margin-left 0.3s ease;
    }

    /* Sidebar reduces by 200px (250px - 50px), so content margin reduces by same amount */
    #content.sidebar-collapsed {
      margin-left: calc(295px - 200px); /* 95px */
    }
  }
</style>