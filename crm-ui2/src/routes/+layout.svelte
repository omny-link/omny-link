<script lang="ts">
  import { onMount } from 'svelte';
  import '../app.css';
  import { applyBootstrapTheme } from '$lib/theme';
  import keycloak, { initKeycloak, keycloakStore } from '$lib/keycloak';
  import Sidebar from '$lib/components/Sidebar.svelte';
  import type { UserInfo } from '$lib/types';

  let authenticated: boolean = false;
  let username: string = '';
  let userEmail: string = '';
  let sidebarCollapsed: boolean = false;
  let sidebar: any;

  function toggleSidebar(): void {
    if (sidebar) {
      sidebar.toggle();
      sidebarCollapsed = !sidebarCollapsed;
    }
  }

  onMount(async () => {
    applyBootstrapTheme();

    try {
      await initKeycloak();
      
      // Subscribe to keycloak store for reactive updates
      keycloakStore.subscribe(state => {
        authenticated = state.authenticated;
        username = state.userInfo?.username || state.userInfo?.preferred_username || '';
        userEmail = state.userInfo?.email || username;
      });
    } catch (err) {
      console.error('Keycloak init error:', err);
    }
  });
</script>

<Sidebar 
  bind:this={sidebar}
  bind:collapsed={sidebarCollapsed}
  {authenticated}
  {username}
  {userEmail}
/>

<main id="content" class="{sidebarCollapsed ? 'sidebar-collapsed' : ''} container mt-4 text-light bg-dark p-4 rounded">
  <slot />
</main>


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